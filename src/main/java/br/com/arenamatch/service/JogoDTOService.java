package br.com.arenamatch.service;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.entity.Jogo;

@Service
public class JogoDTOService {

    public JogoDTO converterParaDTO(Jogo jogo) {
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
