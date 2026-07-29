package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class DocumentoAluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    private String nomeOriginal;
    private String nomeArmazenado;
    private String referenciaArquivo;
    private Long tamanho;
    private String mimeType;
    private LocalDateTime dataEnvio;

    @ManyToOne
    @JoinColumn(name = "usuario_envio_id")
    private Usuario usuarioEnvio;

    @Enumerated(EnumType.STRING)
    private StatusDocumento status = StatusDocumento.EM_ANALISE;

    private LocalDateTime dataAnalise;

    @ManyToOne
    @JoinColumn(name = "usuario_analise_id")
    private Usuario usuarioAnalise;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    private Integer versao = 1;
}
