package br.com.automationlab.repositorio;

import br.com.automationlab.entidade.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("select usuario from Usuario usuario where usuario.username = :username")
    Optional<Usuario> buscarUsernameNormalizado(@Param("username") String username);

    default Optional<Usuario> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return buscarUsernameNormalizado(Usuario.normalizarUsername(username));
    }
}
