package br.com.automationlab.config;

import br.com.automationlab.entidade.Role;
import br.com.automationlab.entidade.Usuario;
import br.com.automationlab.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:security-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SecurityAuthenticationTests {
    private static final String USERNAME = "usuario.teste";
    private static final String SENHA = "senha-ficticia-de-teste";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuarioPersistido() {
        usuarioRepository.deleteAll();
        usuarioRepository.saveAndFlush(new Usuario("Usuário Fictício", USERNAME,
                passwordEncoder.encode(SENHA), Role.ROLE_USER));
    }

    @Test
    void autenticaComSenhaCorretaArmazenadaComoHash() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals(SENHA, usuario.getPassword());

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", USERNAME)
                        .param("password", SENHA))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void rejeitaSenhaIncorreta() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", USERNAME)
                        .param("password", "senha-incorreta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void protegeRotaDaAplicacaoParaUsuarioAnonimo() throws Exception {
        mockMvc.perform(get("/alunos/novo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consoleH2MantemAcessoSemAutenticacaoSemCsrfEComFramesSameOrigin() throws Exception {
        var respostaGet = mockMvc.perform(get("/h2-console/"))
                .andReturn().getResponse();
        assertNotEquals(403, respostaGet.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("SAMEORIGIN",
                respostaGet.getHeader("X-Frame-Options"));

        var respostaPost = mockMvc.perform(post("/h2-console/"))
                .andReturn().getResponse();
        assertNotEquals(403, respostaPost.getStatus());
    }
}
