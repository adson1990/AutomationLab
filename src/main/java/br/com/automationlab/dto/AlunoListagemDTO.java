package br.com.automationlab.dto;

import java.time.LocalDate;

public record AlunoListagemDTO(
        String nome,
        String cpfExibicao,
        LocalDate dataNascimento,
        String curso,
        String turno) {
}
