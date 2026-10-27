package br.com.automationlab.controller;

import br.com.automationlab.dto.AlunoListagemDTO;
import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.exceptions.AlunoException.ErroCampo;
import br.com.automationlab.servico.AlunoService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AlunoController {
    private static final String FORM_VIEW = "aluno/formulario";
    private static final String ALUNO_ATTRIBUTE = "aluno";
    private static final String MENSAGEM_GENERICA =
            "Não foi possível cadastrar o aluno. Verifique os dados e tente novamente.";

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping("/alunos/novo")
    public String novo(Model model) {
        model.addAttribute(ALUNO_ATTRIBUTE, novoAlunoVazio());
        return FORM_VIEW;
    }

    @GetMapping("/alunos")
    public String listar(Authentication autenticacao, Model model) {
        List<AlunoListagemDTO> alunos = alunoService.listar(autenticacao);
        model.addAttribute("alunos", alunos);
        return "aluno/listagem";
    }

    @PostMapping("/alunos")
    public String salvar(
            @Valid @ModelAttribute(ALUNO_ATTRIBUTE) Aluno aluno,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }

        try {
            alunoService.salvar(aluno);
        } catch (AlunoException exception) {
            model.addAttribute(ALUNO_ATTRIBUTE, aluno);
            adicionarErros(exception, aluno, bindingResult);
            return FORM_VIEW;
        }

        redirectAttributes.addFlashAttribute("sucesso", "Aluno cadastrado com sucesso.");
        return "redirect:/alunos/novo";
    }

    private void adicionarErros(AlunoException exception, Aluno aluno, BindingResult bindingResult) {
        if (exception.getErros().isEmpty()) {
            bindingResult.reject("aluno.invalido", MENSAGEM_GENERICA);
            return;
        }

        BeanWrapper propriedades = PropertyAccessorFactory.forBeanPropertyAccess(aluno);
        for (ErroCampo erro : exception.getErros()) {
            if (erro.campo() != null && propriedades.isWritableProperty(erro.campo())) {
                bindingResult.rejectValue(erro.campo(), "aluno.invalido", erro.mensagem());
            } else {
                bindingResult.reject("aluno.invalido", erro.mensagem());
            }
        }
    }

    private Aluno novoAlunoVazio() {
        return new Aluno("", "", null, null, null, null, null);
    }
}
