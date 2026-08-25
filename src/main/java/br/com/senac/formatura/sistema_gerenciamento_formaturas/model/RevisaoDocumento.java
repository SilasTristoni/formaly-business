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
public class RevisaoDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoAluno documento;

    @Enumerated(EnumType.STRING)
    private StatusDocumento statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusDocumento statusNovo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataRevisao;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}
