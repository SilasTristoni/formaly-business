package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RevisaoDocumento;

public interface RevisaoDocumentoRepository extends JpaRepository<RevisaoDocumento, Long> {
    List<RevisaoDocumento> findByDocumentoIdOrderByDataRevisaoDesc(Long documentoId);
}
