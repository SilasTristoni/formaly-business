package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Instituicao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusRegistro;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    List<Instituicao> findByOrganizacaoIdOrderByNomeAsc(Long organizacaoId);
    List<Instituicao> findByOrganizacaoIdAndStatusOrderByNomeAsc(Long organizacaoId, StatusRegistro status);
    Optional<Instituicao> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
