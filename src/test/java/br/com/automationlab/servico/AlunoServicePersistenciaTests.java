package br.com.automationlab.servico;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.repositorio.AlunoRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlunoServicePersistenciaTests {
    private static final String CPF = "52998224725";

    @Test
    void converteSomenteConflitoDaConstraintUnicaDeCpfEPreservaACausa() {
        AlunoRepository repository = mock(AlunoRepository.class);
        DataIntegrityViolationException falhaBanco = falhaIntegridade(Aluno.CONSTRAINT_CPF_UNIQUE);
        when(repository.findByCpf(CPF)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Aluno.class))).thenThrow(falhaBanco);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            AlunoService service = new AlunoService(repository, factory.getValidator());
            AlunoException erro = assertThrows(AlunoException.class,
                    () -> service.salvar(alunoValido()));

            assertSame(falhaBanco, erro.getCause());
        }
    }

    @Test
    void naoConverteOutrasFalhasDeIntegridadeEmCpfDuplicado() {
        AlunoRepository repository = mock(AlunoRepository.class);
        DataIntegrityViolationException falhaBanco = falhaIntegridade("uk_outra_coluna");
        when(repository.findByCpf(CPF)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Aluno.class))).thenThrow(falhaBanco);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            AlunoService service = new AlunoService(repository, factory.getValidator());

            assertSame(falhaBanco, assertThrows(DataIntegrityViolationException.class,
                    () -> service.salvar(alunoValido())));
        }
    }

    private DataIntegrityViolationException falhaIntegridade(String nomeConstraint) {
        SQLException sqlException = new SQLException("violação de integridade", "23505");
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "violação de constraint", sqlException, nomeConstraint);
        return new DataIntegrityViolationException("falha de integridade", hibernateException);
    }

    private Aluno alunoValido() {
        return new Aluno("Estudante Fictício", CPF, LocalDate.now().minusYears(20),
                null, null, null, null);
    }
}
