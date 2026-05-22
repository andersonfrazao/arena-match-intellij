package br.com.arenamatch.service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Jogo;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusJogo;

@Service
public class JogoValidacaoService {

    public void validarDadosConvite(JogoDTO dto) {
        if (dto.getIdMandante() == null || dto.getIdVisitante() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mandante e visitante sao obrigatorios.");
        }
        if (dto.getIdMandante().equals(dto.getIdVisitante())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e permitido convidar o proprio time.");
        }
        if (dto.getDataJogo() == null || dto.getHoraInicio() == null || dto.getHoraInicio().isBlank()
                || dto.getHoraFim() == null || dto.getHoraFim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data e horario do jogo sao obrigatorios.");
        }
        if (dto.getDataJogo().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e permitido enviar convite para data passada.");
        }
    }

    public void validarDisponibilidadeVisitante(Time visitante, JogoDTO dto) {
        DayOfWeek diaJogo = dto.getDataJogo().getDayOfWeek();
        boolean disponibilidadeEncontrada = visitante.getDisponibilidades() != null
                && visitante.getDisponibilidades().stream()
                        .anyMatch(disponibilidade -> disponibilidadeConfere(disponibilidade, diaJogo, dto));

        if (!disponibilidadeEncontrada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O visitante nao possui disponibilidade para a data e horario informados.");
        }
    }

    public void validarRespostaConvite(Jogo jogo, StatusJogo novoStatus) {
        if (novoStatus != StatusJogo.CONFIRMADO && novoStatus != StatusJogo.RECUSADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status invalido para resposta de convite.");
        }
        if (jogo.getStatus() != StatusJogo.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas convites pendentes podem ser respondidos.");
        }
    }

    private boolean disponibilidadeConfere(Disponibilidade disponibilidade, DayOfWeek diaJogo, JogoDTO dto) {
        return obterDiaSemana(disponibilidade.getDiaSemana()) == diaJogo.getValue()
                && disponibilidade.getHoraInicio().equals(dto.getHoraInicio())
                && disponibilidade.getHoraFim().equals(dto.getHoraFim());
    }

    private int obterDiaSemana(String diaSemanaPt) {
        return switch (normalizar(diaSemanaPt)) {
            case "domingo" -> 7;
            case "segunda" -> 1;
            case "terca" -> 2;
            case "quarta" -> 3;
            case "quinta" -> 4;
            case "sexta" -> 5;
            case "sabado" -> 6;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dia da semana invalido: " + diaSemanaPt);
        };
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
