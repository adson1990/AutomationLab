package br.com.automationlab.servico;

import br.com.automationlab.entidade.Role;
import br.com.automationlab.entidade.Usuario;
import br.com.automationlab.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioDetailsServiceTests {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final UsuarioDetailsService service = new UsuarioDetailsService(repository);

    @Test
    void carregaUsuarioEConverteRoleEmAutoridade() {
        Usuario usuario = new Usuario("Admin Fictício", "admin.teste", "{bcrypt}hash-ficticio", Role.ROLE_ADMIN);
        when(repository.findByUsername(" ADMIN.TESTE ")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername(" ADMIN.TESTE ");

        assertEquals("admin.teste", details.getUsername());
        assertEquals("{bcrypt}hash-ficticio", details.getPassword());
        assertEquals("ROLE_ADMIN", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void preservaRoleDeUsuarioComumComoAutoridade() {
        Usuario usuario = new Usuario("Usuário Fictício", "usuario.teste", "{bcrypt}hash-ficticio",
                Role.ROLE_USER);
        when(repository.findByUsername("usuario.teste")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("usuario.teste");

        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void usernameInexistenteGeraUsernameNotFoundException() {
        when(repository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inexistente"));
    }
}
