package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.HistoricoAuditoria;

public interface HistoricoAuditoriaRepository extends JpaRepository<HistoricoAuditoria, Long> {
    List<HistoricoAuditoria> findTop20ByOrganizacaoIdOrderByDataHoraDesc(Long organizacaoId);
    List<HistoricoAuditoria> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, String entidadeId);
}
