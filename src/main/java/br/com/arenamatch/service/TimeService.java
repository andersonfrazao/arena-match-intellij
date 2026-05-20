package br.com.arenamatch.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.enums.StatusJogo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;
    private final BrasilApiService brasilApi;
    private final GoogleMapsService googleMaps;
    private final PasswordEncoder passwordEncoder;
    private final JogoRepository jogoRepository;

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
                .complemento(dto.getComplemento())
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

    public TimeDTO buscarPorId(Long id) {
        Time time = timeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time nao encontrado."));
        return converterParaDTO(time);
    }

    @Transactional
    public Time atualizar(Long id, TimeDTO dto) {
        Time time = timeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time nao encontrado."));

        validarCamposProtegidos(time, dto);
        validarDisponibilidades(dto);

        boolean localAlterado = localAlterado(time, dto);
        if (localAlterado && existeJogoNaoRealizado(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ainda existe jogo a ser realizado. O local so podera ser alterado depois que o jogo acontecer.");
        }

        time.setWhatsapp(dto.getWhatsapp());

        if ("MANDO".equalsIgnoreCase(time.getMandoCampo())) {
            time.setTaxaJogo(dto.getTaxaJogo());
        } else {
            time.setTaxaJogo(null);
        }

        if (localAlterado) {
            time.setCep(dto.getCep());
            time.setLogradouro(dto.getLogradouro());
            time.setNumero(dto.getNumero());
            time.setComplemento(dto.getComplemento());
            time.setCidade(dto.getCidade());
            time.setUf(dto.getUf());
            time.setRegiao(dto.getRegiao());

            String enderecoBusca = dto.getLogradouro() + ", " + dto.getNumero() + " - " + dto.getCidade();
            double[] coords = googleMaps.getLatLong(enderecoBusca);
            time.setLatitude(coords[0]);
            time.setLongitude(coords[1]);
        }

        if (time.getDisponibilidades() == null) {
            time.setDisponibilidades(new ArrayList<>());
        } else {
            time.getDisponibilidades().clear();
        }

        if (dto.getDisponibilidades() != null) {
            dto.getDisponibilidades().forEach(d -> time.getDisponibilidades().add(
                    Disponibilidade.builder()
                            .time(time)
                            .diaSemana(d.getDiaSemana())
                            .categoria(d.getCategoria())
                            .horaInicio(d.getHoraInicio())
                            .horaFim(d.getHoraFim())
                            .build()));
        }

        return timeRepository.save(time);
    }

    private void validarCamposProtegidos(Time time, TimeDTO dto) {
        if (dto.getCpf() != null && !Objects.equals(time.getCpf(), dto.getCpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF nao pode ser alterado.");
        }
        if (dto.getNomeResponsavel() != null && !Objects.equals(time.getNomeResponsavel(), dto.getNomeResponsavel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do responsavel nao pode ser alterado.");
        }
        if (dto.getEmail() != null && !Objects.equals(time.getEmail(), dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail nao pode ser alterado.");
        }
        if (dto.getMandoCampo() != null && !Objects.equals(time.getMandoCampo(), dto.getMandoCampo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mando de campo nao pode ser alterado.");
        }
        if (dto.getNomeTime() != null && !Objects.equals(time.getNomeTime(), dto.getNomeTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do time nao pode ser alterado.");
        }
    }

    private boolean existeJogoNaoRealizado(Long timeId) {
        return jogoRepository.existsJogoNaoRealizadoDoTime(
                timeId,
                LocalDate.now(),
                Arrays.asList(StatusJogo.PENDENTE, StatusJogo.CONFIRMADO));
    }

    private boolean localAlterado(Time time, TimeDTO dto) {
        return !Objects.equals(normalizar(time.getCep()), normalizar(dto.getCep()))
                || !Objects.equals(normalizar(time.getLogradouro()), normalizar(dto.getLogradouro()))
                || !Objects.equals(normalizar(time.getNumero()), normalizar(dto.getNumero()))
                || !Objects.equals(normalizar(time.getComplemento()), normalizar(dto.getComplemento()))
                || !Objects.equals(normalizar(time.getCidade()), normalizar(dto.getCidade()))
                || !Objects.equals(normalizar(time.getUf()), normalizar(dto.getUf()))
                || !Objects.equals(normalizar(time.getRegiao()), normalizar(dto.getRegiao()));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private TimeDTO converterParaDTO(Time time) {
        TimeDTO dto = new TimeDTO();
        dto.setId(time.getId());
        dto.setNomeResponsavel(time.getNomeResponsavel());
        dto.setCpf(time.getCpf());
        dto.setEmail(time.getEmail());
        dto.setWhatsapp(time.getWhatsapp());
        dto.setNomeTime(time.getNomeTime());
        dto.setMandoCampo(time.getMandoCampo());
        dto.setTaxaJogo(time.getTaxaJogo());
        dto.setCep(time.getCep());
        dto.setLogradouro(time.getLogradouro());
        dto.setNumero(time.getNumero());
        dto.setComplemento(time.getComplemento());
        dto.setCidade(time.getCidade());
        dto.setUf(time.getUf());
        dto.setRegiao(time.getRegiao());
        dto.setLatitude(time.getLatitude());
        dto.setLongitude(time.getLongitude());
        if (time.getDisponibilidades() != null) {
            dto.setDisponibilidades(time.getDisponibilidades().stream().map(d -> {
                var disponibilidadeDTO = new br.com.arenamatch.dto.DisponibilidadeDTO();
                disponibilidadeDTO.setDiaSemana(d.getDiaSemana());
                disponibilidadeDTO.setCategoria(d.getCategoria());
                disponibilidadeDTO.setHoraInicio(d.getHoraInicio());
                disponibilidadeDTO.setHoraFim(d.getHoraFim());
                return disponibilidadeDTO;
            }).collect(Collectors.toList()));
        }
        return dto;
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
