package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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

    @PostConstruct
    public void init() {
        filtro = new BuscaFiltroDTO();
        resultados = new ArrayList<>();
        timeLogado = (Time) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("timeLogado");
        
        // Pesquisa inicial automática com padrão 10km
        pesquisar();
    }

    public void pesquisar() {
        if (timeLogado != null) {
            resultados = matchClient.buscar(timeLogado.getId(), filtro);
            
            // --- NOVIDADE: Calcula a data exata para cada resultado exibido ---
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (ResultadoBuscaDTO res : resultados) {
                LocalDate data = calcularProximaData(res.getDiaSemana());
                res.setDataExata(data);
                res.setDataExataFormatada(data.format(formatter));
            }
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
            
            // Agora usamos a data exata que já foi calculada!
            convite.setDataJogo(timeAlvo.getDataExata());
            
            String[] horas = timeAlvo.getHorario().split(" - ");
            convite.setHoraInicio(horas[0].trim());
            convite.setHoraFim(horas[1].trim());

            jogoClient.enviarConvite(convite);

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Convite enviado para " + timeAlvo.getNomeTime() + " no dia " + timeAlvo.getDataExataFormatada()));
                
        } catch (HttpClientErrorException.Conflict e) {
            // Captura o erro 409 (Conflito) que criamos no backend para convites duplicados
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Você já enviou um convite para este time nesta data."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao enviar convite."));
        }
    }

    // <-- MÉTODO AUXILIAR NOVO
    private LocalDate calcularProximaData(String diaSemanaPt) {
        int targetDay = 1;
        switch (diaSemanaPt.toLowerCase()) {
            case "domingo": targetDay = 7; break;
            case "segunda": targetDay = 1; break;
            case "terça":   targetDay = 2; break;
            case "quarta":  targetDay = 3; break;
            case "quinta":  targetDay = 4; break;
            case "sexta":   targetDay = 5; break;
            case "sábado":  targetDay = 6; break;
        }
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.of(targetDay)));
    }
}