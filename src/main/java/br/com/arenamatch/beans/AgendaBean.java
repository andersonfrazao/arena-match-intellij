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

    private static final int DIAS_PERIODO = 15;

    @Autowired private JogoClient jogoClient;

    private Time timeLogado;
    private List<DiaAgenda> diasSemana;
    private DiaAgenda diaSelecionado;
    private LocalDate inicioPeriodo;

    private List<JogoDTO> meusJogos = new ArrayList<>();

    @PostConstruct
    public void init() {
        timeLogado = (Time) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("timeLogado");
        if (timeLogado == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        carregarDados();
    }

    public void carregarDados() {
        meusJogos = jogoClient.listarPorTime(timeLogado.getId());
        if (inicioPeriodo == null) {
            inicioPeriodo = LocalDate.now();
        }
        montarCalendario();
    }

    private void montarCalendario() {
        diasSemana = new ArrayList<>();
        LocalDate dataInicial = getInicioPeriodo();
        Locale localeBR = new Locale("pt", "BR");

        for (int i = 0; i < DIAS_PERIODO; i++) {
            LocalDate data = dataInicial.plusDays(i);
            String dow = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, localeBR);
            String num = String.format("%02d", data.getDayOfMonth());
            String titulo = data.getDayOfWeek().getDisplayName(TextStyle.FULL, localeBR) + " (" + data.format(DateTimeFormatter.ofPattern("dd/MM")) + ")";

            boolean temConfirmado = meusJogos.stream().anyMatch(j -> j.getDataJogo().equals(data) && j.getStatus() == StatusJogo.CONFIRMADO);
            boolean temPendente = meusJogos.stream().anyMatch(j -> j.getDataJogo().equals(data) && j.getStatus() == StatusJogo.PENDENTE);

            String tipo = "none";
            if (temConfirmado) {
                tipo = "game";
            } else if (temPendente) {
                tipo = "invite";
            }

            diasSemana.add(new DiaAgenda(data, dow, num, titulo, tipo));
        }

        if (diaSelecionado != null) {
            diaSelecionado = diasSemana.stream()
                    .filter(d -> d.getDataCompleta().equals(diaSelecionado.getDataCompleta()))
                    .findFirst()
                    .orElse(diasSemana.get(0));
        } else if (!diasSemana.isEmpty()) {
            selecionarDia(diasSemana.get(0));
        }
    }

    public void selecionarDia(DiaAgenda dia) {
        this.diaSelecionado = dia;
    }

    public void periodoAnterior() {
        inicioPeriodo = getInicioPeriodo().minusDays(DIAS_PERIODO);
        montarCalendario();
    }

    public void proximoPeriodo() {
        inicioPeriodo = getInicioPeriodo().plusDays(DIAS_PERIODO);
        montarCalendario();
    }

    public void periodoAtual() {
        inicioPeriodo = LocalDate.now();
        diaSelecionado = null;
        montarCalendario();
    }

    public LocalDate getInicioPeriodo() {
        if (inicioPeriodo == null) {
            inicioPeriodo = LocalDate.now();
        }
        return inicioPeriodo;
    }

    public String getTituloPeriodo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate inicio = getInicioPeriodo();
        return inicio.format(formatter) + " a " + inicio.plusDays(DIAS_PERIODO - 1).format(formatter);
    }

    public List<JogoDTO> getJogosDoDiaSelecionado() {
        if (diaSelecionado == null || meusJogos == null) {
            return new ArrayList<>();
        }
        return meusJogos.stream()
                .filter(j -> j.getDataJogo().equals(diaSelecionado.getDataCompleta()))
                .filter(j -> j.getStatus() == StatusJogo.CONFIRMADO || j.getStatus() == StatusJogo.PENDENTE)
                .collect(Collectors.toList());
    }

    public void responderConvite(JogoDTO jogo, String statusString) {
        try {
            StatusJogo novoStatus = StatusJogo.valueOf(statusString);
            jogoClient.responderConvite(jogo.getId(), novoStatus);
            carregarDados();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Convite " + statusString.toLowerCase() + "."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nao foi possivel responder."));
        }
    }

    @Data
    public static class DiaAgenda {
        private LocalDate dataCompleta;
        private String diaSemanaCurto;
        private String numeroDia;
        private String tituloDetalhe;
        private String tipoMarcador;

        public DiaAgenda(LocalDate dataCompleta, String diaSemanaCurto, String numeroDia, String tituloDetalhe, String tipoMarcador) {
            this.dataCompleta = dataCompleta;
            this.diaSemanaCurto = diaSemanaCurto;
            this.numeroDia = numeroDia;
            this.tituloDetalhe = tituloDetalhe;
            this.tipoMarcador = tipoMarcador;
        }
    }
}
