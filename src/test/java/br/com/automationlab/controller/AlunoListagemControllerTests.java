package br.com.automationlab.controller;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.repositorio.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:aluno-listagem-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AlunoListagemControllerTests {
    private static final String CPF_COMPLETO = "52998224725";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @BeforeEach
    void limparAlunos() {
        alunoRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void htmlDeUsuarioComumNaoContemCpfCompletoEmNenhumCampo() throws Exception {
        salvarAluno();

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(view().name("aluno/listagem"))
                .andExpect(content().string(containsString("529********")))
                .andExpect(content().string(not(containsString(CPF_COMPLETO))));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void htmlDeAdministradorContemCpfCompleto() throws Exception {
        salvarAluno();

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CPF_COMPLETO)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUDITOR")
    void autoridadeNaoAdministrativaRecebeCpfMascarado() throws Exception {
        salvarAluno();

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("529********")))
                .andExpect(content().string(not(containsString(CPF_COMPLETO))));
    }

    @Test
    @WithMockUser
    void listaVaziaExibeMensagemEPermiteAbrirCadastro() throws Exception {
        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum aluno cadastrado.")))
                .andExpect(content().string(containsString("/alunos/novo")));
    }

    @Test
    void acessoAnonimoContinuaBloqueado() throws Exception {
        var resposta = mockMvc.perform(get("/alunos")).andReturn().getResponse();

        assertEquals(401, resposta.getStatus());
    }

    private void salvarAluno() {
        alunoRepository.saveAndFlush(new Aluno("Aluno Fictício", CPF_COMPLETO,
                LocalDate.of(2000, 1, 1), "Engenharia", "Noite", null, null));
    }
}
