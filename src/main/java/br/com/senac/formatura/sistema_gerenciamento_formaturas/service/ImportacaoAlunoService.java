package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ImportIssue;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ImportPreview;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ImportResult;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.EstrategiaImportacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoBeca;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoCadastro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoContratual;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoEscolar;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Turma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.AlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;

@Service
public class ImportacaoAlunoService {
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaTemporaria;

    public ImportacaoAlunoService(
        AlunoRepository alunoRepository,
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.demo.student-password:}") String senhaTemporaria
    ) {
        this.alunoRepository = alunoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaTemporaria = senhaTemporaria;
    }

    public ImportPreview preview(MultipartFile arquivo, Turma turma) {
        ParsedSheet sheet = parse(arquivo);
        List<ImportIssue> issues = new ArrayList<>();
        long validos = 0;
        long incompletos = 0;
        long duplicados = 0;

        for (ParsedRow row : sheet.rows()) {
            RowData data = toRowData(row);
            if (data.nome().isBlank()) {
                incompletos++;
                issues.add(new ImportIssue(row.line(), "nome", "Nome obrigatorio.", "erro"));
                continue;
            }
            if (data.identificador().isBlank()) {
                incompletos++;
                issues.add(new ImportIssue(row.line(), "identificador", "Identificador ausente; sera gerado pelo nome.", "aviso"));
            }
            String identificador = data.identificador().isBlank() ? gerarIdentificadorBase(data.nome()) : normalizarIdentificador(data.identificador());
            if (alunoRepository.findFirstByTurmaIdAndIdentificadorIgnoreCase(turma.getId(), identificador).isPresent()) {
                duplicados++;
                issues.add(new ImportIssue(row.line(), "identificador", "Possivel duplicidade nesta turma.", "aviso"));
            }
            validos++;
        }

        return new ImportPreview(
            arquivo.getOriginalFilename(),
            sheet.headers(),
            sheet.rows().stream().limit(8).map(ParsedRow::values).toList(),
            sugerirMapeamento(sheet.headers()),
            issues,
            validos,
            incompletos,
            duplicados
        );
    }

    @Transactional
    public ImportResult confirmar(MultipartFile arquivo, Turma turma, EstrategiaImportacao estrategia) {
        ParsedSheet sheet = parse(arquivo);
        List<ImportIssue> issues = new ArrayList<>();
        long importados = 0;
        long atualizados = 0;
        long ignorados = 0;

        List<RowCandidate> candidates = new ArrayList<>();
        for (ParsedRow row : sheet.rows()) {
            RowData data = toRowData(row);
            if (data.nome().isBlank()) {
                issues.add(new ImportIssue(row.line(), "nome", "Nome obrigatorio.", "erro"));
                continue;
            }
            String identificador = data.identificador().isBlank() ? gerarIdentificadorBase(data.nome()) : normalizarIdentificador(data.identificador());
            if (identificador.isBlank()) {
                issues.add(new ImportIssue(row.line(), "identificador", "Nao foi possivel gerar identificador.", "erro"));
                continue;
            }
            var existente = alunoRepository.findFirstByTurmaIdAndIdentificadorIgnoreCase(turma.getId(), identificador);
            if (existente.isPresent() && estrategia == EstrategiaImportacao.CANCELAR_IMPORTACAO) {
                issues.add(new ImportIssue(row.line(), "identificador", "Duplicidade encontrada; importacao cancelada pela estrategia escolhida.", "erro"));
            }
            candidates.add(new RowCandidate(row.line(), data, identificador, existente.orElse(null)));
        }

        boolean hasBlockingError = issues.stream().anyMatch(issue -> "erro".equalsIgnoreCase(issue.severidade()))
            && estrategia == EstrategiaImportacao.CANCELAR_IMPORTACAO;
        if (hasBlockingError) {
            return new ImportResult(0, 0, 0, issues.size(), issues);
        }

        for (RowCandidate candidate : candidates) {
            if (candidate.existing() != null && estrategia == EstrategiaImportacao.IGNORAR_DUPLICADOS) {
                ignorados++;
                continue;
            }

            Aluno aluno = candidate.existing() == null ? new Aluno() : candidate.existing();
            aluno.setNome(candidate.data().nome());
            aluno.setIdentificador(candidate.identificador());
            aluno.setEmail(candidate.data().email());
            aluno.setTelefone(candidate.data().telefone());
            aluno.setWhatsapp(candidate.data().whatsapp());
            aluno.setContato(firstNonBlank(candidate.data().email(), candidate.data().whatsapp(), candidate.data().telefone()));
            aluno.setTurma(turma);
            aluno.setCpf("");
            aluno.setSituacaoEscolar(parseEnum(SituacaoEscolar.class, candidate.data().situacaoEscolar(), SituacaoEscolar.REGULAR));
            aluno.setSituacaoContratual(parseEnum(SituacaoContratual.class, candidate.data().situacaoContratual(), SituacaoContratual.NAO_INFORMADA));
            aluno.setSituacaoBeca(parseEnum(SituacaoBeca.class, candidate.data().situacaoBeca(), SituacaoBeca.NAO_INFORMADA));
            aluno.setStatusDocumental(StatusDocumento.PENDENTE);
            aluno.setStatusCadastro(isCadastroCompleto(aluno) ? SituacaoCadastro.COMPLETO : SituacaoCadastro.INCOMPLETO);
            aluno.setStatus("ATIVO");
            aluno.setAtivo(true);
            aluno.setPrecisaTrocarSenha(true);
            aluno.setDataInclusao(aluno.getDataInclusao() == null ? LocalDateTime.now() : aluno.getDataInclusao());
            aluno.setDataUltimaAtualizacao(LocalDateTime.now());
            Aluno salvo = alunoRepository.save(aluno);
            ensureUsuarioAluno(salvo, turma);

            if (candidate.existing() == null) importados++;
            else atualizados++;
        }

        long erros = issues.stream().filter(issue -> "erro".equalsIgnoreCase(issue.severidade())).count();
        return new ImportResult(importados, atualizados, ignorados, erros, issues);
    }

    private void ensureUsuarioAluno(Aluno aluno, Turma turma) {
        if (usuarioRepository.findByAlunoId(aluno.getId()).isPresent()) return;
        if (senhaTemporaria == null || senhaTemporaria.isBlank()) return;
        Usuario usuario = new Usuario();
        usuario.setLogin(gerarLoginUnico(aluno.getIdentificador(), turma));
        usuario.setEmail(firstNonBlank(aluno.getEmail(), usuario.getLogin() + "@formaly.demo"));
        usuario.setNome(aluno.getNome());
        usuario.setSenha(passwordEncoder.encode(senhaTemporaria));
        usuario.setPerfil(Perfil.ROLE_ALUNO);
        usuario.setAluno(aluno);
        usuario.setOrganizacaoAtual(turma.getOrganizacao());
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private String gerarLoginUnico(String identificador, Turma turma) {
        String base = normalizarIdentificador(identificador);
        String candidato = base;
        if (usuarioRepository.findUsuarioByLogin(candidato).isEmpty()) return candidato;
        candidato = base + ".t" + turma.getId();
        int count = 2;
        while (usuarioRepository.findUsuarioByLogin(candidato).isPresent()) {
            candidato = base + ".t" + turma.getId() + "." + count++;
        }
        return candidato;
    }

    private ParsedSheet parse(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo obrigatorio.");
        }
        String name = arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".xlsx")) return parseXlsx(arquivo);
            if (name.endsWith(".csv")) return parseCsv(arquivo);
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao foi possivel ler a planilha.");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato nao suportado. Envie CSV ou XLSX.");
    }

    private ParsedSheet parseCsv(MultipartFile arquivo) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return new ParsedSheet(List.of(), List.of());
            String delimiter = headerLine.contains(";") ? ";" : ",";
            List<String> headers = split(headerLine, delimiter).stream().map(this::normalizarCabecalho).toList();
            List<ParsedRow> rows = new ArrayList<>();
            String line;
            int lineNumber = 2;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    lineNumber++;
                    continue;
                }
                List<String> values = split(line, delimiter);
                rows.add(new ParsedRow(lineNumber, toMap(headers, values)));
                lineNumber++;
            }
            return new ParsedSheet(headers, rows);
        }
    }

    private ParsedSheet parseXlsx(MultipartFile arquivo) throws Exception {
        try (var workbook = WorkbookFactory.create(arquivo.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));
            if (sheet.getPhysicalNumberOfRows() == 0) return new ParsedSheet(List.of(), List.of());
            var headerRow = sheet.getRow(sheet.getFirstRowNum());
            List<String> headers = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                headers.add(normalizarCabecalho(formatter.formatCellValue(headerRow.getCell(cellIndex))));
            }
            List<ParsedRow> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                var row = sheet.getRow(rowIndex);
                if (row == null) continue;
                List<String> values = new ArrayList<>();
                boolean empty = true;
                for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
                    String value = formatter.formatCellValue(row.getCell(cellIndex)).trim();
                    if (!value.isBlank()) empty = false;
                    values.add(value);
                }
                if (!empty) rows.add(new ParsedRow(rowIndex + 1, toMap(headers, values)));
            }
            return new ParsedSheet(headers, rows);
        }
    }

    private List<String> split(String line, String delimiter) {
        return List.of(line.split(java.util.regex.Pattern.quote(delimiter), -1)).stream()
            .map(String::trim)
            .toList();
    }

    private Map<String, String> toMap(List<String> headers, List<String> values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            map.put(headers.get(index), index < values.size() ? values.get(index).trim() : "");
        }
        return map;
    }

    private Map<String, String> sugerirMapeamento(List<String> headers) {
        Map<String, String> mapping = new LinkedHashMap<>();
        List.of("nome", "identificador", "email", "telefone", "whatsapp", "situacao_escolar", "situacao_contratual", "situacao_beca")
            .forEach(field -> mapping.put(field, headers.stream().filter(header -> header.equals(field)).findFirst().orElse("")));
        return mapping;
    }

    private RowData toRowData(ParsedRow row) {
        Map<String, String> values = row.values();
        return new RowData(
            firstNonBlank(values.get("nome"), values.get("nome_completo"), values.get("aluno")),
            firstNonBlank(values.get("identificador"), values.get("matricula"), values.get("ra")),
            values.getOrDefault("email", ""),
            values.getOrDefault("telefone", ""),
            firstNonBlank(values.get("whatsapp"), values.get("celular")),
            values.getOrDefault("situacao_escolar", ""),
            values.getOrDefault("situacao_contratual", ""),
            values.getOrDefault("situacao_beca", "")
        );
    }

    private String normalizarCabecalho(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");
        return normalized;
    }

    private String gerarIdentificadorBase(String nome) {
        String[] partes = Normalizer.normalize(nome == null ? "" : nome, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .trim()
            .split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) return "";
        String base = partes.length == 1 ? partes[0] : partes[0] + "." + partes[partes.length - 1];
        return normalizarIdentificador(base);
    }

    private String normalizarIdentificador(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]", ".")
            .replaceAll("\\.{2,}", ".")
            .replaceAll("^[._-]+|[._-]+$", "");
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, E fallback) {
        String normalized = normalizarCabecalho(value).toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) return fallback;
        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException error) {
            return fallback;
        }
    }

    private boolean isCadastroCompleto(Aluno aluno) {
        return !firstNonBlank(aluno.getNome()).isBlank()
            && !firstNonBlank(aluno.getEmail(), aluno.getWhatsapp(), aluno.getTelefone()).isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private record ParsedSheet(List<String> headers, List<ParsedRow> rows) {}
    private record ParsedRow(int line, Map<String, String> values) {}
    private record RowData(
        String nome,
        String identificador,
        String email,
        String telefone,
        String whatsapp,
        String situacaoEscolar,
        String situacaoContratual,
        String situacaoBeca
    ) {}
    private record RowCandidate(int line, RowData data, String identificador, Aluno existing) {}
}
