package br.com.automationlab.controller;

import br.com.automationlab.dto.AlunoDTO;
import br.com.automationlab.dto.AlunoListagemDTO;
import br.com.automationlab.entidade.Aluno;
import br.com.automationlab.exceptions.AlunoException;
import br.com.automationlab.exceptions.AlunoException.ErroCampo;
import br.com.automationlab.exceptions.AlunoNaoEncontradoException;
import br.com.automationlab.servico.AlunoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class AlunoController {
    private static final String FORM_VIEW = "aluno/formulario";
    private static final String ALUNO_ATTRIBUTE = "aluno";
    private static final String MENSAGEM_GENERICA =
            "Não foi possível cadastrar o aluno. Verifique os dados e tente novamente.";
    private static final String ALUNO_INVALIDO = "aluno.invalido";

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

    @GetMapping("/alunos/buscar")
    public String abrirBusca(Model model) {
        model.addAttribute("id", null);
        return "aluno/aluno";
    }

    @GetMapping("/alunos/buscar/{id}")
    public String buscarPorId(@PathVariable Long id,
                              Authentication autenticacao, 
                              Model model,
                              HttpServletResponse response) {
        try {
            AlunoDTO alunoDTO = alunoService.buscarPorId(id, autenticacao);
            model.addAttribute(ALUNO_ATTRIBUTE, alunoDTO);
            return "aluno/aluno";
        } catch (AlunoNaoEncontradoException exception) {
            model.addAttribute("id", id);
            model.addAttribute("erroBusca", "Não foi encontrado aluno com o ID " + id + ".");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return "aluno/aluno";
        }
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

    @PostMapping("/alunos/upd/{id}")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute(ALUNO_ATTRIBUTE) Aluno aluno,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication autenticacao) {
        model.addAttribute("alunoId", id);
        if (bindingResult.hasErrors()) {
            return "aluno/aluno";
        }

        try {
            alunoService.atualizar(id, aluno, autenticacao);
        } catch (AlunoException exception) {
            model.addAttribute(ALUNO_ATTRIBUTE, aluno);
            adicionarErros(exception, aluno, bindingResult);
            return "aluno/aluno";
        } catch (AlunoNaoEncontradoException exception) {
            model.addAttribute(ALUNO_ATTRIBUTE, aluno);
            bindingResult.reject("aluno.naoEncontrado", exception.getMessage());
            return "aluno/aluno";
        }

        redirectAttributes.addFlashAttribute("sucesso_edit", "Aluno atualizado com sucesso.");
        return "redirect:/alunos/buscar";
    }

    @PostMapping("/aluno/deletar/{id}")
    public String deletar(@PathVariable Long id, 
                          Authentication autenticacao, 
                          RedirectAttributes redirectAttributes) {
        try {
            alunoService.deletar(id, autenticacao);
            redirectAttributes.addFlashAttribute("sucesso_delete", "Aluno deletado com sucesso.");
        } catch (AlunoNaoEncontradoException exception) {
            redirectAttributes.addFlashAttribute("erroBusca", "Não foi encontrado aluno com o ID " + id + ".");
        } catch (AlunoException exception) {
            redirectAttributes.addFlashAttribute("erroBusca", exception.getMessage());
        }
        return "redirect:/alunos/buscar";
    }

    private void adicionarErros(AlunoException exception, Aluno aluno, BindingResult bindingResult) {
        if (exception.getErros().isEmpty()) {
            bindingResult.reject(ALUNO_INVALIDO, MENSAGEM_GENERICA);
            return;
        }

        BeanWrapper propriedades = PropertyAccessorFactory.forBeanPropertyAccess(aluno);
        for (ErroCampo erro : exception.getErros()) {
            if (erro.campo() != null && propriedades.isWritableProperty(erro.campo())) {
                bindingResult.rejectValue(erro.campo(), ALUNO_INVALIDO, erro.mensagem());
            } else {
                bindingResult.reject(ALUNO_INVALIDO, erro.mensagem());
            }
        }
    }

    private Aluno novoAlunoVazio() {
        return new Aluno("", "", null, null, null, null, null);
    }
}
