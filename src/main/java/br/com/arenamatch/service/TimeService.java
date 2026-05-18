package br.com.arenamatch.service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Disponibilidade;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.integration.BrasilApiService;
import br.com.arenamatch.integration.GoogleMapsService;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;
    private final BrasilApiService brasilApi;
    private final GoogleMapsService googleMaps;
    private final PasswordEncoder passwordEncoder;

    public TimeDTO buscarCep(String cep) {
        TimeDTO dto = new TimeDTO();
        dto.setCep(cep);
        brasilApi.preencherEndereco(dto);
        return dto;
    }

    @Transactional
    public Time salvar(TimeDTO dto) {
        validarSenha(dto);

        if (timeRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado no sistema.");
        }
        if (timeRepository.existsByCpf(dto.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF ja cadastrado no sistema.");
        }

        validarDisponibilidades(dto);

        String enderecoBusca = dto.getLogradouro() + ", " + dto.getNumero() + " - " + dto.getCidade();
        double[] coords = googleMaps.getLatLong(enderecoBusca);

        Time time = Time.builder()
                .nomeResponsavel(dto.getNomeResponsavel())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .whatsapp(dto.getWhatsapp())
                .nomeTime(dto.getNomeTime())
                .mandoCampo(dto.getMandoCampo())
                .taxaJogo("MANDO".equalsIgnoreCase(dto.getMandoCampo()) ? dto.getTaxaJogo() : null)
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .cidade(dto.getCidade())
                .uf(dto.getUf())
                .regiao(dto.getRegiao())
                .latitude(coords[0])
                .longitude(coords[1])
                .build();

        if (dto.getDisponibilidades() != null) {
            time.setDisponibilidades(dto.getDisponibilidades().stream().map(d ->
                Disponibilidade.builder()
                    .time(time)
                    .diaSemana(d.getDiaSemana())
                    .categoria(d.getCategoria())
                    .horaInicio(d.getHoraInicio())
                    .horaFim(d.getHoraFim())
                    .build()
            ).collect(Collectors.toList()));
        }

        return timeRepository.save(time);
    }

    private void validarSenha(TimeDTO dto) {
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha obrigatoria.");
        }
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha e a confirmacao nao conferem.");
        }
    }

    private void validarDisponibilidades(TimeDTO dto) {
        if (dto.getDisponibilidades() == null) {
            return;
        }

        Set<String> diaCategoria = new HashSet<>();
        for (var d : dto.getDisponibilidades()) {
            if (d.getDiaSemana() == null || d.getDiaSemana().isBlank()
                    || d.getCategoria() == null
                    || d.getHoraInicio() == null || d.getHoraInicio().isBlank()
                    || d.getHoraFim() == null || d.getHoraFim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disponibilidade incompleta.");
            }

            String chave = d.getDiaSemana().trim().toLowerCase() + "|" + d.getCategoria().name().toLowerCase();
            if (!diaCategoria.add(chave)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e permitido cadastrar mais de um horario para o mesmo dia e categoria.");
            }

            LocalTime inicio;
            LocalTime fim;
            try {
                inicio = LocalTime.parse(d.getHoraInicio());
                fim = LocalTime.parse(d.getHoraFim());
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario invalido.");
            }

            if (!fim.isAfter(inicio)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario final deve ser maior que o inicial.");
            }

            if ("MANDO".equalsIgnoreCase(dto.getMandoCampo())
                    && Duration.between(inicio, fim).toMinutes() > 120) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Times mandantes podem cadastrar intervalos de no maximo 2 horas.");
            }
        }
    }
}
