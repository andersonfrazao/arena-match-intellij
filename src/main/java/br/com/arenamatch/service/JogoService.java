package br.com.arenamatch.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Jogo;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusJogo;
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final TimeRepository timeRepository;

    @Transactional
    public JogoDTO enviarConvite(JogoDTO dto) {
        validarDadosConvite(dto);

        boolean jaExiste = jogoRepository.existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
                dto.getIdMandante(), dto.getIdVisitante(), dto.getDataJogo(),
                Arrays.asList(StatusJogo.PENDENTE, StatusJogo.CONFIRMADO));

        if (jaExiste) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voce ja enviou um convite para este time nesta data.");
        }

        Time mandante = timeRepository.findById(dto.getIdMandante())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mandante nao encontrado."));
        Time visitante = timeRepository.findById(dto.getIdVisitante())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitante nao encontrado."));

        validarDisponibilidadeVisitante(visitante, dto);

        Jogo jogo = Jogo.builder()
                .timeMandante(mandante)
                .timeVisitante(visitante)
                .dataJogo(dto.getDataJogo())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .status(StatusJogo.PENDENTE)
                .build();

        return converterParaDTO(jogoRepository.save(jogo));
    }

    @Transactional
    public JogoDTO responderConvite(Long jogoId, StatusJogo novoStatus) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo nao encontrado."));

        validarRespostaConvite(jogo, novoStatus);

        jogo.setStatus(novoStatus);
        return converterParaDTO(jogoRepository.save(jogo));
    }

    public List<JogoDTO> listarJogosDoTime(Long timeId) {
        return jogoRepository.findJogosByTimeId(timeId).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private void validarDadosConvite(JogoDTO dto) {
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

    private void validarDisponibilidadeVisitante(Time visitante, JogoDTO dto) {
        DayOfWeek diaJogo = dto.getDataJogo().getDayOfWeek();
        boolean disponibilidadeEncontrada = visitante.getDisponibilidades() != null
                && visitante.getDisponibilidades().stream()
                        .anyMatch(disponibilidade -> disponibilidadeConfere(disponibilidade, diaJogo, dto));

        if (!disponibilidadeEncontrada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O visitante nao possui disponibilidade para a data e horario informados.");
        }
    }

    private boolean disponibilidadeConfere(Disponibilidade disponibilidade, DayOfWeek diaJogo, JogoDTO dto) {
        return obterDiaSemana(disponibilidade.getDiaSemana()) == diaJogo.getValue()
                && disponibilidade.getHoraInicio().equals(dto.getHoraInicio())
                && disponibilidade.getHoraFim().equals(dto.getHoraFim());
    }

    private int obterDiaSemana(String diaSemanaPt) {
        return switch (diaSemanaPt.toLowerCase()) {
            case "domingo" -> 7;
            case "segunda" -> 1;
            case "terça", "terca" -> 2;
            case "quarta" -> 3;
            case "quinta" -> 4;
            case "sexta" -> 5;
            case "sábado", "sabado" -> 6;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dia da semana invalido: " + diaSemanaPt);
        };
    }

    private void validarRespostaConvite(Jogo jogo, StatusJogo novoStatus) {
        if (novoStatus != StatusJogo.CONFIRMADO && novoStatus != StatusJogo.RECUSADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status invalido para resposta de convite.");
        }
        if (jogo.getStatus() != StatusJogo.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas convites pendentes podem ser respondidos.");
        }
    }

    private JogoDTO converterParaDTO(Jogo jogo) {
        JogoDTO dto = new JogoDTO();
        dto.setId(jogo.getId());
        dto.setIdMandante(jogo.getTimeMandante().getId());
        dto.setNomeMandante(jogo.getTimeMandante().getNomeTime());
        dto.setIdVisitante(jogo.getTimeVisitante().getId());
        dto.setNomeVisitante(jogo.getTimeVisitante().getNomeTime());
        dto.setDataJogo(jogo.getDataJogo());
        dto.setHoraInicio(jogo.getHoraInicio());
        dto.setHoraFim(jogo.getHoraFim());
        dto.setStatus(jogo.getStatus());
        return dto;
    }
}
