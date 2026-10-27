# Inserir usuário de teste no H2 local

Este procedimento é somente para desenvolvimento local. O console H2 está habilitado e liberado pela cadeia específica de segurança; não exponha essa aplicação ou o console em uma rede pública.

1. Na raiz do projeto, gere o classpath das dependências:

   ```powershell
   mvn dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
   $classpath = "target/classes;" + (Get-Content target/classpath.txt -Raw)
   jshell --class-path $classpath
   ```

2. No JShell, use a mesma fábrica de encoder configurada pela aplicação e leia uma senha fictícia sem gravá-la em um arquivo:

   ```java
   import org.springframework.security.crypto.factory.PasswordEncoderFactories;
   var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
   var senha = System.console().readPassword("Senha fictícia de teste: ");
   System.out.println(encoder.encode(new String(senha)));
   ```

   Copie somente o hash impresso, que inclui o identificador do algoritmo (por exemplo, `{bcrypt}`). Não copie a senha para código, SQL versionado ou logs.

3. Acesse `http://localhost:8080/h2-console` enquanto a aplicação estiver rodando. Use JDBC URL `jdbc:h2:file:./data/automationlab;DB_CLOSE_ON_EXIT=FALSE`, usuário `sa` e a senha local configurada (vazia por padrão neste projeto).

4. Insira dados fictícios e o hash gerado. Troque `<HASH_GERADO>` pelo valor copiado:

   ```sql
   INSERT INTO usuarios (nome, username, password_hash, role)
   VALUES ('Administrador de Teste', 'admin.teste', '<HASH_GERADO>', 'ROLE_ADMIN');
   ```

   Para um usuário comum, use `ROLE_USER`. O username é armazenado em minúsculas, sem espaços externos. Não mantenha senhas ou hashes de teste em arquivos versionados.
