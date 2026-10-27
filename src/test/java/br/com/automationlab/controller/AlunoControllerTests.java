package br.com.automationlab.controller;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.repositorio.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class AlunoControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @BeforeEach
    void limparAlunos() {
        alunoRepository.deleteAll();
    }

    @Test
    void getExibeFormularioComTokenCsrf() throws Exception {
        mockMvc.perform(get("/alunos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("aluno/formulario"))
                .andExpect(model().attributeExists("aluno"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    void postValidoSalvaAlunoERedirecionaComMensagem() throws Exception {
        mockMvc.perform(post("/alunos").with(csrf())
                        .param("nome", "Estudante Fictício")
                        .param("cpf", "529.982.247-25")
                        .param("dataNascimento", "2000-01-01")
                        .param("curso", "Engenharia")
                        .param("turno", "Noite")
                        .param("email", "aluno@example.com")
                        .param("celular", "11999990000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alunos/novo"))
                .andExpect(flash().attribute("sucesso", "Aluno cadastrado com sucesso."));

        org.junit.jupiter.api.Assertions.assertEquals(1, alunoRepository.count());
        Aluno salvo = alunoRepository.findByCpf("52998224725").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Engenharia", salvo.getCurso());
        org.junit.jupiter.api.Assertions.assertEquals("aluno@example.com", salvo.getEmail());
    }

    @Test
    void postComErrosDeValidacaoRetornaFormularioEPreservaOsDados() throws Exception {
        mockMvc.perform(post("/alunos").with(csrf())
                        .param("nome", " ")
                        .param("cpf", "inválido")
                        .param("dataNascimento", "2000-01-01")
                        .param("curso", "Curso preenchido"))
                .andExpect(status().isOk())
                .andExpect(view().name("aluno/formulario"))
                .andExpect(model().attributeHasFieldErrors("aluno", "nome", "cpf"))
                .andExpect(model().attribute("aluno", hasProperty("curso", equalTo("Curso preenchido"))));

        org.junit.jupiter.api.Assertions.assertEquals(0, alunoRepository.count());
    }

    @Test
    void excecaoDeNegocioViraErroNoCampoCpf() throws Exception {
        alunoRepository.saveAndFlush(new Aluno("Aluno Existente", "52998224725",
                LocalDate.of(2000, 1, 1), null, null, null, null));

        mockMvc.perform(post("/alunos").with(csrf())
                        .param("nome", "Outro Estudante")
                        .param("cpf", "529.982.247-25")
                        .param("dataNascimento", "2001-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("aluno/formulario"))
                .andExpect(model().attributeHasFieldErrors("aluno", "cpf"))
                .andExpect(model().attribute("aluno", hasProperty(
                        "dataNascimento", equalTo(LocalDate.of(2001, 1, 1)))))
                .andExpect(content().string(containsString("value=\"2001-01-01\"")));

        org.junit.jupiter.api.Assertions.assertEquals(1, alunoRepository.count());
    }

    @Test
    void excecaoPorResponsavelIncompletoPreservaDataENaoMarcaCampoJaPreenchido() throws Exception {
        mockMvc.perform(post("/alunos").with(csrf())
                        .param("nome", "Estudante Menor")
                        .param("cpf", "529.982.247-25")
                        .param("dataNascimento", "2010-04-03")
                        .param("nomeResponsavel", "Responsável Fictício"))
                .andExpect(status().isOk())
                .andExpect(view().name("aluno/formulario"))
                .andExpect(model().attributeHasFieldErrors("aluno", "telefoneResponsavel"))
                .andExpect(model().attribute("aluno", hasProperty(
                        "dataNascimento", equalTo(LocalDate.of(2010, 4, 3)))))
                .andExpect(content().string(containsString("value=\"2010-04-03\"")));

        org.junit.jupiter.api.Assertions.assertEquals(0, alunoRepository.count());
    }

    @Test
    void postSemTokenCsrfContinuaProibido() throws Exception {
        mockMvc.perform(post("/alunos")
                        .param("nome", "Estudante Fictício")
                        .param("cpf", "529.982.247-25")
                        .param("dataNascimento", "2000-01-01"))
                .andExpect(status().isForbidden());
    }
}
