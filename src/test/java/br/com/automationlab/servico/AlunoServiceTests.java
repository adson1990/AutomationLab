package br.com.automationlab.servico;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.exceptions.AlunoException.ErroCampo;
import br.com.automationlab.repositorio.AlunoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

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
        assertEquals(List.of("dataNascimento", "email", "nome"),
                erro.getErros().stream().map(ErroCampo::campo).toList());
    }

    @Test
    void aceitaEmailNuloEVazioECelularOpcionais() {
        Aluno aluno = alunoAdulto(CPF_VALIDO);
        aluno.setEmail(null);
        aluno.setCelular(null);

        Aluno salvo = service.salvar(aluno);

        assertEquals(null, salvo.getEmail());
        assertEquals(null, salvo.getCelular());

        Aluno alunoComEmailVazio = alunoAdulto("111.444.777-35");
        alunoComEmailVazio.setEmail("");
        assertEquals(null, service.salvar(alunoComEmailVazio).getEmail());
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

    @Test
    void rejeitaEmailComMaisDe254Caracteres() {
        Aluno aluno = alunoAdulto(CPF_VALIDO);
        aluno.setEmail("a".repeat(243) + "@example.com");

        AlunoException erro = assertThrows(AlunoException.class, () -> service.salvar(aluno));

        assertTrue(erro.getErros().stream().anyMatch(campo -> campo.campo().equals("email")));
    }

    @Test
    void rejeitaFormatosDeEmailComPontosEmPosicoesInvalidas() {
        for (String email : List.of(
                "a..b@example.com", ".a@example.com", "a.@example.com",
                "a@example..com", "a@-example.com", "a@b-.com")) {
            Aluno aluno = alunoAdulto(CPF_VALIDO);
            aluno.setEmail(email);

            assertThrows(AlunoException.class, () -> service.salvar(aluno), email);
        }
    }

    @Test
    void rejeitaCpfAusente() {
        Aluno aluno = alunoAdulto(CPF_VALIDO);
        aluno.setCpf(null);

        AlunoException erro = assertThrows(AlunoException.class, () -> service.salvar(aluno));

        assertTrue(erro.getErros().stream().anyMatch(campo -> campo.campo().equals("cpf")));
    }

    @Test
    void rejeitaDataDeNascimentoNoFuturoMasAceitaHoje() {
        Aluno futuro = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().plusDays(1),
                null, null, null, null);
        assertThrows(AlunoException.class, () -> service.salvar(futuro));

        Aluno nascidoHoje = new Aluno("Estudante Fictício", "111.444.777-35", LocalDate.now(),
                null, null, "Responsável Fictício", "11999990000");
        assertNotNull(service.salvar(nascidoHoje).getId());
    }

    @Test
    void rejeitaResponsavelIncompletoParaMenor() {
        Aluno semNome = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().minusYears(17),
                null, null, null, "11999990000");
        AlunoException erroNome = assertThrows(AlunoException.class, () -> service.salvar(semNome));
        assertTrue(erroNome.getErros().stream().anyMatch(campo -> campo.campo().equals("nomeResponsavel")));

        Aluno semTelefone = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().minusYears(17),
                null, null, "Responsável Fictício", " ");
        AlunoException erroTelefone = assertThrows(AlunoException.class, () -> service.salvar(semTelefone));
        assertTrue(erroTelefone.getErros().stream()
                .anyMatch(campo -> campo.campo().equals("telefoneResponsavel")));
    }

    @Test
    void informaSomenteCadaCampoDeResponsavelQueEstaFaltando() {
        Aluno semNome = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().minusYears(17),
                null, null, " ", "11999990000");
        AlunoException erroNome = assertThrows(AlunoException.class, () -> service.salvar(semNome));
        assertEquals(List.of("nomeResponsavel"),
                erroNome.getErros().stream().map(ErroCampo::campo).toList());

        Aluno semTelefone = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().minusYears(17),
                null, null, "Responsável Fictício", null);
        AlunoException erroTelefone = assertThrows(AlunoException.class, () -> service.salvar(semTelefone));
        assertEquals(List.of("telefoneResponsavel"),
                erroTelefone.getErros().stream().map(ErroCampo::campo).toList());
    }

    @Test
    void consideraAdultoQuemCompleta18AnosHoje() {
        Aluno aluno = new Aluno("Estudante Fictício", CPF_VALIDO, LocalDate.now().minusYears(18),
                null, null, null, null);

        assertNotNull(service.salvar(aluno).getId());
    }

    private Aluno alunoAdulto(String cpf) {
        return new Aluno("Estudante Fictício", cpf, LocalDate.now().minusYears(20),
                null, null, null, null);
    }
}
