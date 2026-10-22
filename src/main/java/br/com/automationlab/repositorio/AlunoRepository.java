package br.com.automationlab.repositorio;

import br.com.automationlab.entidade.Aluno;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByCpf(String cpf);

    Optional<Aluno> findByCpf(String cpf);

    Optional<Aluno> findByEmail(String email);

    Optional<Aluno> findById(Long id);
}
