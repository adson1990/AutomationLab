package br.com.automationlab.servico;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.repositorio.AlunoRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class AlunoService {
    private final AlunoRepository repository;
    private final Validator validator;

    public AlunoService(AlunoRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional
    public Aluno salvar(Aluno aluno) {
        Set<ConstraintViolation<Aluno>> violations = validator.validate(aluno);
        if (!violations.isEmpty()) {
            String mensagens = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .collect(Collectors.joining("; "));
            throw new AlunoException("Não foi possível salvar o aluno: " + mensagens);
        }
        aluno.setCpf(normalizarCpf(aluno.getCpf()));

        if (isMenorDeIdade(aluno.getDataNascimento())
                && (emBranco(aluno.getNomeResponsavel()) || emBranco(aluno.getTelefoneResponsavel()))) {
            throw new AlunoException(
                    "Nome e telefone do responsável são obrigatórios para menores de 18 anos");
        }

        boolean cpfPertenceAOutroAluno = repository.findByCpf(aluno.getCpf())
                .filter(existente -> !existente.getId().equals(aluno.getId()))
                .isPresent();
        if (cpfPertenceAOutroAluno) {
            throw new AlunoException("Já existe um aluno cadastrado com este CPF");
        }

        return repository.save(aluno);
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[.\\-/\\s]", "");
    }

    private boolean isMenorDeIdade(LocalDate nascimento) {
        return Period.between(nascimento, LocalDate.now()).getYears() < 18;
    }

    private boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
