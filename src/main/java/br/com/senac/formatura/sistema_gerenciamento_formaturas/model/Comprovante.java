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
public class Comprovante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    private String descricao;
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
    private StatusComprovante status = StatusComprovante.EM_ANALISE;

    private LocalDateTime dataAnalise;

    @ManyToOne
    @JoinColumn(name = "usuario_analise_id")
    private Usuario usuarioAnalise;

    @Column(columnDefinition = "TEXT")
    private String comentario;
}
