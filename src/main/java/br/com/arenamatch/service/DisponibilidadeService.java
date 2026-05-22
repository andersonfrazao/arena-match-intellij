package br.com.arenamatch.service;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;

@Service
public class DisponibilidadeService {

    public void validar(TimeDTO dto) {
        if (dto.getDisponibilidades() == null) {
            return;
        }

        Set<String> diaCategoria = new HashSet<>();
        for (DisponibilidadeDTO disponibilidade : dto.getDisponibilidades()) {
            validarCampos(disponibilidade);
            validarHorario(disponibilidade, dto.getMandoCampo());

            String chave = normalizarDiaSemana(disponibilidade.getDiaSemana())
                    + "|" + disponibilidade.getCategoria().name().toLowerCase();
            if (!diaCategoria.add(chave)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nao e permitido cadastrar mais de um horario para o mesmo dia e categoria.");
            }
        }
    }

    public List<Disponibilidade> criarPara(Time time, TimeDTO dto) {
        if (dto.getDisponibilidades() == null) {
            return null;
        }

        return dto.getDisponibilidades().stream()
                .map(disponibilidade -> criar(time, disponibilidade))
                .collect(Collectors.toList());
    }

    public void substituirDe(Time time, TimeDTO dto) {
        if (time.getDisponibilidades() == null) {
            time.setDisponibilidades(new ArrayList<>());
        } else {
            time.getDisponibilidades().clear();
        }

        if (dto.getDisponibilidades() != null) {
            dto.getDisponibilidades().forEach(disponibilidade ->
                    time.getDisponibilidades().add(criar(time, disponibilidade)));
        }
    }

    public List<DisponibilidadeDTO> converterParaDTO(List<Disponibilidade> disponibilidades) {
        if (disponibilidades == null) {
            return null;
        }

        return disponibilidades.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private Disponibilidade criar(Time time, DisponibilidadeDTO dto) {
        return Disponibilidade.builder()
                .time(time)
                .diaSemana(dto.getDiaSemana())
                .categoria(dto.getCategoria())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .build();
    }

    private DisponibilidadeDTO converterParaDTO(Disponibilidade disponibilidade) {
        DisponibilidadeDTO dto = new DisponibilidadeDTO();
        dto.setDiaSemana(disponibilidade.getDiaSemana());
        dto.setCategoria(disponibilidade.getCategoria());
        dto.setHoraInicio(disponibilidade.getHoraInicio());
        dto.setHoraFim(disponibilidade.getHoraFim());
        return dto;
    }

    private void validarCampos(DisponibilidadeDTO disponibilidade) {
        if (disponibilidade.getDiaSemana() == null || disponibilidade.getDiaSemana().isBlank()
                || disponibilidade.getCategoria() == null
                || disponibilidade.getHoraInicio() == null || disponibilidade.getHoraInicio().isBlank()
                || disponibilidade.getHoraFim() == null || disponibilidade.getHoraFim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disponibilidade incompleta.");
        }
    }

    private void validarHorario(DisponibilidadeDTO disponibilidade, String mandoCampo) {
        LocalTime inicio;
        LocalTime fim;
        try {
            inicio = LocalTime.parse(disponibilidade.getHoraInicio());
            fim = LocalTime.parse(disponibilidade.getHoraFim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario invalido.");
        }

        if (!fim.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario final deve ser maior que o inicial.");
        }

        if ("MANDO".equalsIgnoreCase(mandoCampo)
                && Duration.between(inicio, fim).toMinutes() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Times mandantes podem cadastrar intervalos de no maximo 2 horas.");
        }
    }

    private String normalizarDiaSemana(String diaSemana) {
        return Normalizer.normalize(diaSemana.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
