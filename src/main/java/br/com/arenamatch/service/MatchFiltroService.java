package br.com.arenamatch.service;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;

@Service
public class MatchFiltroService {

    public boolean timeAtende(Time time, double distancia, BuscaFiltroDTO filtro) {
        return distanciaAtende(distancia, filtro) && cidadeAtende(time, filtro);
    }

    public boolean disponibilidadeAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        return diaSemanaAtende(disponibilidade, filtro) && categoriaAtende(disponibilidade, filtro);
    }

    private boolean distanciaAtende(double distancia, BuscaFiltroDTO filtro) {
        return distancia <= filtro.getDistanciaKm();
    }

    private boolean cidadeAtende(Time time, BuscaFiltroDTO filtro) {
        if (filtro.getCidade() == null || filtro.getCidade().isBlank()) {
            return true;
        }

        return time.getCidade() != null && time.getCidade().equalsIgnoreCase(filtro.getCidade());
    }

    private boolean diaSemanaAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        if (filtro.getDiaSemana() == null || filtro.getDiaSemana().isBlank()
                || filtro.getDiaSemana().equals("Qualquer")) {
            return true;
        }

        return disponibilidade.getDiaSemana().equalsIgnoreCase(filtro.getDiaSemana());
    }

    private boolean categoriaAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        return filtro.getCategoria() == null || disponibilidade.getCategoria() == filtro.getCategoria();
    }
}
