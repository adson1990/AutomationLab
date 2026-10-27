package br.com.automationlab.entidade;

import br.com.automationlab.util.CpfValido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "alunos", uniqueConstraints =
        @UniqueConstraint(name = Aluno.CONSTRAINT_CPF_UNIQUE, columnNames = "cpf"))
public class Aluno {
    public static final String CONSTRAINT_CPF_UNIQUE = "uk_alunos_cpf";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @CpfValido
    @Column(nullable = false, length = 11)
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    private String curso;
    private String turno;

    @Column(name = "nome_responsavel")
    private String nomeResponsavel;

    @Column(name = "telefone_responsavel")
    private String telefoneResponsavel;

    @Email(message = "E-mail inválido")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres")
    @Column(length = 254)
    private String email;

    private String celular;

    protected Aluno() {
    }

    public Aluno(String nome, String cpf, LocalDate dataNascimento, String curso, String turno,
                 String nomeResponsavel, String telefoneResponsavel) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.curso = curso;
        this.turno = turno;
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
    }

    @PrePersist
    @PreUpdate
    void normalizarCpf() {
        if (cpf != null) {
            cpf = cpf.replaceAll("[.\\-/\\s]", "");
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }
    public String getTelefoneResponsavel() { return telefoneResponsavel; }
    public void setTelefoneResponsavel(String telefoneResponsavel) { this.telefoneResponsavel = telefoneResponsavel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
}
