package br.com.senac.formatura.sistema_gerenciamento_formaturas.controller;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.AlunoRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.AlunoResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.AnaliseComprovanteRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.AnaliseDocumentoRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.Atalho;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.AtividadeResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ChecklistItem;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ComprovanteResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.DashboardResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.DocumentoResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.EventoResumo;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ImportPreview;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ImportResult;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.InstituicaoRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.InstituicaoResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.Metric;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.RelatorioResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.RequisitoDocumentoRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ResponsavelRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.ResponsavelResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.RevisaoDocumentoResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.TipoDocumentoRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.TipoDocumentoResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.TurmaPendencia;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.TurmaRequest;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.dto.BusinessDtos.TurmaResponse;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Comprovante;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.DocumentoAluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.EstrategiaImportacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Evento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.HistoricoAuditoria;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Instituicao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RequisitoDocumentoTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Responsavel;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoBeca;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoCadastro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoContratual;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoEscolar;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusComprovante;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusRegistro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.TipoDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Turma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.AlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.ComprovanteRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.DocumentoAlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.EventoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.HistoricoAuditoriaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.InstituicaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RequisitoDocumentoTurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.ResponsavelRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RevisaoDocumentoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TipoDocumentoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.service.AuditService;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.service.BusinessAuthorizationService;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.service.DocumentoBusinessService;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.service.ImportacaoAlunoService;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.service.StorageService;

@RestController
@RequestMapping("/api/business")
public class BusinessController {
    private final BusinessAuthorizationService authz;
    private final AuditService auditService;
    private final DocumentoBusinessService documentoService;
    private final ImportacaoAlunoService importacaoAlunoService;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final InstituicaoRepository instituicaoRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final RequisitoDocumentoTurmaRepository requisitoRepository;
    private final DocumentoAlunoRepository documentoRepository;
    private final RevisaoDocumentoRepository revisaoRepository;
    private final ComprovanteRepository comprovanteRepository;
    private final EventoRepository eventoRepository;
    private final HistoricoAuditoriaRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final String senhaAlunoDemo;

    public BusinessController(
        BusinessAuthorizationService authz,
        AuditService auditService,
        DocumentoBusinessService documentoService,
        ImportacaoAlunoService importacaoAlunoService,
        StorageService storageService,
        PasswordEncoder passwordEncoder,
        InstituicaoRepository instituicaoRepository,
        TurmaRepository turmaRepository,
        AlunoRepository alunoRepository,
        ResponsavelRepository responsavelRepository,
        TipoDocumentoRepository tipoDocumentoRepository,
        RequisitoDocumentoTurmaRepository requisitoRepository,
        DocumentoAlunoRepository documentoRepository,
        RevisaoDocumentoRepository revisaoRepository,
        ComprovanteRepository comprovanteRepository,
        EventoRepository eventoRepository,
        HistoricoAuditoriaRepository historicoRepository,
        UsuarioRepository usuarioRepository,
        @Value("${app.demo.student-password:}") String senhaAlunoDemo
    ) {
        this.authz = authz;
        this.auditService = auditService;
        this.documentoService = documentoService;
        this.importacaoAlunoService = importacaoAlunoService;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
        this.instituicaoRepository = instituicaoRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.responsavelRepository = responsavelRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.requisitoRepository = requisitoRepository;
        this.documentoRepository = documentoRepository;
        this.revisaoRepository = revisaoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.eventoRepository = eventoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.senhaAlunoDemo = senhaAlunoDemo;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
        @RequestParam(required = false) Long instituicaoId,
        @RequestParam(required = false) Long turmaId
    ) {
        authz.exigirOperacionalOuComissao();
        Organizacao org = authz.organizacaoAtual();
        List<Turma> turmas = turmasFiltradas(org, instituicaoId, turmaId);
        List<Long> turmaIds = turmas.stream().map(Turma::getId).toList();
        List<Aluno> alunos = turmaIds.isEmpty() ? List.of() : alunoRepository.findByTurmaIdInOrderByNomeAsc(turmaIds);
        List<DocumentoAluno> documentos = documentoService.listarDocumentosPermitidos(turmaIds);
        List<Comprovante> comprovantes = turmaIds.isEmpty() ? List.of() : comprovanteRepository.findByTurmaIdInOrderByDataEnvioDesc(turmaIds);

        long cadastroIncompleto = alunos.stream().filter(a -> a.getStatusCadastro() != SituacaoCadastro.COMPLETO).count();
        long pendentes = documentos.stream().filter(d -> d.getStatus() == StatusDocumento.PENDENTE).count();
        pendentes += estimarPendenciasSemArquivo(turmas, alunos, documentos);
        long emAnalise = documentos.stream().filter(d -> d.getStatus() == StatusDocumento.EM_ANALISE).count();
        long reprovados = documentos.stream().filter(d -> d.getStatus() == StatusDocumento.REPROVADO).count();
        long comprovantesAnalise = comprovantes.stream().filter(c -> c.getStatus() == StatusComprovante.EM_ANALISE || c.getStatus() == StatusComprovante.ENVIADO).count();

        Map<String, Long> statusDocs = documentos.stream().collect(Collectors.groupingBy(
            d -> d.getStatus() == null ? "PENDENTE" : d.getStatus().name(),
            LinkedHashMap::new,
            Collectors.counting()
        ));
        statusDocs.putIfAbsent("PENDENTE", pendentes);

        return new DashboardResponse(
            List.of(
                new Metric("Turmas ativas", String.valueOf(turmas.stream().filter(t -> t.getStatusTurma() != StatusTurma.CONCLUIDA && t.getStatusTurma() != StatusTurma.CANCELADA).count()), "operacao em andamento", "blue"),
                new Metric("Instituicoes", String.valueOf(turmas.stream().map(Turma::getInstituicaoEntidade).filter(Objects::nonNull).map(Instituicao::getId).distinct().count()), "atendidas no filtro", "neutral"),
                new Metric("Alunos", String.valueOf(alunos.size()), "cadastros ativos", "green"),
                new Metric("Cadastros incompletos", String.valueOf(cadastroIncompleto), "precisam revisao", cadastroIncompleto > 0 ? "amber" : "green"),
                new Metric("Docs pendentes", String.valueOf(pendentes), "sem envio aprovado", pendentes > 0 ? "amber" : "green"),
                new Metric("Docs em analise", String.valueOf(emAnalise), "fila operacional", emAnalise > 0 ? "blue" : "neutral"),
                new Metric("Docs reprovados", String.valueOf(reprovados), "aguardam reenvio", reprovados > 0 ? "red" : "green"),
                new Metric("Comprovantes", String.valueOf(comprovantesAnalise), "aguardando analise", comprovantesAnalise > 0 ? "amber" : "neutral")
            ),
            turmas.stream().map(t -> toTurmaPendencia(t, alunos, documentos)).sorted(Comparator.comparing(TurmaPendencia::pendencias).reversed()).limit(5).toList(),
            statusDocs,
            historicoRepository.findTop20ByOrganizacaoIdOrderByDataHoraDesc(org.getId()).stream().map(this::toAtividade).toList(),
            eventoRepository.findByOrganizacaoIdOrderByDataEventoAscNomeAsc(org.getId()).stream()
                .filter(e -> e.getDataEvento() == null || !e.getDataEvento().isBefore(LocalDate.now()))
                .limit(6)
                .map(this::toEventoResumo)
                .toList(),
            List.of(
                new Atalho("Importar alunos", "importacao", "upload"),
                new Atalho("Analisar documentos", "documentos", "file-check"),
                new Atalho("Relatorios", "relatorios", "chart")
            )
        );
    }

    @GetMapping("/instituicoes")
    public List<InstituicaoResponse> listarInstituicoes(@RequestParam(required = false) String busca, @RequestParam(required = false) String status) {
        authz.exigirOperacionalOuComissao();
        Organizacao org = authz.organizacaoAtual();
        List<Instituicao> items = instituicaoRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId());
        String normalizedSearch = normalizeSearch(busca);
        String normalizedStatus = safe(status).toUpperCase(Locale.ROOT);
        return items.stream()
            .filter(item -> normalizedSearch.isBlank() || normalizeSearch(item.getNome() + " " + item.getCidade()).contains(normalizedSearch))
            .filter(item -> normalizedStatus.isBlank() || item.getStatus().name().equals(normalizedStatus))
            .map(this::toInstituicaoResponse)
            .toList();
    }

    @PostMapping("/instituicoes")
    @Transactional
    public InstituicaoResponse criarInstituicao(@RequestBody InstituicaoRequest request) {
        authz.exigirAdminOrganizacao();
        Instituicao instituicao = new Instituicao();
        aplicarInstituicao(instituicao, request, authz.organizacaoAtual());
        Instituicao salva = instituicaoRepository.save(instituicao);
        auditService.registrar(authz.organizacaoAtual(), authz.usuarioAtual(), "Instituicao", salva.getId(), "CRIACAO", "Instituicao criada: " + salva.getNome());
        return toInstituicaoResponse(salva);
    }

    @PutMapping("/instituicoes/{id}")
    @Transactional
    public InstituicaoResponse atualizarInstituicao(@PathVariable Long id, @RequestBody InstituicaoRequest request) {
        authz.exigirAdminOrganizacao();
        Organizacao org = authz.organizacaoAtual();
        Instituicao instituicao = instituicaoRepository.findByIdAndOrganizacaoId(id, org.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituicao nao encontrada."));
        aplicarInstituicao(instituicao, request, org);
        auditService.registrar(org, authz.usuarioAtual(), "Instituicao", id, "ALTERACAO", "Instituicao atualizada: " + instituicao.getNome());
        return toInstituicaoResponse(instituicaoRepository.save(instituicao));
    }

    @PatchMapping("/instituicoes/{id}/status")
    @Transactional
    public InstituicaoResponse alterarStatusInstituicao(@PathVariable Long id, @RequestParam String status) {
        authz.exigirAdminOrganizacao();
        Organizacao org = authz.organizacaoAtual();
        Instituicao instituicao = instituicaoRepository.findByIdAndOrganizacaoId(id, org.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituicao nao encontrada."));
        instituicao.setStatus(parseEnum(StatusRegistro.class, status, StatusRegistro.ATIVO));
        auditService.registrar(org, authz.usuarioAtual(), "Instituicao", id, "ALTERACAO_STATUS", "Status da instituicao alterado para " + instituicao.getStatus());
        return toInstituicaoResponse(instituicaoRepository.save(instituicao));
    }

    @GetMapping("/turmas")
    public List<TurmaResponse> listarTurmas(@RequestParam(required = false) Long instituicaoId, @RequestParam(required = false) String status) {
        authz.exigirOperacionalOuComissao();
        Organizacao org = authz.organizacaoAtual();
        String normalizedStatus = safe(status).toUpperCase(Locale.ROOT);
        return turmasFiltradas(org, instituicaoId, null).stream()
            .filter(t -> normalizedStatus.isBlank() || t.getStatusTurma().name().equals(normalizedStatus) || safe(t.getStatus()).equalsIgnoreCase(normalizedStatus))
            .map(this::toTurmaResponse)
            .toList();
    }

    @GetMapping("/turmas/{id}")
    public TurmaResponse obterTurma(@PathVariable Long id) {
        Turma turma = turmaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        return toTurmaResponse(turma);
    }

    @PostMapping("/turmas")
    @Transactional
    public TurmaResponse criarTurma(@RequestBody TurmaRequest request) {
        authz.exigirOperacional();
        Organizacao org = authz.organizacaoAtual();
        Turma turma = new Turma();
        aplicarTurma(turma, request, org);
        Turma salva = turmaRepository.save(turma);
        auditService.registrar(org, authz.usuarioAtual(), "Turma", salva.getId(), "CRIACAO", "Turma criada: " + salva.getNome());
        return toTurmaResponse(salva);
    }

    @PutMapping("/turmas/{id}")
    @Transactional
    public TurmaResponse atualizarTurma(@PathVariable Long id, @RequestBody TurmaRequest request) {
        authz.exigirOperacional();
        Organizacao org = authz.organizacaoAtual();
        Turma turma = turmaRepository.findByIdAndOrganizacaoId(id, org.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        aplicarTurma(turma, request, org);
        auditService.registrar(org, authz.usuarioAtual(), "Turma", id, "ALTERACAO", "Turma atualizada: " + turma.getNome());
        return toTurmaResponse(turmaRepository.save(turma));
    }

    @GetMapping("/alunos")
    public List<AlunoResponse> listarAlunos(@RequestParam(required = false) Long turmaId, @RequestParam(required = false) String busca) {
        authz.exigirOperacionalOuComissao();
        List<Turma> turmas = turmaId == null ? authz.turmasPermitidas() : List.of(turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada.")));
        turmas.forEach(authz::exigirAcessoTurma);
        List<Long> turmaIds = turmas.stream().map(Turma::getId).toList();
        String normalizedSearch = normalizeSearch(busca);
        List<Aluno> alunos = turmaIds.isEmpty() ? List.of() : alunoRepository.findByTurmaIdInOrderByNomeAsc(turmaIds);
        return alunos.stream()
            .filter(a -> normalizedSearch.isBlank() || normalizeSearch(a.getNome() + " " + a.getIdentificador() + " " + a.getEmail()).contains(normalizedSearch))
            .map(this::toAlunoResponse)
            .toList();
    }

    @GetMapping("/alunos/{id}")
    public AlunoResponse obterAluno(@PathVariable Long id) {
        Aluno aluno = alunoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado."));
        authz.exigirAcessoAluno(aluno);
        return toAlunoResponse(aluno);
    }

    @PostMapping("/alunos")
    @Transactional
    public AlunoResponse criarAluno(@RequestBody AlunoRequest request) {
        authz.exigirOperacional();
        Turma turma = turmaRepository.findById(request.turmaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        Aluno aluno = new Aluno();
        aplicarAluno(aluno, request, turma);
        Aluno salvo = alunoRepository.save(aluno);
        criarUsuarioAlunoSeNecessario(salvo, request.senha());
        auditService.registrar(authz.organizacaoAtual(), authz.usuarioAtual(), "Aluno", salvo.getId(), "CRIACAO", "Aluno criado: " + salvo.getNome());
        return toAlunoResponse(salvo);
    }

    @PutMapping("/alunos/{id}")
    @Transactional
    public AlunoResponse atualizarAluno(@PathVariable Long id, @RequestBody AlunoRequest request) {
        authz.exigirOperacional();
        Aluno aluno = alunoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado."));
        authz.exigirAcessoAluno(aluno);
        Turma turma = turmaRepository.findById(request.turmaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        aplicarAluno(aluno, request, turma);
        usuarioRepository.findByAlunoId(aluno.getId()).ifPresent(usuario -> {
            usuario.setNome(aluno.getNome());
            usuario.setEmail(firstNonBlank(aluno.getEmail(), usuario.getEmail()));
            if (!safe(request.senha()).isBlank()) {
                usuario.setSenha(passwordEncoder.encode(request.senha()));
                aluno.setPrecisaTrocarSenha(false);
            }
            usuarioRepository.save(usuario);
        });
        auditService.registrar(authz.organizacaoAtual(), authz.usuarioAtual(), "Aluno", id, "ALTERACAO", "Aluno atualizado: " + aluno.getNome());
        return toAlunoResponse(alunoRepository.save(aluno));
    }

    @GetMapping("/tipos-documentos")
    public List<TipoDocumentoResponse> listarTiposDocumento() {
        authz.exigirOperacionalOuComissao();
        return tipoDocumentoRepository.findByOrganizacaoIdOrderByNomeAsc(authz.organizacaoAtual().getId()).stream()
            .map(this::toTipoDocumentoResponse)
            .toList();
    }

    @PostMapping("/tipos-documentos")
    @Transactional
    public TipoDocumentoResponse salvarTipoDocumento(@RequestBody TipoDocumentoRequest request) {
        authz.exigirAdminOrganizacao();
        Organizacao org = authz.organizacaoAtual();
        TipoDocumento tipo = new TipoDocumento();
        aplicarTipoDocumento(tipo, request, org);
        TipoDocumento salvo = tipoDocumentoRepository.save(tipo);
        auditService.registrar(org, authz.usuarioAtual(), "TipoDocumento", salvo.getId(), "CRIACAO", "Tipo documental criado: " + salvo.getNome());
        return toTipoDocumentoResponse(salvo);
    }

    @GetMapping("/turmas/{turmaId}/documentos/requisitos")
    public List<ChecklistItem> listarRequisitosTurma(@PathVariable Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        return requisitoRepository.findByTurmaIdAndAtivoTrueOrderByTipoDocumentoNomeAsc(turmaId).stream()
            .map(req -> new ChecklistItem(req.getId(), req.getTipoDocumento().getId(), req.getTipoDocumento().getNome(), req.getTipoDocumento().getDescricao(), req.getObrigatorio(), "CONFIGURADO", null))
            .toList();
    }

    @PostMapping("/turmas/{turmaId}/documentos/requisitos")
    @Transactional
    public List<ChecklistItem> configurarRequisitoTurma(@PathVariable Long turmaId, @RequestBody RequisitoDocumentoRequest request) {
        authz.exigirOperacional();
        Turma turma = turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        TipoDocumento tipo = tipoDocumentoRepository.findByIdAndOrganizacaoId(request.tipoDocumentoId(), turma.getOrganizacao().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo documental nao encontrado."));
        RequisitoDocumentoTurma requisito = requisitoRepository.findByTurmaIdAndTipoDocumentoId(turmaId, tipo.getId()).orElseGet(RequisitoDocumentoTurma::new);
        requisito.setTurma(turma);
        requisito.setTipoDocumento(tipo);
        requisito.setObrigatorio(request.obrigatorio() == null || request.obrigatorio());
        requisito.setAtivo(request.ativo() == null || request.ativo());
        requisito.setStatus(Boolean.TRUE.equals(requisito.getAtivo()) ? StatusRegistro.ATIVO : StatusRegistro.INATIVO);
        requisitoRepository.save(requisito);
        auditService.registrar(turma.getOrganizacao(), authz.usuarioAtual(), "Turma", turmaId, "CONFIG_DOCUMENTOS", "Requisito documental configurado: " + tipo.getNome());
        return listarRequisitosTurma(turmaId);
    }

    @GetMapping("/alunos/{alunoId}/documentos")
    public List<ChecklistItem> checklistAluno(@PathVariable Long alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado."));
        authz.exigirAcessoAluno(aluno);
        return requisitoRepository.findByTurmaIdAndAtivoTrueOrderByTipoDocumentoNomeAsc(aluno.getTurma().getId()).stream()
            .map(req -> {
                DocumentoAluno atual = documentoRepository.findTopByAlunoIdAndTipoDocumentoIdOrderByVersaoDesc(alunoId, req.getTipoDocumento().getId()).orElse(null);
                String status = atual == null ? StatusDocumento.PENDENTE.name() : atual.getStatus().name();
                return new ChecklistItem(req.getId(), req.getTipoDocumento().getId(), req.getTipoDocumento().getNome(), req.getTipoDocumento().getDescricao(), req.getObrigatorio(), status, atual == null ? null : toDocumentoResponse(atual));
            })
            .toList();
    }

    @PostMapping("/alunos/{alunoId}/documentos/{tipoDocumentoId}/upload")
    @Transactional
    public DocumentoResponse uploadDocumento(
        @PathVariable Long alunoId,
        @PathVariable Long tipoDocumentoId,
        @RequestParam("arquivo") MultipartFile arquivo,
        @RequestParam(required = false) String observacao
    ) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado."));
        authz.exigirAcessoAluno(aluno);
        TipoDocumento tipo = tipoDocumentoRepository.findByIdAndOrganizacaoId(tipoDocumentoId, aluno.getTurma().getOrganizacao().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo documental nao encontrado."));
        DocumentoAluno documento = documentoService.enviarDocumento(aluno, tipo, arquivo, authz.usuarioAtual(), observacao);
        auditService.registrar(aluno.getTurma().getOrganizacao(), authz.usuarioAtual(), "DocumentoAluno", documento.getId(), "UPLOAD", "Documento enviado: " + tipo.getNome());
        return toDocumentoResponse(documento);
    }

    @PostMapping("/documentos/{documentoId}/analisar")
    @Transactional
    public DocumentoResponse analisarDocumento(@PathVariable Long documentoId, @RequestBody AnaliseDocumentoRequest request) {
        authz.exigirOperacional();
        DocumentoAluno documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento nao encontrado."));
        authz.exigirAcessoAluno(documento.getAluno());
        StatusDocumento status = documentoService.parseStatusDocumento(request.status());
        DocumentoAluno salvo = documentoService.analisarDocumento(documento, status, authz.usuarioAtual(), request.justificativa(), request.observacao());
        auditService.registrar(documento.getAluno().getTurma().getOrganizacao(), authz.usuarioAtual(), "DocumentoAluno", documentoId, status.name(), "Documento analisado: " + status.name());
        return toDocumentoResponse(salvo);
    }

    @GetMapping("/documentos")
    public List<DocumentoResponse> listarDocumentos(@RequestParam(required = false) Long turmaId, @RequestParam(required = false) String status) {
        authz.exigirOperacionalOuComissao();
        List<Turma> turmas = turmaId == null ? authz.turmasPermitidas() : List.of(turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada.")));
        turmas.forEach(authz::exigirAcessoTurma);
        String normalizedStatus = safe(status).toUpperCase(Locale.ROOT);
        return documentoService.listarDocumentosPermitidos(turmas.stream().map(Turma::getId).toList()).stream()
            .filter(d -> normalizedStatus.isBlank() || d.getStatus().name().equals(normalizedStatus))
            .map(this::toDocumentoResponse)
            .toList();
    }

    @GetMapping("/comprovantes")
    public List<ComprovanteResponse> listarComprovantes(@RequestParam(required = false) Long turmaId, @RequestParam(required = false) Long alunoId, @RequestParam(required = false) String status) {
        authz.exigirOperacionalOuComissao();
        List<Turma> turmas = turmaId == null ? authz.turmasPermitidas() : List.of(turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada.")));
        turmas.forEach(authz::exigirAcessoTurma);
        String normalizedStatus = safe(status).toUpperCase(Locale.ROOT);
        return comprovanteRepository.findByTurmaIdInOrderByDataEnvioDesc(turmas.stream().map(Turma::getId).toList()).stream()
            .filter(c -> alunoId == null || (c.getAluno() != null && alunoId.equals(c.getAluno().getId())))
            .filter(c -> normalizedStatus.isBlank() || c.getStatus().name().equals(normalizedStatus))
            .map(this::toComprovanteResponse)
            .toList();
    }

    @PostMapping("/alunos/{alunoId}/comprovantes/upload")
    @Transactional
    public ComprovanteResponse uploadComprovante(
        @PathVariable Long alunoId,
        @RequestParam("arquivo") MultipartFile arquivo,
        @RequestParam(required = false) String descricao
    ) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado."));
        authz.exigirAcessoAluno(aluno);
        var stored = storageService.store(arquivo, "comprovantes/turma-" + aluno.getTurma().getId(), List.of("pdf", "jpg", "jpeg", "png"), 5 * 1024 * 1024L);
        Comprovante comprovante = new Comprovante();
        comprovante.setOrganizacao(aluno.getTurma().getOrganizacao());
        comprovante.setTurma(aluno.getTurma());
        comprovante.setAluno(aluno);
        comprovante.setDescricao(firstNonBlank(descricao, "Comprovante enviado por " + aluno.getNome()));
        comprovante.setNomeOriginal(stored.originalName());
        comprovante.setNomeArmazenado(stored.storedName());
        comprovante.setReferenciaArquivo(stored.reference());
        comprovante.setTamanho(stored.size());
        comprovante.setMimeType(stored.mimeType());
        comprovante.setDataEnvio(LocalDateTime.now());
        comprovante.setUsuarioEnvio(authz.usuarioAtual());
        comprovante.setStatus(StatusComprovante.EM_ANALISE);
        Comprovante salvo = comprovanteRepository.save(comprovante);
        auditService.registrar(aluno.getTurma().getOrganizacao(), authz.usuarioAtual(), "Comprovante", salvo.getId(), "UPLOAD", "Comprovante enviado.");
        return toComprovanteResponse(salvo);
    }

    @PostMapping("/comprovantes/{id}/analisar")
    @Transactional
    public ComprovanteResponse analisarComprovante(@PathVariable Long id, @RequestBody AnaliseComprovanteRequest request) {
        authz.exigirOperacional();
        Comprovante comprovante = comprovanteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprovante nao encontrado."));
        authz.exigirAcessoTurma(comprovante.getTurma());
        StatusComprovante status = parseEnum(StatusComprovante.class, request.status(), StatusComprovante.EM_ANALISE);
        if (status != StatusComprovante.APROVADO && status != StatusComprovante.REPROVADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de analise invalido.");
        }
        if (status == StatusComprovante.REPROVADO && safe(request.comentario()).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comentario obrigatorio para reprovacao.");
        }
        comprovante.setStatus(status);
        comprovante.setComentario(safe(request.comentario()));
        comprovante.setDataAnalise(LocalDateTime.now());
        comprovante.setUsuarioAnalise(authz.usuarioAtual());
        auditService.registrar(comprovante.getOrganizacao(), authz.usuarioAtual(), "Comprovante", id, status.name(), "Comprovante analisado: " + status.name());
        return toComprovanteResponse(comprovanteRepository.save(comprovante));
    }

    @PostMapping("/importacoes/alunos/preview")
    public ImportPreview previewImportacao(@RequestParam("arquivo") MultipartFile arquivo, @RequestParam Long turmaId) {
        authz.exigirOperacional();
        Turma turma = turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        return importacaoAlunoService.preview(arquivo, turma);
    }

    @PostMapping("/importacoes/alunos/confirmar")
    @Transactional
    public ImportResult confirmarImportacao(
        @RequestParam("arquivo") MultipartFile arquivo,
        @RequestParam Long turmaId,
        @RequestParam(defaultValue = "IGNORAR_DUPLICADOS") String estrategia
    ) {
        authz.exigirOperacional();
        Turma turma = turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada."));
        authz.exigirAcessoTurma(turma);
        ImportResult result = importacaoAlunoService.confirmar(arquivo, turma, parseEnum(EstrategiaImportacao.class, estrategia, EstrategiaImportacao.IGNORAR_DUPLICADOS));
        auditService.registrar(turma.getOrganizacao(), authz.usuarioAtual(), "Aluno", turmaId, "IMPORTACAO", "Importacao de alunos: " + result.importados() + " novos, " + result.atualizados() + " atualizados.");
        return result;
    }

    @GetMapping("/relatorios/{tipo}")
    public RelatorioResponse relatorio(@PathVariable String tipo, @RequestParam(required = false) Long turmaId) {
        authz.exigirOperacionalOuComissao();
        List<Turma> turmas = turmaId == null ? authz.turmasPermitidas() : List.of(turmaRepository.findById(turmaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada.")));
        turmas.forEach(authz::exigirAcessoTurma);
        List<Long> turmaIds = turmas.stream().map(Turma::getId).toList();
        List<Aluno> alunos = turmaIds.isEmpty() ? List.of() : alunoRepository.findByTurmaIdInOrderByNomeAsc(turmaIds);
        List<Comprovante> comprovantes = turmaIds.isEmpty() ? List.of() : comprovanteRepository.findByTurmaIdInOrderByDataEnvioDesc(turmaIds);
        List<DocumentoAluno> documentos = documentoService.listarDocumentosPermitidos(turmaIds);
        String normalized = normalizeSearch(tipo).replace("_", "-");

        List<Map<String, String>> linhas = switch (normalized) {
            case "cadastro-incompleto" -> alunos.stream().filter(a -> a.getStatusCadastro() != SituacaoCadastro.COMPLETO).map(this::linhaAluno).toList();
            case "documentos-pendentes" -> alunos.stream().filter(a -> a.getStatusDocumental() == StatusDocumento.PENDENTE).map(this::linhaAluno).toList();
            case "documentos-analise" -> documentos.stream().filter(d -> d.getStatus() == StatusDocumento.EM_ANALISE).map(this::linhaDocumento).toList();
            case "documentos-reprovados" -> documentos.stream().filter(d -> d.getStatus() == StatusDocumento.REPROVADO).map(this::linhaDocumento).toList();
            case "beca" -> alunos.stream().map(this::linhaAluno).toList();
            case "situacao-escolar" -> alunos.stream().map(this::linhaAluno).toList();
            case "comprovantes-pendentes" -> comprovantes.stream().filter(c -> c.getStatus() == StatusComprovante.PENDENTE || c.getStatus() == StatusComprovante.EM_ANALISE).map(this::linhaComprovante).toList();
            case "resumo-turma" -> turmas.stream().map(this::linhaTurma).toList();
            default -> alunos.stream().map(this::linhaAluno).toList();
        };

        return new RelatorioResponse(
            normalized,
            List.of(
                new Metric("Turmas", String.valueOf(turmas.size()), "no filtro", "neutral"),
                new Metric("Alunos", String.valueOf(alunos.size()), "cadastros", "green"),
                new Metric("Documentos", String.valueOf(documentos.size()), "envios", "blue"),
                new Metric("Linhas", String.valueOf(linhas.size()), "resultado", "amber")
            ),
            linhas
        );
    }

    @GetMapping(value = "/relatorios/{tipo}/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarRelatorioCsv(@PathVariable String tipo, @RequestParam(required = false) Long turmaId) {
        RelatorioResponse relatorio = relatorio(tipo, turmaId);
        String csv = toCsv(relatorio.linhas());
        String filename = "formaly-business-" + normalizeSearch(tipo).replace("_", "-") + "-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private List<Turma> turmasFiltradas(Organizacao org, Long instituicaoId, Long turmaId) {
        List<Turma> permitted = authz.turmasPermitidas();
        return permitted.stream()
            .filter(t -> t.getOrganizacao() != null && org.getId().equals(t.getOrganizacao().getId()))
            .filter(t -> instituicaoId == null || (t.getInstituicaoEntidade() != null && instituicaoId.equals(t.getInstituicaoEntidade().getId())))
            .filter(t -> turmaId == null || turmaId.equals(t.getId()))
            .toList();
    }

    private void aplicarInstituicao(Instituicao instituicao, InstituicaoRequest request, Organizacao org) {
        instituicao.setOrganizacao(org);
        instituicao.setNome(requireText(request.nome(), "Nome da instituicao e obrigatorio."));
        instituicao.setNomeAbreviado(firstNonBlank(request.nomeAbreviado(), request.nome()));
        instituicao.setCidade(safe(request.cidade()));
        instituicao.setEstado(safe(request.estado()).toUpperCase(Locale.ROOT));
        instituicao.setContato(safe(request.contato()));
        instituicao.setObservacoes(safe(request.observacoes()));
        instituicao.setStatus(parseEnum(StatusRegistro.class, request.status(), StatusRegistro.ATIVO));
    }

    private void aplicarTurma(Turma turma, TurmaRequest request, Organizacao org) {
        Instituicao instituicao = instituicaoRepository.findByIdAndOrganizacaoId(request.instituicaoId(), org.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituicao nao encontrada."));
        turma.setOrganizacao(org);
        turma.setInstituicaoEntidade(instituicao);
        turma.setInstituicao(instituicao.getNome());
        turma.setNome(requireText(request.nome(), "Nome da turma e obrigatorio."));
        turma.setCurso(requireText(request.curso(), "Curso ou serie e obrigatorio."));
        turma.setAnoSemestre(firstNonBlank(request.anoSemestre(), "Nao informado"));
        turma.setDataPrevistaFormatura(request.dataPrevistaFormatura());
        turma.setResponsavelComercial(safe(request.responsavelComercial()));
        turma.setResponsavelOperacional(safe(request.responsavelOperacional()));
        turma.setRepresentante(safe(request.representante()));
        turma.setStatusTurma(parseEnum(StatusTurma.class, request.status(), StatusTurma.ATIVA));
        turma.setStatus(turma.getStatusTurma().name());
        turma.setAtivo(turma.getStatusTurma() != StatusTurma.CANCELADA);
    }

    private void aplicarAluno(Aluno aluno, AlunoRequest request, Turma turma) {
        String identificador = normalizarIdentificador(firstNonBlank(request.identificador(), gerarIdentificadorBase(request.nome())));
        if (identificador.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador do aluno invalido.");
        }
        alunoRepository.findFirstByTurmaIdAndIdentificadorIgnoreCase(turma.getId(), identificador).ifPresent(existente -> {
            if (aluno.getId() == null || !aluno.getId().equals(existente.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador ja utilizado nesta turma.");
            }
        });
        aluno.setNome(requireText(request.nome(), "Nome do aluno e obrigatorio."));
        aluno.setIdentificador(identificador);
        aluno.setCpf(safe(request.cpf()));
        aluno.setDataNascimento(request.dataNascimento());
        aluno.setEmail(safe(request.email()));
        aluno.setTelefone(safe(request.telefone()));
        aluno.setWhatsapp(safe(request.whatsapp()));
        aluno.setContato(firstNonBlank(request.email(), request.whatsapp(), request.telefone()));
        aluno.setTurma(turma);
        aluno.setSituacaoEscolar(parseEnum(SituacaoEscolar.class, request.situacaoEscolar(), SituacaoEscolar.REGULAR));
        aluno.setSituacaoContratual(parseEnum(SituacaoContratual.class, request.situacaoContratual(), SituacaoContratual.NAO_INFORMADA));
        aluno.setSituacaoBeca(parseEnum(SituacaoBeca.class, request.situacaoBeca(), SituacaoBeca.NAO_INFORMADA));
        aluno.setStatusDocumental(parseEnum(StatusDocumento.class, request.statusDocumental(), StatusDocumento.PENDENTE));
        aluno.setStatusCadastro(parseEnum(SituacaoCadastro.class, request.statusCadastro(), cadastroCompleto(request) ? SituacaoCadastro.COMPLETO : SituacaoCadastro.INCOMPLETO));
        aluno.setObservacaoInterna(safe(request.observacaoInterna()));
        aluno.setAtivo(true);
        aluno.setStatus("ATIVO");
        aluno.setDataInclusao(aluno.getDataInclusao() == null ? LocalDateTime.now() : aluno.getDataInclusao());
        aluno.setDataUltimaAtualizacao(LocalDateTime.now());

        if (request.responsavelId() != null) {
            Responsavel responsavel = responsavelRepository.findById(request.responsavelId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Responsavel nao encontrado."));
            aluno.setResponsavelLegal(responsavel);
        } else if (request.responsavel() != null && !safe(request.responsavel().nome()).isBlank()) {
            aluno.setResponsavelLegal(criarResponsavel(request.responsavel(), turma.getOrganizacao()));
        }
    }

    private Responsavel criarResponsavel(ResponsavelRequest request, Organizacao org) {
        Responsavel responsavel = new Responsavel();
        responsavel.setOrganizacao(org);
        responsavel.setNome(safe(request.nome()));
        responsavel.setParentesco(safe(request.parentesco()));
        responsavel.setCpf(safe(request.cpf()));
        responsavel.setEmail(safe(request.email()));
        responsavel.setTelefone(safe(request.telefone()));
        responsavel.setWhatsapp(safe(request.whatsapp()));
        responsavel.setContatoPrincipal(Boolean.TRUE.equals(request.contatoPrincipal()));
        responsavel.setObservacao(safe(request.observacao()));
        return responsavelRepository.save(responsavel);
    }

    private void criarUsuarioAlunoSeNecessario(Aluno aluno, String senhaInformada) {
        if (usuarioRepository.findByAlunoId(aluno.getId()).isPresent()) return;
        String senhaInicial = firstNonBlank(senhaInformada, senhaAlunoDemo);
        if (safe(senhaInicial).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma senha inicial para criar o usuario do aluno.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(aluno.getNome());
        usuario.setLogin(gerarLoginUnico(aluno.getIdentificador(), aluno.getTurma()));
        usuario.setEmail(firstNonBlank(aluno.getEmail(), usuario.getLogin() + "@formaly.demo"));
        usuario.setSenha(passwordEncoder.encode(senhaInicial));
        usuario.setPerfil(Perfil.ROLE_ALUNO);
        usuario.setAluno(aluno);
        usuario.setOrganizacaoAtual(aluno.getTurma().getOrganizacao());
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        aluno.setPrecisaTrocarSenha(safe(senhaInformada).isBlank());
    }

    private void aplicarTipoDocumento(TipoDocumento tipo, TipoDocumentoRequest request, Organizacao org) {
        tipo.setOrganizacao(org);
        tipo.setNome(requireText(request.nome(), "Nome do tipo documental e obrigatorio."));
        tipo.setDescricao(safe(request.descricao()));
        tipo.setObrigatorio(request.obrigatorio() == null || request.obrigatorio());
        tipo.setAplicavelMenorIdade(request.aplicavelMenorIdade() == null || request.aplicavelMenorIdade());
        tipo.setPermiteMultiplosArquivos(Boolean.TRUE.equals(request.permiteMultiplosArquivos()));
        tipo.setValidadeDias(request.validadeDias());
        tipo.setExtensoesPermitidas(firstNonBlank(request.extensoesPermitidas(), "pdf,jpg,jpeg,png"));
        tipo.setTamanhoMaximoBytes(request.tamanhoMaximoBytes() == null ? 5 * 1024 * 1024L : request.tamanhoMaximoBytes());
        tipo.setStatus(parseEnum(StatusRegistro.class, request.status(), StatusRegistro.ATIVO));
    }

    private InstituicaoResponse toInstituicaoResponse(Instituicao instituicao) {
        long turmas = turmaRepository.findByInstituicaoEntidadeIdOrderByNomeAsc(instituicao.getId()).size();
        return new InstituicaoResponse(instituicao.getId(), instituicao.getNome(), instituicao.getNomeAbreviado(), instituicao.getCidade(), instituicao.getEstado(), instituicao.getContato(), instituicao.getStatus().name(), instituicao.getObservacoes(), turmas);
    }

    private TurmaResponse toTurmaResponse(Turma turma) {
        int alunos = (int) alunoRepository.countByTurmaId(turma.getId());
        List<Aluno> alunosTurma = alunoRepository.findByTurmaIdOrderByNomeAsc(turma.getId());
        List<DocumentoAluno> documentos = documentoRepository.findByAlunoTurmaIdOrderByDataEnvioDesc(turma.getId());
        long pendencias = estimarPendenciasSemArquivo(List.of(turma), alunosTurma, documentos)
            + documentos.stream().filter(d -> d.getStatus() == StatusDocumento.REPROVADO || d.getStatus() == StatusDocumento.EM_ANALISE).count();
        return new TurmaResponse(
            turma.getId(),
            turma.getInstituicaoEntidade() != null ? turma.getInstituicaoEntidade().getId() : null,
            turma.getInstituicaoEntidade() != null ? turma.getInstituicaoEntidade().getNome() : turma.getInstituicao(),
            turma.getCurso(),
            turma.getNome(),
            turma.getAnoSemestre(),
            turma.getDataPrevistaFormatura(),
            turma.getResponsavelComercial(),
            turma.getResponsavelOperacional(),
            turma.getRepresentante(),
            alunos,
            percentualDocumentacao(alunosTurma, documentos),
            pendencias,
            turma.getStatusTurma() != null ? turma.getStatusTurma().name() : firstNonBlank(turma.getStatus(), "ATIVA")
        );
    }

    private AlunoResponse toAlunoResponse(Aluno aluno) {
        boolean esconderObservacao = authz.usuarioAtual().getPerfil() == Perfil.ROLE_COMISSAO || authz.usuarioAtual().getPerfil() == Perfil.ROLE_ALUNO;
        Instituicao instituicao = aluno.getTurma() != null ? aluno.getTurma().getInstituicaoEntidade() : null;
        return new AlunoResponse(
            aluno.getId(),
            aluno.getNome(),
            aluno.getIdentificador(),
            maskCpf(aluno.getCpf()),
            aluno.getDataNascimento(),
            aluno.getEmail(),
            aluno.getTelefone(),
            aluno.getWhatsapp(),
            aluno.getTurma() != null ? aluno.getTurma().getId() : null,
            aluno.getTurma() != null ? aluno.getTurma().getNome() : "",
            instituicao != null ? instituicao.getId() : null,
            instituicao != null ? instituicao.getNome() : "",
            aluno.getResponsavelLegal() == null ? null : toResponsavelResponse(aluno.getResponsavelLegal()),
            aluno.getSituacaoEscolar() == null ? "" : aluno.getSituacaoEscolar().name(),
            aluno.getSituacaoContratual() == null ? "" : aluno.getSituacaoContratual().name(),
            aluno.getSituacaoBeca() == null ? "" : aluno.getSituacaoBeca().name(),
            aluno.getStatusDocumental() == null ? "" : aluno.getStatusDocumental().name(),
            aluno.getStatusCadastro() == null ? "" : aluno.getStatusCadastro().name(),
            esconderObservacao ? "" : aluno.getObservacaoInterna(),
            aluno.getDataInclusao(),
            aluno.getDataUltimaAtualizacao()
        );
    }

    private ResponsavelResponse toResponsavelResponse(Responsavel responsavel) {
        return new ResponsavelResponse(responsavel.getId(), responsavel.getNome(), responsavel.getParentesco(), responsavel.getEmail(), responsavel.getTelefone(), responsavel.getWhatsapp(), responsavel.getContatoPrincipal());
    }

    private TipoDocumentoResponse toTipoDocumentoResponse(TipoDocumento tipo) {
        return new TipoDocumentoResponse(tipo.getId(), tipo.getNome(), tipo.getDescricao(), tipo.getObrigatorio(), tipo.getAplicavelMenorIdade(), tipo.getPermiteMultiplosArquivos(), tipo.getValidadeDias(), tipo.getExtensoesPermitidas(), tipo.getTamanhoMaximoBytes(), tipo.getStatus().name());
    }

    private DocumentoResponse toDocumentoResponse(DocumentoAluno documento) {
        List<RevisaoDocumentoResponse> historico = revisaoRepository.findByDocumentoIdOrderByDataRevisaoDesc(documento.getId()).stream()
            .map(r -> new RevisaoDocumentoResponse(r.getStatusAnterior() == null ? "" : r.getStatusAnterior().name(), r.getStatusNovo() == null ? "" : r.getStatusNovo().name(), r.getUsuario() == null ? "Sistema" : firstNonBlank(r.getUsuario().getNome(), r.getUsuario().getLogin()), r.getDataRevisao(), r.getJustificativa(), r.getObservacao()))
            .toList();
        return new DocumentoResponse(
            documento.getId(),
            documento.getAluno().getId(),
            documento.getAluno().getNome(),
            documento.getTipoDocumento().getId(),
            documento.getTipoDocumento().getNome(),
            documento.getNomeOriginal(),
            documento.getTamanho(),
            documento.getMimeType(),
            documento.getDataEnvio(),
            documento.getStatus() == null ? StatusDocumento.PENDENTE.name() : documento.getStatus().name(),
            documento.getDataAnalise(),
            documento.getJustificativa(),
            documento.getObservacao(),
            documento.getVersao(),
            historico
        );
    }

    private ComprovanteResponse toComprovanteResponse(Comprovante comprovante) {
        return new ComprovanteResponse(
            comprovante.getId(),
            comprovante.getTurma().getId(),
            comprovante.getTurma().getNome(),
            comprovante.getAluno() == null ? null : comprovante.getAluno().getId(),
            comprovante.getAluno() == null ? "" : comprovante.getAluno().getNome(),
            comprovante.getDescricao(),
            comprovante.getNomeOriginal(),
            comprovante.getTamanho(),
            comprovante.getMimeType(),
            comprovante.getDataEnvio(),
            comprovante.getStatus() == null ? StatusComprovante.PENDENTE.name() : comprovante.getStatus().name(),
            comprovante.getDataAnalise(),
            comprovante.getComentario()
        );
    }

    private AtividadeResponse toAtividade(HistoricoAuditoria historico) {
        return new AtividadeResponse(historico.getEntidade(), historico.getAcao(), historico.getResumo(), historico.getUsuario() == null ? "Sistema" : firstNonBlank(historico.getUsuario().getNome(), historico.getUsuario().getLogin()), historico.getDataHora());
    }

    private EventoResumo toEventoResumo(Evento evento) {
        return new EventoResumo(evento.getId(), evento.getNome(), evento.getTurma() == null ? "" : evento.getTurma().getNome(), evento.getDataEvento(), evento.getLocalEvento(), evento.getStatus());
    }

    private TurmaPendencia toTurmaPendencia(Turma turma, List<Aluno> alunos, List<DocumentoAluno> documentos) {
        List<Aluno> alunosTurma = alunos.stream().filter(a -> a.getTurma() != null && turma.getId().equals(a.getTurma().getId())).toList();
        List<DocumentoAluno> docsTurma = documentos.stream().filter(d -> d.getAluno().getTurma() != null && turma.getId().equals(d.getAluno().getTurma().getId())).toList();
        long pendencias = estimarPendenciasSemArquivo(List.of(turma), alunosTurma, docsTurma)
            + docsTurma.stream().filter(d -> d.getStatus() == StatusDocumento.REPROVADO || d.getStatus() == StatusDocumento.EM_ANALISE).count();
        return new TurmaPendencia(turma.getId(), turma.getNome(), turma.getInstituicaoEntidade() == null ? turma.getInstituicao() : turma.getInstituicaoEntidade().getNome(), pendencias, percentualDocumentacao(alunosTurma, docsTurma));
    }

    private long estimarPendenciasSemArquivo(List<Turma> turmas, List<Aluno> alunos, List<DocumentoAluno> documentos) {
        long total = 0;
        for (Turma turma : turmas) {
            List<RequisitoDocumentoTurma> requisitos = requisitoRepository.findByTurmaIdAndAtivoTrueOrderByTipoDocumentoNomeAsc(turma.getId()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getObrigatorio()))
                .toList();
            if (requisitos.isEmpty()) continue;
            List<Aluno> alunosTurma = alunos.stream().filter(a -> a.getTurma() != null && turma.getId().equals(a.getTurma().getId())).toList();
            for (Aluno aluno : alunosTurma) {
                for (RequisitoDocumentoTurma requisito : requisitos) {
                    boolean existe = documentos.stream().anyMatch(d -> d.getAluno().getId().equals(aluno.getId()) && d.getTipoDocumento().getId().equals(requisito.getTipoDocumento().getId()));
                    if (!existe) total++;
                }
            }
        }
        return total;
    }

    private double percentualDocumentacao(List<Aluno> alunos, List<DocumentoAluno> documentos) {
        if (alunos.isEmpty()) return 0.0;
        long aprovados = alunos.stream().filter(a -> a.getStatusDocumental() == StatusDocumento.APROVADO).count();
        if (aprovados > 0) return Math.round((aprovados * 10000.0) / alunos.size()) / 100.0;
        long docsAprovados = documentos.stream().filter(d -> d.getStatus() == StatusDocumento.APROVADO).count();
        long docsTotais = Math.max(documentos.size(), 1);
        return Math.round((docsAprovados * 10000.0) / docsTotais) / 100.0;
    }

    private Map<String, String> linhaAluno(Aluno aluno) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("aluno", aluno.getNome());
        row.put("identificador", aluno.getIdentificador());
        row.put("turma", aluno.getTurma() == null ? "" : aluno.getTurma().getNome());
        row.put("instituicao", aluno.getTurma() == null || aluno.getTurma().getInstituicaoEntidade() == null ? "" : aluno.getTurma().getInstituicaoEntidade().getNome());
        row.put("cadastro", aluno.getStatusCadastro() == null ? "" : aluno.getStatusCadastro().name());
        row.put("documentos", aluno.getStatusDocumental() == null ? "" : aluno.getStatusDocumental().name());
        row.put("beca", aluno.getSituacaoBeca() == null ? "" : aluno.getSituacaoBeca().name());
        row.put("situacao_escolar", aluno.getSituacaoEscolar() == null ? "" : aluno.getSituacaoEscolar().name());
        return row;
    }

    private Map<String, String> linhaDocumento(DocumentoAluno documento) {
        Map<String, String> row = linhaAluno(documento.getAluno());
        row.put("documento", documento.getTipoDocumento().getNome());
        row.put("status_documento", documento.getStatus() == null ? "" : documento.getStatus().name());
        row.put("versao", String.valueOf(documento.getVersao()));
        row.put("justificativa", safe(documento.getJustificativa()));
        return row;
    }

    private Map<String, String> linhaComprovante(Comprovante comprovante) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("turma", comprovante.getTurma() == null ? "" : comprovante.getTurma().getNome());
        row.put("aluno", comprovante.getAluno() == null ? "" : comprovante.getAluno().getNome());
        row.put("descricao", safe(comprovante.getDescricao()));
        row.put("status", comprovante.getStatus() == null ? "" : comprovante.getStatus().name());
        row.put("comentario", safe(comprovante.getComentario()));
        return row;
    }

    private Map<String, String> linhaTurma(Turma turma) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("turma", turma.getNome());
        row.put("instituicao", turma.getInstituicaoEntidade() == null ? turma.getInstituicao() : turma.getInstituicaoEntidade().getNome());
        row.put("curso", turma.getCurso());
        row.put("ano_semestre", turma.getAnoSemestre());
        row.put("status", turma.getStatusTurma() == null ? turma.getStatus() : turma.getStatusTurma().name());
        row.put("alunos", String.valueOf(alunoRepository.countByTurmaId(turma.getId())));
        row.put("data_formatura", turma.getDataPrevistaFormatura() == null ? "" : turma.getDataPrevistaFormatura().toString());
        return row;
    }

    private String toCsv(List<Map<String, String>> rows) {
        if (rows.isEmpty()) return "";
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        StringBuilder csv = new StringBuilder(String.join(";", headers)).append('\n');
        for (Map<String, String> row : rows) {
            csv.append(headers.stream().map(header -> csvEscape(row.get(header))).collect(Collectors.joining(";"))).append('\n');
        }
        return csv.toString();
    }

    private String csvEscape(String value) {
        return "\"" + safe(value).replace("\"", "\"\"") + "\"";
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

    private String maskCpf(String cpf) {
        String digits = safe(cpf).replaceAll("\\D", "");
        if (digits.length() < 4) return "";
        return "***." + digits.substring(Math.max(0, digits.length() - 6), Math.max(0, digits.length() - 3)) + ".***-" + digits.substring(digits.length() - 2);
    }

    private boolean cadastroCompleto(AlunoRequest request) {
        return !safe(request.nome()).isBlank() && !firstNonBlank(request.email(), request.whatsapp(), request.telefone()).isBlank();
    }

    private String requireText(String value, String message) {
        String normalized = safe(value);
        if (normalized.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return normalized;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, E fallback) {
        String normalized = safe(value).toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) return fallback;
        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opcao invalida: " + value);
        }
    }

    private String normalizeSearch(String value) {
        return Normalizer.normalize(safe(value), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
    }

    private String gerarIdentificadorBase(String nome) {
        String[] partes = normalizeSearch(nome).trim().split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) return "";
        return normalizarIdentificador(partes.length == 1 ? partes[0] : partes[0] + "." + partes[partes.length - 1]);
    }

    private String normalizarIdentificador(String valor) {
        return normalizeSearch(valor)
            .replaceAll("[^a-z0-9._-]", ".")
            .replaceAll("\\.{2,}", ".")
            .replaceAll("^[._-]+|[._-]+$", "");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
