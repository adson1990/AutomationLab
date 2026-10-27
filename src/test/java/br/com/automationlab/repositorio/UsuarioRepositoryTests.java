package br.com.automationlab.repositorio;

import br.com.automationlab.entidade.Role;
import br.com.automationlab.entidade.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuarios-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UsuarioRepositoryTests {
    @Autowired
    private UsuarioRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void consultaUsernameRemovendoEspacosEIgnorandoMaiusculas() {
        Usuario usuario = repository.saveAndFlush(
                new Usuario("Usuário de Teste", "  Teste.User ", hashFicticio(), Role.ROLE_USER));

        Usuario encontrado = repository.findByUsername(" TESTE.USER ").orElseThrow();

        assertEquals(usuario.getId(), encontrado.getId());
        assertEquals("teste.user", encontrado.getUsername());
    }

    @Test
    void persisteRoleComoTexto() {
        repository.saveAndFlush(
                new Usuario("Admin de Teste", "admin.teste", hashFicticio(), Role.ROLE_ADMIN));

        Object valorArmazenado = entityManager
                .createNativeQuery("select role from usuarios where username = :username")
                .setParameter("username", "admin.teste")
                .getSingleResult();

        assertEquals("ROLE_ADMIN", valorArmazenado.toString());
    }

    @Test
    void restringeUsernameDuplicadoAposNormalizacao() {
        repository.saveAndFlush(
                new Usuario("Usuário Um", "mesmo.usuario", hashFicticio(), Role.ROLE_USER));
        Usuario duplicado = new Usuario("Usuário Dois", " MESMO.USUARIO ", hashFicticio(), Role.ROLE_ADMIN);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicado));
    }

    @Test
    void consultaNaoRetornaSenhaEmTextoPuro() {
        Usuario usuario = new Usuario("Usuário de Teste", "senha.teste", hashFicticio(), Role.ROLE_USER);
        repository.saveAndFlush(usuario);

        Usuario encontrado = repository.findByUsername("senha.teste").orElseThrow();

        assertTrue(encontrado.getPassword().startsWith("{bcrypt}"));
    }

    private String hashFicticio() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("senha-ficticia-de-teste");
    }
}
