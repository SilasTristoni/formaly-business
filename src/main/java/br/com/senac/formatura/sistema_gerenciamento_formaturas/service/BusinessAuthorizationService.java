package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Turma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.UsuarioOrganizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioOrganizacaoRepository;

@Service
public class BusinessAuthorizationService {
    private final UsuarioOrganizacaoRepository usuarioOrganizacaoRepository;
    private final TurmaRepository turmaRepository;

    public BusinessAuthorizationService(
        UsuarioOrganizacaoRepository usuarioOrganizacaoRepository,
        TurmaRepository turmaRepository
    ) {
        this.usuarioOrganizacaoRepository = usuarioOrganizacaoRepository;
        this.turmaRepository = turmaRepository;
    }

    public Usuario usuarioAtual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao expirada.");
        }
        return usuario;
    }

    public Organizacao organizacaoAtual() {
        Usuario usuario = usuarioAtual();
        if (usuario.getOrganizacaoAtual() != null) {
            return usuario.getOrganizacaoAtual();
        }
        if (usuario.getAluno() != null
            && usuario.getAluno().getTurma() != null
            && usuario.getAluno().getTurma().getOrganizacao() != null) {
            return usuario.getAluno().getTurma().getOrganizacao();
        }
        return usuarioOrganizacaoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId()).stream()
            .map(UsuarioOrganizacao::getOrganizacao)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sem organizacao ativa."));
    }

    public void exigirAdminOrganizacao() {
        if (usuarioAtual().getPerfil() != Perfil.ROLE_ADMIN_ORGANIZACAO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Recurso restrito ao administrador da organizacao.");
        }
    }

    public void exigirOperacional() {
        Perfil perfil = usuarioAtual().getPerfil();
        if (perfil != Perfil.ROLE_ADMIN_ORGANIZACAO && perfil != Perfil.ROLE_COLABORADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Recurso restrito ao time operacional.");
        }
    }

    public void exigirOperacionalOuComissao() {
        Perfil perfil = usuarioAtual().getPerfil();
        if (perfil != Perfil.ROLE_ADMIN_ORGANIZACAO && perfil != Perfil.ROLE_COLABORADOR && perfil != Perfil.ROLE_COMISSAO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado para este perfil.");
        }
    }

    public List<Turma> turmasPermitidas() {
        Usuario usuario = usuarioAtual();
        Organizacao organizacao = organizacaoAtual();
        if (usuario.getPerfil() == Perfil.ROLE_ADMIN_ORGANIZACAO) {
            return turmaRepository.findByOrganizacaoIdOrderByNomeAsc(organizacao.getId());
        }
        if (usuario.getPerfil() == Perfil.ROLE_ALUNO) {
            Turma turma = usuario.getAluno() != null ? usuario.getAluno().getTurma() : null;
            return turma == null ? List.of() : List.of(turma);
        }

        List<UsuarioOrganizacao> vinculos = usuarioOrganizacaoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId());
        boolean acessoOrganizacaoInteira = vinculos.stream()
            .filter(v -> v.getOrganizacao() != null && organizacao.getId().equals(v.getOrganizacao().getId()))
            .anyMatch(v -> v.getTurmaPermitida() == null);
        if (acessoOrganizacaoInteira && usuario.getPerfil() == Perfil.ROLE_COLABORADOR) {
            return turmaRepository.findByOrganizacaoIdOrderByNomeAsc(organizacao.getId());
        }
        return vinculos.stream()
            .filter(v -> v.getOrganizacao() != null && organizacao.getId().equals(v.getOrganizacao().getId()))
            .map(UsuarioOrganizacao::getTurmaPermitida)
            .filter(Objects::nonNull)
            .toList();
    }

    public void exigirAcessoTurma(Turma turma) {
        if (turma == null || turma.getOrganizacao() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma nao encontrada.");
        }
        Organizacao organizacao = organizacaoAtual();
        if (!organizacao.getId().equals(turma.getOrganizacao().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Turma fora da organizacao atual.");
        }
        boolean permitido = turmasPermitidas().stream()
            .anyMatch(item -> item.getId() != null && item.getId().equals(turma.getId()));
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sem acesso a esta turma.");
        }
    }

    public void exigirAcessoAluno(Aluno aluno) {
        if (aluno == null || aluno.getTurma() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno nao encontrado.");
        }
        Usuario usuario = usuarioAtual();
        if (usuario.getPerfil() == Perfil.ROLE_ALUNO) {
            if (usuario.getAluno() == null || !usuario.getAluno().getId().equals(aluno.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno sem acesso a este cadastro.");
            }
            return;
        }
        exigirAcessoTurma(aluno.getTurma());
    }
}
