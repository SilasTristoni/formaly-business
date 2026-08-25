package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Responsavel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    private String nome;
    private String parentesco;
    private String cpf;
    private String email;
    private String telefone;
    private String whatsapp;
    private Boolean contatoPrincipal = false;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}
