package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import br.com.arenamatch.client.JogoClient;
import br.com.arenamatch.client.MatchClient;
import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Categoria;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Data;

@Component
@ViewScoped
@Data
public class BuscaBean implements Serializable {

    @Autowired
    private MatchClient matchClient;
    @Autowired
    private JogoClient jogoClient;

    private BuscaFiltroDTO filtro;
    private List<ResultadoBuscaDTO> resultados;
    private Time timeLogado;
    private final Categoria[] categorias = Categoria.values();

    @PostConstruct
    public void init() {
        filtro = new BuscaFiltroDTO();
        resultados = new ArrayList<>();
        timeLogado = (Time) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("timeLogado");

        pesquisar();
    }

    public void pesquisar() {
        if (timeLogado != null) {
            resultados = matchClient.buscar(timeLogado.getId(), filtro);
        }
    }

    public void limpar() {
        filtro = new BuscaFiltroDTO();
        pesquisar();
    }

    public void convidar(ResultadoBuscaDTO timeAlvo) {
        try {
            JogoDTO convite = new JogoDTO();
            convite.setIdMandante(timeLogado.getId());
            convite.setIdVisitante(timeAlvo.getIdTime());
            convite.setDataJogo(timeAlvo.getDataExata());
            convite.setHoraInicio(timeAlvo.getHoraInicio());
            convite.setHoraFim(timeAlvo.getHoraFim());

            jogoClient.enviarConvite(convite);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                            "Convite enviado para " + timeAlvo.getNomeTime() + " no dia " + timeAlvo.getDataExataFormatada()));

        } catch (HttpClientErrorException.Conflict e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencao", "Voce ja enviou um convite para este time nesta data."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao enviar convite."));
        }
    }
}
