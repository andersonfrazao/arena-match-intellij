package br.com.arenamatch.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.integration.BrasilApiService;
import br.com.arenamatch.integration.GoogleMapsService;
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.enums.StatusConta;
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
    private final DisponibilidadeService disponibilidadeService;

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

        disponibilidadeService.validar(dto);

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
                .build();

        preencherCoordenadas(time, dto);
        time.setDisponibilidades(disponibilidadeService.criarPara(time, dto));

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
        disponibilidadeService.validar(dto);

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

            preencherCoordenadas(time, dto);
        }

        disponibilidadeService.substituirDe(time, dto);

        return timeRepository.save(time);
    }

    @Transactional
    public Time desativar(Long id) {
        Time time = timeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time nao encontrado."));

        time.setStatusConta(StatusConta.INATIVO);
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

    private void preencherCoordenadas(Time time, TimeDTO dto) {
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            time.setLatitude(dto.getLatitude());
            time.setLongitude(dto.getLongitude());
            return;
        }

        double[] coords = googleMaps.getLatLongPorEndereco(montarEnderecoBusca(dto), dto.getCep());
        time.setLatitude(coords[0]);
        time.setLongitude(coords[1]);
    }

    private String montarEnderecoBusca(TimeDTO dto) {
        return Stream.of(dto.getLogradouro(), dto.getNumero(), dto.getCidade(), dto.getUf(), dto.getCep(), "Brazil")
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
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
        dto.setDisponibilidades(disponibilidadeService.converterParaDTO(time.getDisponibilidades()));
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

}
