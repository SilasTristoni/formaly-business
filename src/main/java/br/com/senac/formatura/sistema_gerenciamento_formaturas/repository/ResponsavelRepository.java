package br.com.senac.formatura.sistema_gerenciamento_formaturas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Responsavel;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {
    List<Responsavel> findByOrganizacaoIdOrderByNomeAsc(Long organizacaoId);
}
