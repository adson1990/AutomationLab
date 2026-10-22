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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @CpfValido
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    private String curso;
    private String turno;

    @Column(name = "nome_responsavel")
    private String nomeResponsavel;

    @Column(name = "telefone_responsavel")
    private String telefoneResponsavel;

    @Pattern(
            regexp = "^\\s*$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "E-mail inválido"
    )
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
