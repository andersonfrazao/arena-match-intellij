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

 // Importe: import org.springframework.web.server.ResponseStatusException;
    // Importe: import org.springframework.http.HttpStatus;
    // Importe: import java.util.Arrays;

    @Transactional
    public JogoDTO enviarConvite(JogoDTO dto) {
        // --- NOVA VALIDAÇÃO: Bloqueia duplicidade ---
        boolean jaExiste = jogoRepository.existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
                dto.getIdMandante(), dto.getIdVisitante(), dto.getDataJogo(), 
                Arrays.asList(StatusJogo.PENDENTE, StatusJogo.CONFIRMADO));

        if (jaExiste) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já enviou um convite para este time nesta data.");
        }
        // --------------------------------------------

        Time mandante = timeRepository.findById(dto.getIdMandante()).orElseThrow(() -> new RuntimeException("Mandante não encontrado"));
        Time visitante = timeRepository.findById(dto.getIdVisitante()).orElseThrow(() -> new RuntimeException("Visitante não encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
        
        jogo.setStatus(novoStatus);
        return converterParaDTO(jogoRepository.save(jogo));
    }

    public List<JogoDTO> listarJogosDoTime(Long timeId) {
        return jogoRepository.findJogosByTimeId(timeId).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
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