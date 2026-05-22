package br.com.arenamatch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public ResultadoBuscaDTO criarResultado(Time adversario, Time mandante, Disponibilidade disponibilidade,
            String horaInicio, String horaFim, double distancia, boolean convitePendente) {
        LocalDate dataExata = calcularProximaData(disponibilidade.getDiaSemana());

        return ResultadoBuscaDTO.builder()
                .idTime(adversario.getId())
                .nomeTime(adversario.getNomeTime())
                .categoria(disponibilidade.getCategoria())
                .diaSemana(disponibilidade.getDiaSemana())
                .horario(horaInicio + " - " + horaFim)
                .horaInicio(horaInicio)
                .horaFim(horaFim)
                .distancia(formatarDistancia(distancia))
                .mandoCampo(adversario.getMandoCampo())
                .taxaJogo(mandante.getTaxaJogo())
                .enderecoJogo(formatarEndereco(mandante))
                .dataExata(dataExata)
                .dataExataFormatada(dataExata.format(DATA_FORMATTER))
                .convitePendente(convitePendente)
                .build();
    }

    public boolean horariosCoincidem(Disponibilidade primeira, Disponibilidade segunda) {
        LocalTime inicio = LocalTime.parse(maiorHoraInicio(primeira, segunda));
        LocalTime fim = LocalTime.parse(menorHoraFim(primeira, segunda));
        return fim.isAfter(inicio);
    }

    public String maiorHoraInicio(Disponibilidade primeira, Disponibilidade segunda) {
        LocalTime primeiraHora = LocalTime.parse(primeira.getHoraInicio());
        LocalTime segundaHora = LocalTime.parse(segunda.getHoraInicio());
        return primeiraHora.isAfter(segundaHora) ? primeira.getHoraInicio() : segunda.getHoraInicio();
    }

    public String menorHoraFim(Disponibilidade primeira, Disponibilidade segunda) {
        LocalTime primeiraHora = LocalTime.parse(primeira.getHoraFim());
        LocalTime segundaHora = LocalTime.parse(segunda.getHoraFim());
        return primeiraHora.isBefore(segundaHora) ? primeira.getHoraFim() : segunda.getHoraFim();
    }

    private double formatarDistancia(double distancia) {
        return BigDecimal.valueOf(distancia)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private LocalDate calcularProximaData(String diaSemanaPt) {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.of(obterDiaSemana(diaSemanaPt))));
    }

    private String formatarEndereco(Time mandante) {
        StringBuilder endereco = new StringBuilder();
        adicionarParteEndereco(endereco, mandante.getLogradouro());
        adicionarParteEndereco(endereco, mandante.getNumero());
        adicionarParteEndereco(endereco, mandante.getCidade());
        adicionarParteEndereco(endereco, mandante.getUf());
        return endereco.toString();
    }

    private void adicionarParteEndereco(StringBuilder endereco, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        if (endereco.length() > 0) {
            endereco.append(", ");
        }
        endereco.append(valor.trim());
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
