package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

import java.time.LocalDate;
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
import lombok.ToString;

@Data
@ToString(exclude = "turma")
@Entity
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(length = 80)
    private String identificador;

    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String whatsapp;

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Responsavel responsavelLegal;

    private String contato;
    private String status = "ATIVO";

    @Enumerated(EnumType.STRING)
    private SituacaoEscolar situacaoEscolar = SituacaoEscolar.REGULAR;

    @Enumerated(EnumType.STRING)
    private SituacaoContratual situacaoContratual = SituacaoContratual.NAO_INFORMADA;

    @Enumerated(EnumType.STRING)
    private SituacaoBeca situacaoBeca = SituacaoBeca.NAO_INFORMADA;

    @Enumerated(EnumType.STRING)
    private StatusDocumento statusDocumental = StatusDocumento.PENDENTE;

    @Enumerated(EnumType.STRING)
    private SituacaoCadastro statusCadastro = SituacaoCadastro.INCOMPLETO;

    private String observacaoInterna;
    private Boolean precisaTrocarSenha = false;
    private Boolean ativo = true;
    private LocalDateTime dataInclusao;
    private LocalDateTime dataUltimaAtualizacao;

    public String getNomeTurma() {
        return turma != null ? turma.getNome() : "";
    }

    public String getEmailExibicao() {
        return email != null && !email.isBlank() ? email : contato;
    }

    public String getWhatsappExibicao() {
        return whatsapp != null && !whatsapp.isBlank() ? whatsapp : contato;
    }

    public Long getOrganizacaoId() {
        return turma != null && turma.getOrganizacao() != null ? turma.getOrganizacao().getId() : null;
    }
}
