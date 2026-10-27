package br.com.automationlab.exceptions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exceção personalizada para representar erros relacionados a operações com alunos.
 */

public class AlunoException extends RuntimeException {
    private final List<ErroCampo> erros;

    public AlunoException(String message) {
        super(message);
        this.erros = List.of();
    }

    public AlunoException(String message, Throwable cause) {
        super(message, cause);
        this.erros = List.of();
    }

    private AlunoException(String message, Throwable cause, List<ErroCampo> erros) {
        super(message, cause);
        this.erros = List.copyOf(erros);
    }

    public static AlunoException comErros(List<ErroCampo> erros) {
        List<ErroCampo> ordenados = erros.stream()
                .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem))
                .toList();
        String mensagem = ordenados.stream()
                .map(erro -> erro.campo() + ": " + erro.mensagem())
                .collect(Collectors.joining("; "));
        return new AlunoException("Não foi possível salvar o aluno: " + mensagem, null, ordenados);
    }

    public static AlunoException comErros(List<ErroCampo> erros, Throwable causa) {
        List<ErroCampo> ordenados = erros.stream()
                .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem))
                .toList();
        String mensagem = ordenados.stream()
                .map(erro -> erro.campo() + ": " + erro.mensagem())
                .collect(Collectors.joining("; "));
        return new AlunoException("Não foi possível salvar o aluno: " + mensagem, causa, ordenados);
    }

    public List<ErroCampo> getErros() {
        return erros;
    }

    public record ErroCampo(String campo, String mensagem) {
    }
}
