package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.DocumentoAluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RequisitoDocumentoTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RevisaoDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.TipoDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.AlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.DocumentoAlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RequisitoDocumentoTurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RevisaoDocumentoRepository;

@Service
public class DocumentoBusinessService {
    private final DocumentoAlunoRepository documentoRepository;
    private final RevisaoDocumentoRepository revisaoRepository;
    private final RequisitoDocumentoTurmaRepository requisitoRepository;
    private final AlunoRepository alunoRepository;
    private final StorageService storageService;

    public DocumentoBusinessService(
        DocumentoAlunoRepository documentoRepository,
        RevisaoDocumentoRepository revisaoRepository,
        RequisitoDocumentoTurmaRepository requisitoRepository,
        AlunoRepository alunoRepository,
        StorageService storageService
    ) {
        this.documentoRepository = documentoRepository;
        this.revisaoRepository = revisaoRepository;
        this.requisitoRepository = requisitoRepository;
        this.alunoRepository = alunoRepository;
        this.storageService = storageService;
    }

    @Transactional
    public DocumentoAluno enviarDocumento(Aluno aluno, TipoDocumento tipo, MultipartFile file, Usuario usuario, String observacao) {
        long maxBytes = tipo.getTamanhoMaximoBytes() == null ? 5 * 1024 * 1024L : tipo.getTamanhoMaximoBytes();
        List<String> extensoes = Arrays.stream(safe(tipo.getExtensoesPermitidas()).split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
        var stored = storageService.store(file, "documentos/aluno-" + aluno.getId(), extensoes, maxBytes);
        int proximaVersao = documentoRepository
            .findTopByAlunoIdAndTipoDocumentoIdOrderByVersaoDesc(aluno.getId(), tipo.getId())
            .map(documento -> documento.getVersao() == null ? 2 : documento.getVersao() + 1)
            .orElse(1);

        DocumentoAluno documento = new DocumentoAluno();
        documento.setAluno(aluno);
        documento.setTipoDocumento(tipo);
        documento.setNomeOriginal(stored.originalName());
        documento.setNomeArmazenado(stored.storedName());
        documento.setReferenciaArquivo(stored.reference());
        documento.setTamanho(stored.size());
        documento.setMimeType(stored.mimeType());
        documento.setDataEnvio(LocalDateTime.now());
        documento.setUsuarioEnvio(usuario);
        documento.setStatus(StatusDocumento.EM_ANALISE);
        documento.setObservacao(safe(observacao));
        documento.setVersao(proximaVersao);
        DocumentoAluno salvo = documentoRepository.save(documento);
        registrarRevisao(salvo, null, StatusDocumento.EM_ANALISE, usuario, "", "Documento enviado para analise.");
        atualizarStatusDocumental(aluno);
        return salvo;
    }

    @Transactional
    public DocumentoAluno analisarDocumento(DocumentoAluno documento, StatusDocumento novoStatus, Usuario usuario, String justificativa, String observacao) {
        if (novoStatus != StatusDocumento.APROVADO && novoStatus != StatusDocumento.REPROVADO && novoStatus != StatusDocumento.EXPIRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de analise invalido.");
        }
        if (novoStatus == StatusDocumento.REPROVADO && safe(justificativa).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Justificativa obrigatoria para reprovacao.");
        }
        StatusDocumento anterior = documento.getStatus();
        documento.setStatus(novoStatus);
        documento.setUsuarioAnalise(usuario);
        documento.setDataAnalise(LocalDateTime.now());
        documento.setJustificativa(safe(justificativa));
        documento.setObservacao(safe(observacao));
        DocumentoAluno salvo = documentoRepository.save(documento);
        registrarRevisao(salvo, anterior, novoStatus, usuario, justificativa, observacao);
        atualizarStatusDocumental(documento.getAluno());
        return salvo;
    }

    public List<DocumentoAluno> listarDocumentosPermitidos(List<Long> turmaIds) {
        return turmaIds.isEmpty() ? List.of() : documentoRepository.findByAlunoTurmaIdInOrderByDataEnvioDesc(turmaIds);
    }

    private void atualizarStatusDocumental(Aluno aluno) {
        if (aluno == null || aluno.getTurma() == null) return;
        List<RequisitoDocumentoTurma> requisitos = requisitoRepository.findByTurmaIdAndAtivoTrueOrderByTipoDocumentoNomeAsc(aluno.getTurma().getId());
        if (requisitos.isEmpty()) {
            aluno.setStatusDocumental(StatusDocumento.PENDENTE);
            alunoRepository.save(aluno);
            return;
        }

        boolean temReprovado = false;
        boolean temAnalise = false;
        boolean todosObrigatoriosAprovados = true;
        for (RequisitoDocumentoTurma requisito : requisitos) {
            if (!Boolean.TRUE.equals(requisito.getObrigatorio())) continue;
            var atual = documentoRepository.findTopByAlunoIdAndTipoDocumentoIdOrderByVersaoDesc(
                aluno.getId(),
                requisito.getTipoDocumento().getId()
            );
            StatusDocumento status = atual.map(DocumentoAluno::getStatus).orElse(StatusDocumento.PENDENTE);
            if (status == StatusDocumento.REPROVADO) temReprovado = true;
            if (status == StatusDocumento.EM_ANALISE || status == StatusDocumento.ENVIADO) temAnalise = true;
            if (status != StatusDocumento.APROVADO) todosObrigatoriosAprovados = false;
        }

        if (temReprovado) aluno.setStatusDocumental(StatusDocumento.REPROVADO);
        else if (temAnalise) aluno.setStatusDocumental(StatusDocumento.EM_ANALISE);
        else if (todosObrigatoriosAprovados) aluno.setStatusDocumental(StatusDocumento.APROVADO);
        else aluno.setStatusDocumental(StatusDocumento.PENDENTE);
        aluno.setDataUltimaAtualizacao(LocalDateTime.now());
        alunoRepository.save(aluno);
    }

    private void registrarRevisao(
        DocumentoAluno documento,
        StatusDocumento anterior,
        StatusDocumento novo,
        Usuario usuario,
        String justificativa,
        String observacao
    ) {
        RevisaoDocumento revisao = new RevisaoDocumento();
        revisao.setDocumento(documento);
        revisao.setStatusAnterior(anterior);
        revisao.setStatusNovo(novo);
        revisao.setUsuario(usuario);
        revisao.setDataRevisao(LocalDateTime.now());
        revisao.setJustificativa(safe(justificativa));
        revisao.setObservacao(safe(observacao));
        revisaoRepository.save(revisao);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public StatusDocumento parseStatusDocumento(String status) {
        try {
            return StatusDocumento.valueOf(safe(status).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status documental invalido.");
        }
    }
}
