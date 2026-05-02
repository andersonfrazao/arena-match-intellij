package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.arenamatch.client.JogoClient;
import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusJogo;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Data;

@Component
@ViewScoped
@Data
public class AgendaBean implements Serializable {

    @Autowired private JogoClient jogoClient; // <-- NOVO

    private Time timeLogado;
    private List<DiaAgenda> diasSemana;
    private DiaAgenda diaSelecionado;
    
    // Lista com todos os jogos (pendentes e confirmados)
    private List<JogoDTO> meusJogos = new ArrayList<>();

    @PostConstruct
    public void init() {
        timeLogado = (Time) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("timeLogado");
        if (timeLogado == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
                return;
            } catch (Exception e) { e.printStackTrace(); }
        }
        carregarDados();
    }

    public void carregarDados() {
        // Vai buscar os jogos reais à API
        meusJogos = jogoClient.listarPorTime(timeLogado.getId());
        montarCalendario();
    }

    private void montarCalendario() {
        diasSemana = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        Locale localeBR = new Locale("pt", "BR");

        for (int i = 0; i < 7; i++) {
            LocalDate data = hoje.plusDays(i);
            String dow = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, localeBR);
            String num = String.format("%02d", data.getDayOfMonth());
            String titulo = data.getDayOfWeek().getDisplayName(TextStyle.FULL, localeBR) + " (" + data.format(DateTimeFormatter.ofPattern("dd/MM")) + ")";
            
            // <-- LÓGICA REAL: Verifica se há jogos nesta data
            boolean temConfirmado = meusJogos.stream().anyMatch(j -> j.getDataJogo().equals(data) && j.getStatus() == StatusJogo.CONFIRMADO);
            boolean temPendente = meusJogos.stream().anyMatch(j -> j.getDataJogo().equals(data) && j.getStatus() == StatusJogo.PENDENTE);

            String tipo = "none";
            if (temConfirmado) tipo = "game";
            else if (temPendente) tipo = "invite";

            diasSemana.add(new DiaAgenda(data, dow, num, titulo, tipo));
        }

        // Mantém a seleção atual ou seleciona o primeiro dia
        if (diaSelecionado != null) {
            diaSelecionado = diasSemana.stream().filter(d -> d.getDataCompleta().equals(diaSelecionado.getDataCompleta())).findFirst().orElse(diasSemana.get(0));
        } else if (!diasSemana.isEmpty()) {
            selecionarDia(diasSemana.get(0));
        }
    }

    public void selecionarDia(DiaAgenda dia) {
        this.diaSelecionado = dia;
    }

    // <-- MÉTODO NOVO: Obtém apenas os jogos do dia em que o utilizador clicou
    public List<JogoDTO> getJogosDoDiaSelecionado() {
        if (diaSelecionado == null || meusJogos == null) return new ArrayList<>();
        return meusJogos.stream()
                .filter(j -> j.getDataJogo().equals(diaSelecionado.getDataCompleta()))
                .filter(j -> j.getStatus() == StatusJogo.CONFIRMADO || j.getStatus() == StatusJogo.PENDENTE)
                .collect(Collectors.toList());
    }

    // <-- MÉTODO NOVO: Ação de Aceitar/Recusar o convite
    public void responderConvite(JogoDTO jogo, String statusString) {
        try {
            StatusJogo novoStatus = StatusJogo.valueOf(statusString);
            jogoClient.responderConvite(jogo.getId(), novoStatus);
            carregarDados(); // Recarrega para atualizar as bolinhas do calendário
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Convite " + statusString.toLowerCase() + "."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível responder."));
        }
    }

    // Classe auxiliar mantida igual
    @Data
    public static class DiaAgenda {
        private LocalDate dataCompleta;
        private String diaSemanaCurto; 
        private String numeroDia;      
        private String tituloDetalhe;  
        private String tipoMarcador;   
        public DiaAgenda(LocalDate dataCompleta, String diaSemanaCurto, String numeroDia, String tituloDetalhe, String tipoMarcador) {
            this.dataCompleta = dataCompleta; this.diaSemanaCurto = diaSemanaCurto; this.numeroDia = numeroDia; this.tituloDetalhe = tituloDetalhe; this.tipoMarcador = tipoMarcador;
        }
    }
}