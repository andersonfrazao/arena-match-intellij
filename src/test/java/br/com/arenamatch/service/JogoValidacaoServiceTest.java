package br.com.arenamatch.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.JogoDTO;

class JogoValidacaoServiceTest {

    private final JogoValidacaoService jogoValidacaoService = new JogoValidacaoService();

    @Test
    void rejeitaConviteComDataPassada() {
        JogoDTO convite = new JogoDTO();
        convite.setIdMandante(1L);
        convite.setIdVisitante(2L);
        convite.setIdSolicitante(1L);
        convite.setDataJogo(LocalDate.now().minusDays(1));
        convite.setHoraInicio("19:00");
        convite.setHoraFim("20:00");

        assertThatThrownBy(() -> jogoValidacaoService.validarDadosConvite(convite))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    org.assertj.core.api.Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    org.assertj.core.api.Assertions.assertThat(exception.getReason()).contains("data passada");
                });
    }
}
