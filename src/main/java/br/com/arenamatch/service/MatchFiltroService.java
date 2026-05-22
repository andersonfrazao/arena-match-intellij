package br.com.arenamatch.service;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.entity.Disponibilidade;

@Service
public class MatchFiltroService {

    public boolean distanciaAtende(double distancia, BuscaFiltroDTO filtro) {
        return filtro.getDistanciaKm() == null || distancia <= filtro.getDistanciaKm();
    }

    public boolean disponibilidadeAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        return diaSemanaAtende(disponibilidade, filtro) && categoriaAtende(disponibilidade, filtro);
    }

    public boolean diaSemanaAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        if (filtro.getDiaSemana() == null || filtro.getDiaSemana().isBlank()
                || filtro.getDiaSemana().equals("Qualquer")) {
            return true;
        }

        return disponibilidade.getDiaSemana().equalsIgnoreCase(filtro.getDiaSemana());
    }

    public boolean categoriaAtende(Disponibilidade disponibilidade, BuscaFiltroDTO filtro) {
        return filtro.getCategoria() == null || disponibilidade.getCategoria() == filtro.getCategoria();
    }
}
