package br.com.automationlab.servico;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.repositorio.AlunoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AlunoServiceTests {
    private static final String CPF_VALIDO = "529.982.247-25";

    @Autowired
    private AlunoService service;

    @Autowired
    private AlunoRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void salvaAlunoComCpfNormalizadoECamposOpcionais() {
        Aluno aluno = service.salvar(alunoAdulto(CPF_VALIDO));

        assertNotNull(aluno.getId());
        assertEquals("52998224725", aluno.getCpf());
        assertEquals(null, aluno.getCurso());
        assertEquals(null, aluno.getTurno());
        assertEquals(aluno.getId(), repository.findById(aluno.getId()).orElseThrow().getId());
    }

    @Test
    void rejeitaCpfComDigitosVerificadoresInvalidos() {
        assertThrows(AlunoException.class,
                () -> service.salvar(alunoAdulto("111.111.111-11")));
    }

    @Test
    void rejeitaCpfComCaracteresQueNaoSaoSeparadoresValidos() {
        assertThrows(AlunoException.class,
                () -> service.salvar(alunoAdulto("abc52998224725")));
    }

    @Test
    void rejeitaCpfDuplicadoMesmoQuandoFormatadoDeFormaDiferente() {
        service.salvar(alunoAdulto(CPF_VALIDO));

        assertThrows(AlunoException.class,
                () -> service.salvar(alunoAdulto("52998224725")));
    }

    @Test
    void bancoImpedeCpfDuplicadoMesmoSeORepositorioForUsadoDiretamente() {
        repository.saveAndFlush(alunoAdulto(CPF_VALIDO));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(alunoAdulto("52998224725")));
    }

    @Test
    void exigeResponsavelParaMenorEPermiteAdultoSemResponsavel() {
        Aluno menor = new Aluno("Estudante Fictício", "52998224725", LocalDate.now().minusYears(17),
                null, null, null, null);
        assertThrows(AlunoException.class, () -> service.salvar(menor));

        Aluno menorComResponsavel = new Aluno("Estudante Fictício", "11144477735",
                LocalDate.now().minusYears(17), null, null, "Responsável Fictício", "11999990000");
        assertNotNull(service.salvar(menorComResponsavel).getId());
    }

    @Test
    void rejeitaNomeDataDeNascimentoEEmaiInvalidosComExcecaoDeAluno() {
        Aluno aluno = new Aluno(" ", CPF_VALIDO, null, null, null, null, null);
        aluno.setEmail("email-invalido");

        AlunoException erro = assertThrows(AlunoException.class, () -> service.salvar(aluno));

        assertTrue(erro.getMessage().contains("Nome é obrigatório"));
        assertTrue(erro.getMessage().contains("Data de nascimento é obrigatória"));
        assertTrue(erro.getMessage().contains("E-mail inválido"));
    }

    @Test
    void aceitaEmailECelularOpcionaisOuEmBranco() {
        Aluno aluno = alunoAdulto(CPF_VALIDO);
        aluno.setEmail(" ");
        aluno.setCelular(null);

        Aluno salvo = service.salvar(aluno);

        assertEquals(" ", salvo.getEmail());
        assertEquals(null, salvo.getCelular());
    }

    @Test
    void aceitaEmailComFormatoValidoECelularInformado() {
        Aluno aluno = alunoAdulto(CPF_VALIDO);
        aluno.setEmail("aluno@example.com");
        aluno.setCelular("11999990000");

        Aluno salvo = service.salvar(aluno);

        assertEquals("aluno@example.com", salvo.getEmail());
        assertEquals("11999990000", salvo.getCelular());
    }

    private Aluno alunoAdulto(String cpf) {
        return new Aluno("Estudante Fictício", cpf, LocalDate.now().minusYears(20),
                null, null, null, null);
    }
}