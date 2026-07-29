package br.com.senac.formatura.sistema_gerenciamento_formaturas.model;

public enum Perfil {
    ROLE_ADMIN_ORGANIZACAO,
    ROLE_COLABORADOR,
    ROLE_COMISSAO,
    ROLE_ALUNO;

    public boolean isOperacional() {
        return this == ROLE_ADMIN_ORGANIZACAO || this == ROLE_COLABORADOR;
    }

    public boolean isOrganizacaoAdmin() {
        return this == ROLE_ADMIN_ORGANIZACAO;
    }
}
