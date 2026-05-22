package br.com.arenamatch.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusJogo;
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final TimeRepository timeRepository;
    private final JogoRepository jogoRepository;
    private final MatchFiltroService matchFiltroService;
    private final MatchResultadoService matchResultadoService;

    public List<ResultadoBuscaDTO> buscarAdversarios(Long idTimeBuscando, BuscaFiltroDTO filtro) {
        Time timeBuscando = timeRepository.findById(idTimeBuscando)
                .orElseThrow(() -> new RuntimeException("Time logado nao encontrado"));

        List<Time> outrosTimes = timeRepository.findAllOutrosTimesComDisponibilidade(idTimeBuscando);
        List<ResultadoBuscaDTO> resultados = new ArrayList<>();

        for (Time adversario : outrosTimes) {
            double distancia = matchResultadoService.calcularDistancia(timeBuscando, adversario);
            if (!mandoCampoInverso(timeBuscando, adversario)
                    || !matchFiltroService.distanciaAtende(distancia, filtro)) {
                continue;
            }

            adicionarDisponibilidadesValidas(resultados, timeBuscando, adversario, distancia, filtro);
        }

        resultados.sort((primeiro, segundo) -> Double.compare(primeiro.getDistancia(), segundo.getDistancia()));
        return resultados;
    }

    private void adicionarDisponibilidadesValidas(List<ResultadoBuscaDTO> resultados, Time timeBuscando,
            Time adversario, double distancia, BuscaFiltroDTO filtro) {
        for (Disponibilidade disponibilidadeLogado : timeBuscando.getDisponibilidades()) {
            if (!matchFiltroService.disponibilidadeAtende(disponibilidadeLogado, filtro)) {
                continue;
            }
            for (Disponibilidade disponibilidadeAdversario : adversario.getDisponibilidades()) {
                if (disponibilidadesCoincidem(disponibilidadeLogado, disponibilidadeAdversario)) {
                    resultados.add(criarResultado(timeBuscando, adversario, disponibilidadeAdversario,
                            disponibilidadeLogado, distancia));
                }
            }
        }
    }

    private ResultadoBuscaDTO criarResultado(Time timeBuscando, Time adversario,
            Disponibilidade disponibilidadeAdversario, Disponibilidade disponibilidadeLogado, double distancia) {
        Time mandante = temMando(timeBuscando) ? timeBuscando : adversario;
        String horaInicio = matchResultadoService.maiorHoraInicio(disponibilidadeLogado, disponibilidadeAdversario);
        String horaFim = matchResultadoService.menorHoraFim(disponibilidadeLogado, disponibilidadeAdversario);

        ResultadoBuscaDTO resultado = matchResultadoService.criarResultado(adversario, mandante,
                disponibilidadeAdversario, horaInicio, horaFim, distancia, false);
        Long visitanteId = visitante(timeBuscando, adversario).getId();
        resultado.setConvitePendente(jogoRepository.existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
                mandante.getId(), visitanteId, resultado.getDataExata(), List.of(StatusJogo.PENDENTE)));
        resultado.setJogoIndisponivel(jogoRepository.existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
                mandante.getId(), visitanteId, resultado.getDataExata(), List.of(StatusJogo.CONFIRMADO)));
        return resultado;
    }

    private boolean disponibilidadesCoincidem(Disponibilidade primeira, Disponibilidade segunda) {
        return mesmoDia(primeira, segunda)
                && primeira.getCategoria() == segunda.getCategoria()
                && matchResultadoService.horariosCoincidem(primeira, segunda);
    }

    private boolean mesmoDia(Disponibilidade primeira, Disponibilidade segunda) {
        return primeira.getDiaSemana().equalsIgnoreCase(segunda.getDiaSemana());
    }

    private boolean mandoCampoInverso(Time timeBuscando, Time adversario) {
        return temMando(timeBuscando) != temMando(adversario);
    }

    private boolean temMando(Time time) {
        return "MANDO".equalsIgnoreCase(time.getMandoCampo());
    }

    private Time visitante(Time timeBuscando, Time adversario) {
        return temMando(timeBuscando) ? adversario : timeBuscando;
    }
}
