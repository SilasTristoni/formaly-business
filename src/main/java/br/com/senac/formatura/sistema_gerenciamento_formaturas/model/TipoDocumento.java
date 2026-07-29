package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

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
public class TipoDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private Boolean obrigatorio = true;
    private Boolean aplicavelMenorIdade = true;
    private Boolean permiteMultiplosArquivos = false;
    private Integer validadeDias;
    private String extensoesPermitidas = "pdf,jpg,jpeg,png";
    private Long tamanhoMaximoBytes = 5242880L;

    @Enumerated(EnumType.STRING)
    private StatusRegistro status = StatusRegistro.ATIVO;
}
