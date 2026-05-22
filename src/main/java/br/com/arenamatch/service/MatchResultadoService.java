package br.com.arenamatch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.util.GeoUtil;

@Service
public class MatchResultadoService {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public double calcularDistancia(Time timeBuscando, Time adversario) {
        if (timeBuscando.getLatitude() == null || adversario.getLatitude() == null) {
            return 0.0;
        }

        return GeoUtil.calcularDistancia(
                timeBuscando.getLatitude(), timeBuscando.getLongitude(),
                adversario.getLatitude(), adversario.getLongitude());
    }

    public ResultadoBuscaDTO criarResultado(Time adversario, Disponibilidade disponibilidade, double distancia) {
        LocalDate dataExata = calcularProximaData(disponibilidade.getDiaSemana());

        return ResultadoBuscaDTO.builder()
                .idTime(adversario.getId())
                .nomeTime(adversario.getNomeTime())
                .categoria(disponibilidade.getCategoria())
                .diaSemana(disponibilidade.getDiaSemana())
                .horario(disponibilidade.getHoraInicio() + " - " + disponibilidade.getHoraFim())
                .horaInicio(disponibilidade.getHoraInicio())
                .horaFim(disponibilidade.getHoraFim())
                .distancia(formatarDistancia(distancia))
                .mandoCampo(adversario.getMandoCampo())
                .dataExata(dataExata)
                .dataExataFormatada(dataExata.format(DATA_FORMATTER))
                .build();
    }

    private double formatarDistancia(double distancia) {
        return BigDecimal.valueOf(distancia)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private LocalDate calcularProximaData(String diaSemanaPt) {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.of(obterDiaSemana(diaSemanaPt))));
    }

    private int obterDiaSemana(String diaSemanaPt) {
        return switch (normalizar(diaSemanaPt)) {
            case "domingo" -> 7;
            case "segunda" -> 1;
            case "terca" -> 2;
            case "quarta" -> 3;
            case "quinta" -> 4;
            case "sexta" -> 5;
            case "sabado" -> 6;
            default -> throw new IllegalArgumentException("Dia da semana invalido: " + diaSemanaPt);
        };
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
