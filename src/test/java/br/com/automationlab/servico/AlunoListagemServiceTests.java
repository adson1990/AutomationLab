package br.com.automationlab.servico;

import br.com.automationlab.dto.AlunoListagemDTO;
import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.repositorio.AlunoRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlunoListagemServiceTests {
    private static final String CPF_COMPLETO = "52998224725";

    private final AlunoRepository repository = mock(AlunoRepository.class);
    private final AlunoService service = new AlunoService(repository, mock(Validator.class));

    @BeforeEach
    void prepararAluno() {
        when(repository.findAll()).thenReturn(List.of(new Aluno("Aluno Fictício", CPF_COMPLETO,
                LocalDate.of(2000, 1, 1), "Engenharia", "Noite", null, null)));
    }

    @Test
    void administradorRecebeCpfCompleto() {
        AlunoListagemDTO dto = service.listar(authentication("ROLE_ADMIN")).get(0);

        assertEquals(CPF_COMPLETO, dto.cpfExibicao());
    }

    @Test
    void usuarioComumRecebeCpfMascaradoSemCampoComCpfOriginal() {
        AlunoListagemDTO dto = service.listar(authentication("ROLE_USER")).get(0);

        assertEquals("529********", dto.cpfExibicao());
        assertEquals(List.of("nome", "cpfExibicao", "dataNascimento", "curso", "turno"),
                java.util.Arrays.stream(AlunoListagemDTO.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName).toList());
    }

    @Test
    void autoridadeAusenteOuNaoAdministrativaRecebeSomenteCpfMascarado() {
        assertEquals("529********", service.listar(authentication("ROLE_AUDITOR")).get(0).cpfExibicao());
        assertEquals("529********", service.listar(null).get(0).cpfExibicao());
        assertEquals("529********", service.listar(authentication()).get(0).cpfExibicao());
    }

    private Authentication authentication(String... authorities) {
        return UsernamePasswordAuthenticationToken.authenticated(
                "usuario.teste", "ignored",
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
    }
}
