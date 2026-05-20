package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import br.com.arenamatch.client.JogoClient;
import br.com.arenamatch.client.TimeClient;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.StatusJogo;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Getter;
import lombok.Setter;

@Component
@ViewScoped
public class AlteracaoCadastroBean implements Serializable {

    @Autowired private TimeClient timeClient;
    @Autowired private JogoClient jogoClient;

    @Getter @Setter private TimeDTO timeDTO = new TimeDTO();
    @Getter @Setter private DisponibilidadeDTO novaDisp = new DisponibilidadeDTO();
    @Getter private final Categoria[] categorias = Categoria.values();
    @Getter private boolean localBloqueado;

    private Time timeLogado;

    @PostConstruct
    public void init() {
        timeLogado = (Time) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("timeLogado");
        if (timeLogado == null) {
            redirecionarLogin();
            return;
        }

        timeDTO = timeClient.buscarPorId(timeLogado.getId());
        if (timeDTO.getDisponibilidades() == null) {
            timeDTO.setDisponibilidades(new ArrayList<>());
        }
        localBloqueado = existeJogoNaoRealizado();
    }

    public String getCpfOculto() {
        String cpf = timeDTO.getCpf();
        if (cpf == null || cpf.isBlank()) {
            return "***.***.***-**";
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() < 11) {
            return "***.***.***-**";
        }
        return "***.***.***-" + digitos.substring(9);
    }

    public void buscarCep() {
        if (localBloqueado) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Local bloqueado",
                    "Ainda existe jogo a ser realizado. O local so podera ser alterado depois que o jogo acontecer.");
            return;
        }

        if (timeDTO.getCep() != null && timeDTO.getCep().replaceAll("\\D", "").length() >= 8) {
            TimeDTO dados = timeClient.buscarEnderecoPorCep(timeDTO.getCep());
            if (dados != null) {
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

    public void removerHorario(DisponibilidadeDTO item) {
        timeDTO.getDisponibilidades().remove(item);
    }

    public String salvar() {
        try {
            Time timeAtualizado = timeClient.atualizarTime(timeLogado.getId(), timeDTO);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("timeLogado", timeAtualizado);
            timeLogado = timeAtualizado;
            timeDTO = timeClient.buscarPorId(timeAtualizado.getId());
            localBloqueado = existeJogoNaoRealizado();

            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Cadastro atualizado", "As alteracoes foram salvas.");
            return null;
        } catch (HttpClientErrorException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Nao foi possivel salvar", extrairMensagem(e));
            return null;
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Falha tecnica: " + e.getMessage());
            return null;
        }
    }

    private boolean validarHorario() {
        if (novaDisp.getDiaSemana() == null || novaDisp.getDiaSemana().isBlank()
                || novaDisp.getCategoria() == null
                || novaDisp.getHoraInicio() == null || novaDisp.getHoraInicio().isBlank()
                || novaDisp.getHoraFim() == null || novaDisp.getHoraFim().isBlank()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Atencao", "Preencha dia, categoria, inicio e fim do horario.");
            return false;
        }

        LocalTime inicio;
        LocalTime fim;
        try {
            inicio = LocalTime.parse(novaDisp.getHoraInicio());
            fim = LocalTime.parse(novaDisp.getHoraFim());
        } catch (DateTimeParseException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Atencao", "Informe horarios validos no formato HH:mm.");
            return false;
        }

        if (!fim.isAfter(inicio)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Atencao", "O horario final deve ser maior que o horario inicial.");
            return false;
        }

        boolean horarioDuplicado = timeDTO.getDisponibilidades().stream()
                .anyMatch(d -> d.getDiaSemana().equalsIgnoreCase(novaDisp.getDiaSemana())
                        && d.getCategoria() == novaDisp.getCategoria());
        if (horarioDuplicado) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Atencao", "Ja existe um horario para este dia da semana e categoria.");
            return false;
        }

        if ("MANDO".equalsIgnoreCase(timeDTO.getMandoCampo())) {
            long minutos = Duration.between(inicio, fim).toMinutes();
            if (minutos > 120) {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Atencao", "Times mandantes podem cadastrar intervalos de no maximo 2 horas.");
                return false;
            }
        }

        return true;
    }

    private boolean existeJogoNaoRealizado() {
        return jogoClient.listarPorTime(timeLogado.getId()).stream()
                .anyMatch(this::jogoNaoRealizado);
    }

    private boolean jogoNaoRealizado(JogoDTO jogo) {
        return !jogo.getDataJogo().isBefore(LocalDate.now())
                && (jogo.getStatus() == StatusJogo.PENDENTE || jogo.getStatus() == StatusJogo.CONFIRMADO);
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumo, detalhe));
    }

    private String extrairMensagem(HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();
        if (body != null && body.contains("Ainda existe jogo")) {
            return "Ainda existe jogo a ser realizado. O local so podera ser alterado depois que o jogo acontecer.";
        }
        if (body != null && !body.isBlank()) {
            return body;
        }
        return e.getMessage();
    }

    private void redirecionarLogin() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel redirecionar para o login.", e);
        }
    }
}
