package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.UsuarioOrganizacao;

public interface UsuarioOrganizacaoRepository extends JpaRepository<UsuarioOrganizacao, Long> {
    List<UsuarioOrganizacao> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    List<UsuarioOrganizacao> findByOrganizacaoIdAndAtivoTrue(Long organizacaoId);
    Optional<UsuarioOrganizacao> findByUsuarioIdAndOrganizacaoIdAndTurmaPermitidaId(Long usuarioId, Long organizacaoId, Long turmaId);
}
