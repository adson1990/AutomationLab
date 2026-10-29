package br.com.automationlab.servico;

import br.com.automationlab.dto.AlunoDTO;
import br.com.automationlab.dto.AlunoListagemDTO;
import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.exceptions.AlunoException.ErroCampo;
import br.com.automationlab.exceptions.AlunoNaoEncontradoException;
import br.com.automationlab.repositorio.AlunoRepository;

import org.hibernate.exception.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class AlunoService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String MASCARA_CPF = "********";

    private final AlunoRepository repository;
    private final Validator validator;

    public AlunoService(AlunoRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional
    public Aluno salvar(Aluno aluno) {
        if ("".equals(aluno.getEmail())) {
            aluno.setEmail(null);
        }

        Set<ConstraintViolation<Aluno>> violations = validator.validate(aluno);
        if (!violations.isEmpty()) {
            List<ErroCampo> erros = violations.stream()
                    .map(erro -> new ErroCampo(erro.getPropertyPath().toString(), erro.getMessage()))
                    .distinct()
                    .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem))
                    .toList();
            throw AlunoException.comErros(erros);
        }
        aluno.setCpf(normalizarCpf(aluno.getCpf()));

        if (isMenorDeIdade(aluno.getDataNascimento())) {
            List<ErroCampo> errosResponsavel = new ArrayList<>();
            if (emBranco(aluno.getNomeResponsavel())) {
                errosResponsavel.add(new ErroCampo(
                        "nomeResponsavel", "Nome do responsável é obrigatório para menores de 18 anos"));
            }
            if (emBranco(aluno.getTelefoneResponsavel())) {
                errosResponsavel.add(new ErroCampo(
                        "telefoneResponsavel", "Telefone do responsável é obrigatório para menores de 18 anos"));
            }
            if (!errosResponsavel.isEmpty()) {
                throw AlunoException.comErros(errosResponsavel);
            }
        }

        boolean cpfPertenceAOutroAluno = repository.findByCpf(aluno.getCpf())
                .filter(existente -> !existente.getId().equals(aluno.getId()))
                .isPresent();
        if (cpfPertenceAOutroAluno) {
            throw AlunoException.comErros(List.of(new ErroCampo("cpf", "Já existe um aluno cadastrado com este CPF")));
        }

        try {
            return repository.saveAndFlush(aluno);
        } catch (DataIntegrityViolationException exception) {
            if (isViolacaoDeCpfUnico(exception)) {
                throw AlunoException.comErros(List.of(
                        new ErroCampo("cpf", "Já existe um aluno cadastrado com este CPF")), exception);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<AlunoListagemDTO> listar(Authentication autenticacao) {
        boolean administrador = autenticacao != null && autenticacao.getAuthorities() != null
                && autenticacao.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));

        return repository.findAll().stream()
                .map(aluno -> new AlunoListagemDTO(
                        aluno.getNome(),
                        administrador ? aluno.getCpf() : mascararCpf(aluno.getCpf()),
                        aluno.getDataNascimento(),
                        aluno.getCurso(),
                        aluno.getTurno()))
                .toList();
    }

    private String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() < 3) {
            return MASCARA_CPF;
        }
        return cpf.substring(0, 3) + MASCARA_CPF;
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[.\\-/\\s]", "");
    }

    private boolean isMenorDeIdade(LocalDate nascimento) {
        return Period.between(nascimento, LocalDate.now()).getYears() < 18;
    }

    private boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean isViolacaoDeCpfUnico(DataIntegrityViolationException exception) {
        Throwable causa = exception;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violacao
                    && Aluno.CONSTRAINT_CPF_UNIQUE.equalsIgnoreCase(violacao.getConstraintName())) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public AlunoDTO buscarPorId(Long id, Authentication autenticacao) {
        Aluno aluno = repository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));
        
        AlunoDTO alunoDTO = new AlunoDTO(aluno);
        alunoDTO.setCpf(retornaCpfPorRuleDeUsuario(autenticacao, aluno.getCpf()));
        
        return alunoDTO;
    }

    public String retornaCpfPorRuleDeUsuario(Authentication autenticacao, String cpf) {
        boolean administrador = isAdministrador(autenticacao);

        return administrador ? cpf : mascararCpf(cpf);
    }

    public boolean isAdministrador(Authentication autenticacao) {
        return autenticacao != null && autenticacao.getAuthorities() != null
                && autenticacao.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
