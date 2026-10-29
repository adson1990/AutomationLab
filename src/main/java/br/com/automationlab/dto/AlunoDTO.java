package br.com.automationlab.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.util.CpfValido;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class AlunoDTO {

    private Long id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String curso;
    private String turno;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String email;
    private String celular;

    public AlunoDTO() {
    }

    public AlunoDTO(Long id, String nome, String cpf, LocalDate dataNascimento, String curso, String turno,
            String nomeResponsavel, String telefoneResponsavel, String email, String celular) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.curso = curso;
        this.turno = turno;
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
        this.email = email;
        this.celular = celular;
    }

    public AlunoDTO(Aluno aluno) {
        this.id = aluno.getId();
        this.nome = aluno.getNome();
        this.cpf = aluno.getCpf();
        this.dataNascimento = aluno.getDataNascimento();
        this.curso = aluno.getCurso();
        this.turno = aluno.getTurno();
        this.nomeResponsavel = aluno.getNomeResponsavel();
        this.telefoneResponsavel = aluno.getTelefoneResponsavel();
        this.email = aluno.getEmail();
        this.celular = aluno.getCelular();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getCurso() {
        return curso;
    }

    public String getTurno() {
        return turno;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getTelefoneResponsavel() {
        return telefoneResponsavel;
    }

    public String getEmail() {
        return email;
    }

    public String getCelular() {

        return celular;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public void setTelefoneResponsavel(String telefoneResponsavel) {
        this.telefoneResponsavel = telefoneResponsavel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }
}