package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Comprovante;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusComprovante;

public interface ComprovanteRepository extends JpaRepository<Comprovante, Long> {
    List<Comprovante> findByOrganizacaoIdOrderByDataEnvioDesc(Long organizacaoId);
    List<Comprovante> findByTurmaIdOrderByDataEnvioDesc(Long turmaId);
    List<Comprovante> findByTurmaIdInOrderByDataEnvioDesc(Collection<Long> turmaIds);
    long countByOrganizacaoIdAndStatus(Long organizacaoId, StatusComprovante status);
    long countByTurmaIdAndStatus(Long turmaId, StatusComprovante status);
}
