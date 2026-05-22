package br.com.arenamatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private JogoRepository jogoRepository;

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        matchService = new MatchService(timeRepository, jogoRepository,
                new MatchFiltroService(), new MatchResultadoService());
        when(jogoRepository.existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
                anyLong(), anyLong(), org.mockito.ArgumentMatchers.any(LocalDate.class), anyList()))
                .thenReturn(false);
    }

    @Test
    void cruzaHorarioComTimeDoPapelInverso() {
        Time mandante = time(1L, "Mandante", "MANDO",
                disponibilidade("Domingo", "10:00", "12:00"));
        Time visitante = time(2L, "Visitante", "VISITANTE",
                disponibilidade("Domingo", "11:00", "13:00"));

        when(timeRepository.findById(mandante.getId())).thenReturn(Optional.of(mandante));
        when(timeRepository.findAllOutrosTimesComDisponibilidade(mandante.getId())).thenReturn(List.of(visitante));

        List<ResultadoBuscaDTO> resultados = matchService.buscarAdversarios(mandante.getId(), new BuscaFiltroDTO());

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getNomeTime()).isEqualTo("Visitante");
        assertThat(resultados.get(0).getHorario()).isEqualTo("11:00 - 12:00");
    }

    @Test
    void naoRetornaTimeDoMesmoPapelOuDiaDiferente() {
        Time mandante = time(1L, "Mandante", "MANDO",
                disponibilidade("Domingo", "10:00", "12:00"));
        Time outroMandante = time(2L, "Outro mandante", "MANDO",
                disponibilidade("Domingo", "10:00", "12:00"));
        Time visitanteSabado = time(3L, "Visitante sabado", "VISITANTE",
                disponibilidade("Sabado", "10:00", "12:00"));

        when(timeRepository.findById(mandante.getId())).thenReturn(Optional.of(mandante));
        when(timeRepository.findAllOutrosTimesComDisponibilidade(mandante.getId()))
                .thenReturn(List.of(outroMandante, visitanteSabado));

        assertThat(matchService.buscarAdversarios(mandante.getId(), new BuscaFiltroDTO())).isEmpty();
    }

    @Test
    void usaEnderecoETaxaDoMandanteQuandoVisitanteBusca() {
        Time visitante = time(1L, "Visitante", "VISITANTE",
                disponibilidade("Quarta", "19:00", "21:00"));
        Time mandante = time(2L, "Mandante", "MANDO",
                disponibilidade("Quarta", "19:30", "20:30"));
        mandante.setTaxaJogo(new BigDecimal("90.00"));
        mandante.setLogradouro("Rua do Campo");
        mandante.setNumero("10");
        mandante.setCidade("Sao Paulo");
        mandante.setUf("SP");

        when(timeRepository.findById(visitante.getId())).thenReturn(Optional.of(visitante));
        when(timeRepository.findAllOutrosTimesComDisponibilidade(visitante.getId())).thenReturn(List.of(mandante));

        ResultadoBuscaDTO resultado = matchService.buscarAdversarios(visitante.getId(), new BuscaFiltroDTO()).get(0);

        assertThat(resultado.getMandoCampo()).isEqualTo("MANDO");
        assertThat(resultado.getTaxaJogo()).isEqualByComparingTo("90.00");
        assertThat(resultado.getEnderecoJogo()).isEqualTo("Rua do Campo, 10, Sao Paulo, SP");
    }

    private Time time(Long id, String nome, String mandoCampo, Disponibilidade disponibilidade) {
        Time time = new Time();
        time.setId(id);
        time.setNomeTime(nome);
        time.setMandoCampo(mandoCampo);
        time.setDisponibilidades(List.of(disponibilidade));
        return time;
    }

    private Disponibilidade disponibilidade(String dia, String inicio, String fim) {
        Disponibilidade disponibilidade = new Disponibilidade();
        disponibilidade.setDiaSemana(dia);
        disponibilidade.setHoraInicio(inicio);
        disponibilidade.setHoraFim(fim);
        disponibilidade.setCategoria(Categoria.ESPORTE);
        return disponibilidade;
    }
}
