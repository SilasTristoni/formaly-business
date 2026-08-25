package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RequisitoDocumentoTurma;

public interface RequisitoDocumentoTurmaRepository extends JpaRepository<RequisitoDocumentoTurma, Long> {
    List<RequisitoDocumentoTurma> findByTurmaIdAndAtivoTrueOrderByTipoDocumentoNomeAsc(Long turmaId);
    Optional<RequisitoDocumentoTurma> findByTurmaIdAndTipoDocumentoId(Long turmaId, Long tipoDocumentoId);
}
