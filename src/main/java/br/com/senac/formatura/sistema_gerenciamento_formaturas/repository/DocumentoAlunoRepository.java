package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.DocumentoAluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;

public interface DocumentoAlunoRepository extends JpaRepository<DocumentoAluno, Long> {
    List<DocumentoAluno> findByAlunoIdOrderByTipoDocumentoNomeAscVersaoDesc(Long alunoId);
    List<DocumentoAluno> findByAlunoTurmaIdOrderByDataEnvioDesc(Long turmaId);
    List<DocumentoAluno> findByAlunoTurmaOrganizacaoIdOrderByDataEnvioDesc(Long organizacaoId);
    List<DocumentoAluno> findByAlunoTurmaIdInOrderByDataEnvioDesc(Collection<Long> turmaIds);
    long countByAlunoTurmaOrganizacaoIdAndStatus(Long organizacaoId, StatusDocumento status);
    long countByAlunoTurmaIdAndStatus(Long turmaId, StatusDocumento status);
    Optional<DocumentoAluno> findTopByAlunoIdAndTipoDocumentoIdOrderByVersaoDesc(Long alunoId, Long tipoDocumentoId);
}
