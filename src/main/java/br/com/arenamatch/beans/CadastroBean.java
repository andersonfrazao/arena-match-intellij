package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import br.com.arenamatch.client.TimeClient;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.TimeDTO;
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

    @PostConstruct
    public void init() {
        timeDTO.setDisponibilidades(new ArrayList<>());
    }

    public void next() { if(step < 2) step++; }
    public void back() { if(step > 0) step--; }
    public void setStep(int s) { this.step = s; }

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
        DisponibilidadeDTO d = new DisponibilidadeDTO();
        d.setDiaSemana(novaDisp.getDiaSemana());
        d.setCategoria(novaDisp.getCategoria());
        d.setHoraInicio(novaDisp.getHoraInicio());
        d.setHoraFim(novaDisp.getHoraFim());
        
        timeDTO.getDisponibilidades().add(d);
        novaDisp = new DisponibilidadeDTO(); 
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