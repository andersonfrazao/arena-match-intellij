package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import br.com.arenamatch.jsf.client.TimeClient;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.enums.Categoria;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

@Component
@ViewScoped
public class CadastroBean implements Serializable {

    @Autowired private TimeClient timeClient;

    @Getter @Setter private TimeDTO timeDTO = new TimeDTO();
    @Getter @Setter private int step = 0;
    @Getter @Setter private DisponibilidadeDTO novaDisp = new DisponibilidadeDTO();
    @Getter private final Categoria[] categorias = Categoria.values();

    @PostConstruct
    public void init() {
        timeDTO.setDisponibilidades(new ArrayList<>());
    }

    public void next() { if(step < 2) step++; }
    public void back() { if(step > 0) step--; }
    public void setStep(int s) { this.step = s; }

    public void atualizarMandoCampo() {
        if (!"MANDO".equalsIgnoreCase(timeDTO.getMandoCampo())) {
            timeDTO.setTaxaJogo(null);
        }
    }

    public void buscarCep() {
        if(timeDTO.getCep() != null && timeDTO.getCep().length() >= 8) {
            TimeDTO dados = timeClient.buscarEnderecoPorCep(timeDTO.getCep());
            if(dados != null) {
                timeDTO.setLogradouro(dados.getLogradouro());
                timeDTO.setCidade(dados.getCidade());
                timeDTO.setUf(dados.getUf());
                timeDTO.setRegiao(dados.getRegiao());
            }
        }
    }

    public void adicionarHorario() {
        if (!validarHorario()) {
            return;
        }

        DisponibilidadeDTO d = new DisponibilidadeDTO();
        d.setDiaSemana(novaDisp.getDiaSemana());
        d.setCategoria(novaDisp.getCategoria());
        d.setHoraInicio(novaDisp.getHoraInicio());
        d.setHoraFim(novaDisp.getHoraFim());
        
        timeDTO.getDisponibilidades().add(d);
        novaDisp = new DisponibilidadeDTO(); 
    }

    private boolean validarHorario() {
        if (novaDisp.getDiaSemana() == null || novaDisp.getDiaSemana().isBlank()
                || novaDisp.getCategoria() == null
                || novaDisp.getHoraInicio() == null || novaDisp.getHoraInicio().isBlank()
                || novaDisp.getHoraFim() == null || novaDisp.getHoraFim().isBlank()) {
            adicionarMensagemErro("Preencha dia, categoria, inicio e fim do horario.");
            return false;
        }

        LocalTime inicio;
        LocalTime fim;
        try {
            inicio = LocalTime.parse(novaDisp.getHoraInicio());
            fim = LocalTime.parse(novaDisp.getHoraFim());
        } catch (DateTimeParseException e) {
            adicionarMensagemErro("Informe horarios validos no formato HH:mm.");
            return false;
        }

        if (!fim.isAfter(inicio)) {
            adicionarMensagemErro("O horario final deve ser maior que o horario inicial.");
            return false;
        }

        boolean horarioDuplicado = timeDTO.getDisponibilidades().stream()
                .anyMatch(d -> mesmoDiaSemana(d.getDiaSemana(), novaDisp.getDiaSemana())
                        && d.getCategoria() == novaDisp.getCategoria());
        if (horarioDuplicado) {
            adicionarMensagemErro("Ja existe um horario para este dia da semana e categoria.");
            return false;
        }

        if ("MANDO".equalsIgnoreCase(timeDTO.getMandoCampo())) {
            long minutos = Duration.between(inicio, fim).toMinutes();
            if (minutos > 120) {
                adicionarMensagemErro("Times mandantes podem cadastrar intervalos de no maximo 2 horas.");
                return false;
            }
        }

        return true;
    }

    private boolean mesmoDiaSemana(String primeiroDia, String segundoDia) {
        return normalizarDiaSemana(primeiroDia).equals(normalizarDiaSemana(segundoDia));
    }

    private String normalizarDiaSemana(String diaSemana) {
        if (diaSemana == null) {
            return "";
        }

        return Normalizer.normalize(diaSemana.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private void adicionarMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atencao", mensagem));
    }

    public void removerHorario(DisponibilidadeDTO item) {
        timeDTO.getDisponibilidades().remove(item);
    }

    public String salvar() {
        // 1. Validação de Senhas Iguais (Frontend Logic)
        if (timeDTO.getSenha() == null || !timeDTO.getSenha().equals(timeDTO.getConfirmarSenha())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A senha e a confirmação não conferem."));
            return null; // Fica na tela
        }

        try {
            // 2. Tenta salvar na API
            timeClient.salvarTime(timeDTO);
            
            // Sucesso
            return "login.xhtml?faces-redirect=true";
            
        } catch (HttpClientErrorException e) {
            // 3. Captura erro de validação da API (409 Conflict)
            String msgErro = "Erro ao salvar.";
            
            // Tenta extrair a mensagem "E-mail já cadastrado" do backend
            if (e.getStatusCode().value() == 409) {
                // Geralmente vem no body ou message
                msgErro = e.getResponseBodyAsString(); 
                // Limpeza básica se vier um JSON de erro do Spring
                if(msgErro.contains("message")) {
                   // Simplificando para exibir mensagem genérica ou tratar JSON
                   msgErro = "E-mail ou CPF já cadastrados."; 
                }
            }
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atenção", msgErro));
            return null;
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha técnica: " + e.getMessage()));
            return null;
        }
    }
}
