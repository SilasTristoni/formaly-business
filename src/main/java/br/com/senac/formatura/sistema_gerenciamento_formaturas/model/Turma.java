package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"alunos", "eventos", "lancamentos"})
@Entity
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organizacao_id")
    private Organizacao organizacao;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicaoEntidade;

    private String nome;
    private String curso;
    private String instituicao;
    private String anoSemestre;
    private String representante;
    private String responsavelComercial;
    private String responsavelOperacional;
    private LocalDate dataPrevistaFormatura;

    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Aluno> alunos;

    @OneToMany(mappedBy = "turma")
    @JsonIgnore
    private List<Evento> eventos;

    @OneToMany(mappedBy = "turma")
    @JsonIgnore
    private List<LancamentoFinanceiro> lancamentos;

    @Column(precision = 14, scale = 2)
    private java.math.BigDecimal totalArrecadado = java.math.BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private java.math.BigDecimal metaArrecadacao = java.math.BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private StatusTurma statusTurma = StatusTurma.ATIVA;

    private Boolean ativo = true;
    private String status = "ATIVA";

    public Integer getQuantidadeAlunos() {
        return alunos != null ? alunos.size() : 0;
    }

    public Double getTotalArrecadado() {
        return totalArrecadado == null ? 0.0 : totalArrecadado.doubleValue();
    }

    public void setTotalArrecadado(Double totalArrecadado) {
        this.totalArrecadado = java.math.BigDecimal.valueOf(totalArrecadado == null ? 0.0 : totalArrecadado);
    }

    public java.math.BigDecimal getTotalArrecadadoDecimal() {
        return totalArrecadado == null ? java.math.BigDecimal.ZERO : totalArrecadado;
    }

    public void setTotalArrecadadoDecimal(java.math.BigDecimal totalArrecadado) {
        this.totalArrecadado = totalArrecadado == null ? java.math.BigDecimal.ZERO : totalArrecadado;
    }

    public Double getMetaArrecadacao() {
        return metaArrecadacao == null ? 0.0 : metaArrecadacao.doubleValue();
    }

    public void setMetaArrecadacao(Double metaArrecadacao) {
        this.metaArrecadacao = java.math.BigDecimal.valueOf(metaArrecadacao == null ? 0.0 : metaArrecadacao);
    }

    public java.math.BigDecimal getMetaArrecadacaoDecimal() {
        return metaArrecadacao == null ? java.math.BigDecimal.ZERO : metaArrecadacao;
    }

    public void setMetaArrecadacaoDecimal(java.math.BigDecimal metaArrecadacao) {
        this.metaArrecadacao = metaArrecadacao == null ? java.math.BigDecimal.ZERO : metaArrecadacao;
    }
}
