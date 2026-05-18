package br.com.arenamatch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.util.GeoUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final TimeRepository timeRepository;

    public List<ResultadoBuscaDTO> buscarAdversarios(Long idTimeBuscando, BuscaFiltroDTO filtro) {
        Time timeBuscando = timeRepository.findById(idTimeBuscando)
                .orElseThrow(() -> new RuntimeException("Time logado não encontrado"));

        List<Time> outrosTimes = timeRepository.findAllOutrosTimesComDisponibilidade(idTimeBuscando);
        List<ResultadoBuscaDTO> resultados = new ArrayList<>();

        for (Time outro : outrosTimes) {
            // Calcula a distância real
            double distancia = 0.0;
            if (timeBuscando.getLatitude() != null && outro.getLatitude() != null) {
                distancia = GeoUtil.calcularDistancia(
                        timeBuscando.getLatitude(), timeBuscando.getLongitude(),
                        outro.getLatitude(), outro.getLongitude());
            }

            // Filtro de Distância
            if (distancia > filtro.getDistanciaKm()) {
                continue;
            }

            // Filtro de Cidade (se informado)
            if (filtro.getCidade() != null && !filtro.getCidade().isBlank()) {
                if (outro.getCidade() == null || !outro.getCidade().equalsIgnoreCase(filtro.getCidade())) {
                    continue;
                }
            }

            // Avalia as disponibilidades desse time
            for (Disponibilidade disp : outro.getDisponibilidades()) {
                // Filtro de Dia da Semana
                if (filtro.getDiaSemana() != null && !filtro.getDiaSemana().isBlank() && !filtro.getDiaSemana().equals("Qualquer")) {
                    if (!disp.getDiaSemana().equalsIgnoreCase(filtro.getDiaSemana())) continue;
                }

                // Filtro de Categoria
                if (filtro.getCategoria() != null) {
                    if (disp.getCategoria() != filtro.getCategoria()) continue;
                }

                // Arredonda distância para 1 casa decimal
                BigDecimal distFormatada = new BigDecimal(distancia).setScale(1, RoundingMode.HALF_UP);

                resultados.add(ResultadoBuscaDTO.builder()
                        .idTime(outro.getId())
                        .nomeTime(outro.getNomeTime())
                        .categoria(disp.getCategoria())
                        .diaSemana(disp.getDiaSemana())
                        .horario(disp.getHoraInicio() + " - " + disp.getHoraFim())
                        .distancia(distFormatada.doubleValue())
                        .mandoCampo(outro.getMandoCampo())
                        .ligaVinculada(null) // Etapa futura
                        .build());
            }
        }

        // Ordena pelos mais próximos
        resultados.sort((r1, r2) -> Double.compare(r1.getDistancia(), r2.getDistancia()));

        return resultados;
    }
}
