package br.com.senac.formatura.sistema_gerenciamento_formaturas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class BusinessDtos {
    private BusinessDtos() {}

    public record InstituicaoRequest(
        String nome,
        String nomeAbreviado,
        String cidade,
        String estado,
        String contato,
        String status,
        String observacoes
    ) {}

    public record InstituicaoResponse(
        Long id,
        String nome,
        String nomeAbreviado,
        String cidade,
        String estado,
        String contato,
        String status,
        String observacoes,
        long turmas
    ) {}

    public record TurmaRequest(
        Long instituicaoId,
        String curso,
        String nome,
        String anoSemestre,
        LocalDate dataPrevistaFormatura,
        String responsavelComercial,
        String responsavelOperacional,
        String representante,
        String status
    ) {}

    public record TurmaResponse(
        Long id,
        Long instituicaoId,
        String instituicao,
        String curso,
        String nome,
        String anoSemestre,
        LocalDate dataPrevistaFormatura,
        String responsavelComercial,
        String responsavelOperacional,
        String representante,
        int quantidadeAlunos,
        double percentualDocumentacao,
        long pendencias,
        String status
    ) {}

    public record ResponsavelRequest(
        String nome,
        String parentesco,
        String cpf,
        String email,
        String telefone,
        String whatsapp,
        String observacao,
        Boolean contatoPrincipal
    ) {}

    public record ResponsavelResponse(
        Long id,
        String nome,
        String parentesco,
        String email,
        String telefone,
        String whatsapp,
        Boolean contatoPrincipal
    ) {}

    public record AlunoRequest(
        String nome,
        String identificador,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String whatsapp,
        Long turmaId,
        Long responsavelId,
        ResponsavelRequest responsavel,
        String situacaoEscolar,
        String situacaoContratual,
        String situacaoBeca,
        String statusDocumental,
        String statusCadastro,
        String observacaoInterna,
        String senha
    ) {}

    public record AlunoResponse(
        Long id,
        String nome,
        String identificador,
        String cpfMascarado,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String whatsapp,
        Long turmaId,
        String turma,
        Long instituicaoId,
        String instituicao,
        ResponsavelResponse responsavel,
        String situacaoEscolar,
        String situacaoContratual,
        String situacaoBeca,
        String statusDocumental,
        String statusCadastro,
        String observacaoInterna,
        LocalDateTime dataInclusao,
        LocalDateTime dataUltimaAtualizacao
    ) {}

    public record TipoDocumentoRequest(
        String nome,
        String descricao,
        Boolean obrigatorio,
        Boolean aplicavelMenorIdade,
        Boolean permiteMultiplosArquivos,
        Integer validadeDias,
        String extensoesPermitidas,
        Long tamanhoMaximoBytes,
        String status
    ) {}

    public record TipoDocumentoResponse(
        Long id,
        String nome,
        String descricao,
        Boolean obrigatorio,
        Boolean aplicavelMenorIdade,
        Boolean permiteMultiplosArquivos,
        Integer validadeDias,
        String extensoesPermitidas,
        Long tamanhoMaximoBytes,
        String status
    ) {}

    public record RequisitoDocumentoRequest(Long tipoDocumentoId, Boolean obrigatorio, Boolean ativo) {}

    public record ChecklistItem(
        Long requisitoId,
        Long tipoDocumentoId,
        String tipoDocumento,
        String descricao,
        Boolean obrigatorio,
        String status,
        DocumentoResponse documentoAtual
    ) {}

    public record DocumentoResponse(
        Long id,
        Long alunoId,
        String aluno,
        Long tipoDocumentoId,
        String tipoDocumento,
        String nomeOriginal,
        Long tamanho,
        String mimeType,
        LocalDateTime dataEnvio,
        String status,
        LocalDateTime dataAnalise,
        String justificativa,
        String observacao,
        Integer versao,
        List<RevisaoDocumentoResponse> historico
    ) {}

    public record RevisaoDocumentoResponse(
        String statusAnterior,
        String statusNovo,
        String usuario,
        LocalDateTime dataRevisao,
        String justificativa,
        String observacao
    ) {}

    public record AnaliseDocumentoRequest(String status, String justificativa, String observacao) {}

    public record ComprovanteResponse(
        Long id,
        Long turmaId,
        String turma,
        Long alunoId,
        String aluno,
        String descricao,
        String nomeOriginal,
        Long tamanho,
        String mimeType,
        LocalDateTime dataEnvio,
        String status,
        LocalDateTime dataAnalise,
        String comentario
    ) {}

    public record AnaliseComprovanteRequest(String status, String comentario) {}

    public record DashboardResponse(
        List<Metric> metrics,
        List<TurmaPendencia> turmasComPendencias,
        Map<String, Long> statusDocumentais,
        List<AtividadeResponse> ultimasAtividades,
        List<EventoResumo> proximosEventos,
        List<Atalho> atalhos
    ) {}

    public record Metric(String label, String value, String hint, String tone) {}
    public record TurmaPendencia(Long turmaId, String turma, String instituicao, long pendencias, double documentacaoConcluida) {}
    public record AtividadeResponse(String entidade, String acao, String resumo, String usuario, LocalDateTime dataHora) {}
    public record EventoResumo(Long id, String nome, String turma, LocalDate dataEvento, String local, String status) {}
    public record Atalho(String label, String screen, String icon) {}

    public record ImportPreview(
        String fileName,
        List<String> headers,
        List<Map<String, String>> sampleRows,
        Map<String, String> suggestedMapping,
        List<ImportIssue> issues,
        long validos,
        long incompletos,
        long duplicados
    ) {}

    public record ImportIssue(int linha, String coluna, String mensagem, String severidade) {}

    public record ImportResult(
        long importados,
        long atualizados,
        long ignorados,
        long erros,
        List<ImportIssue> issues
    ) {}

    public record RelatorioResponse(
        String tipo,
        List<Metric> resumo,
        List<Map<String, String>> linhas
    ) {}
}
