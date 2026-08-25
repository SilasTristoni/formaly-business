package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.TipoDocumento;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    List<TipoDocumento> findByOrganizacaoIdOrderByNomeAsc(Long organizacaoId);
    Optional<TipoDocumento> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
    Optional<TipoDocumento> findByOrganizacaoIdAndNomeIgnoreCase(Long organizacaoId, String nome);
}
