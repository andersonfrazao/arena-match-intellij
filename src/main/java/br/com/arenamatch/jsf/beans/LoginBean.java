package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.AuthClient;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.Serializable;

@Component
@ViewScoped
public class LoginBean implements Serializable {

    @Autowired
    private AuthClient authClient;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String senha;

    public String fazerLogin() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(this.email);
        dto.setSenha(this.senha);

        Time timeLogado;
        try {
            timeLogado = authClient.login(dto);
        } catch (HttpClientErrorException.Forbidden e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conta desativada",
                            "A conta foi desativada. Entre em contato pelo e-mail arenamatch.app@gmail.com."));
            return null;
        }

        if (timeLogado != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("timeLogado", timeLogado);
            
            // CORREÇÃO: Removido o prefixo "dashboard/"
            return "agenda.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro:", "Usuário ou senha inválidos."));
            return null;
        }
    }
}
