package br.com.arenamatch.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final TimeRepository timeRepository;
    private final MatchFiltroService matchFiltroService;
    private final MatchResultadoService matchResultadoService;

    public List<ResultadoBuscaDTO> buscarAdversarios(Long idTimeBuscando, BuscaFiltroDTO filtro) {
        Time timeBuscando = timeRepository.findById(idTimeBuscando)
                .orElseThrow(() -> new RuntimeException("Time logado nao encontrado"));

        List<Time> outrosTimes = timeRepository.findAllOutrosTimesComDisponibilidade(idTimeBuscando);
        List<ResultadoBuscaDTO> resultados = new ArrayList<>();

        for (Time adversario : outrosTimes) {
            double distancia = matchResultadoService.calcularDistancia(timeBuscando, adversario);
            if (!matchFiltroService.timeAtende(adversario, distancia, filtro)) {
                continue;
            }

            adicionarDisponibilidadesValidas(resultados, adversario, distancia, filtro);
        }

        resultados.sort((primeiro, segundo) -> Double.compare(primeiro.getDistancia(), segundo.getDistancia()));
        return resultados;
    }

    private void adicionarDisponibilidadesValidas(List<ResultadoBuscaDTO> resultados, Time adversario,
            double distancia, BuscaFiltroDTO filtro) {
        for (Disponibilidade disponibilidade : adversario.getDisponibilidades()) {
            if (matchFiltroService.disponibilidadeAtende(disponibilidade, filtro)) {
                resultados.add(matchResultadoService.criarResultado(adversario, disponibilidade, distancia));
            }
        }
    }
}
