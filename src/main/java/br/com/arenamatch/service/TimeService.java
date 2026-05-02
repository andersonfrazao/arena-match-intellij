package br.com.arenamatch.service;

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
    	
    	if (timeRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado no sistema.");
        }
        if (timeRepository.existsByCpf(dto.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado no sistema.");
        }
        // 1. Busca Geo (Lat/Long) antes de salvar
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
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .cidade(dto.getCidade())
                .uf(dto.getUf())
                .regiao(dto.getRegiao())
                .latitude(coords[0])
                .longitude(coords[1])
                .build();

        // 2. Converte disponibilidades
        if(dto.getDisponibilidades() != null) {
            time.setDisponibilidades(dto.getDisponibilidades().stream().map(d -> 
                Disponibilidade.builder()
                    .time(time) // Vínculo
                    .diaSemana(d.getDiaSemana())
                    .categoria(d.getCategoria())
                    .horaInicio(d.getHoraInicio())
                    .horaFim(d.getHoraFim())
                    .build()
            ).collect(Collectors.toList()));
        }
        
     // 3. Monta Disponibilidades
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
}