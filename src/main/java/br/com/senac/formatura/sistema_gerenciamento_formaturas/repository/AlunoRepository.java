package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Aluno findByIdentificador(String identificador);
    Optional<Aluno> findFirstByTurmaIdAndIdentificadorIgnoreCase(Long turmaId, String identificador);
    Optional<Aluno> findByIdAndTurmaOrganizacaoId(Long id, Long organizacaoId);
    List<Aluno> findByTurmaOrganizacaoIdOrderByNomeAsc(Long organizacaoId);
    List<Aluno> findByTurmaIdOrderByNomeAsc(Long turmaId);
    List<Aluno> findByTurmaIdInOrderByNomeAsc(List<Long> turmaIds);
    long countByTurmaId(Long turmaId);
    long countByTurmaOrganizacaoId(Long organizacaoId);
}
