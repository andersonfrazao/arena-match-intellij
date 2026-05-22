package br.com.arenamatch.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.JogoDTO;
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
    private final JogoValidacaoService jogoValidacaoService;
    private final JogoDTOService jogoDTOService;

    @Transactional
    public JogoDTO enviarConvite(JogoDTO dto) {
        jogoValidacaoService.validarDadosConvite(dto);

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
        Time solicitante = timeRepository.findById(dto.getIdSolicitante())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time solicitante nao encontrado."));

        jogoValidacaoService.validarDisponibilidades(mandante, visitante, dto);

        Jogo jogo = Jogo.builder()
                .timeMandante(mandante)
                .timeVisitante(visitante)
                .timeSolicitante(solicitante)
                .dataJogo(dto.getDataJogo())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .status(StatusJogo.PENDENTE)
                .build();

        return jogoDTOService.converterParaDTO(jogoRepository.save(jogo));
    }

    @Transactional
    public JogoDTO responderConvite(Long jogoId, StatusJogo novoStatus) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo nao encontrado."));

        jogoValidacaoService.validarRespostaConvite(jogo, novoStatus);

        jogo.setStatus(novoStatus);
        return jogoDTOService.converterParaDTO(jogoRepository.save(jogo));
    }

    public List<JogoDTO> listarJogosDoTime(Long timeId) {
        return jogoRepository.findJogosByTimeId(timeId).stream()
                .map(jogoDTOService::converterParaDTO)
                .collect(Collectors.toList());
    }
}
