package br.com.arenamatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.integration.BrasilApiService;
import br.com.arenamatch.integration.GoogleMapsService;
import br.com.arenamatch.repository.JogoRepository;
import br.com.arenamatch.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private BrasilApiService brasilApi;

    @Mock
    private GoogleMapsService googleMaps;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private DisponibilidadeService disponibilidadeService;

    @InjectMocks
    private TimeService timeService;

    @Test
    void salvaCoordenadasConsultadasPeloCep() {
        TimeDTO dto = cadastro();

        when(passwordEncoder.encode(dto.getSenha())).thenReturn("hash");
        when(googleMaps.getLatLongPorEndereco("Rua Arena, 10, Sao Paulo, SP, 01001-000, Brazil", dto.getCep()))
                .thenReturn(new double[]{-23.5401, -46.6402});
        when(timeRepository.save(any(Time.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Time time = timeService.salvar(dto);

        assertThat(time.getLatitude()).isEqualTo(-23.5401);
        assertThat(time.getLongitude()).isEqualTo(-46.6402);
        verify(googleMaps).getLatLongPorEndereco("Rua Arena, 10, Sao Paulo, SP, 01001-000, Brazil", "01001-000");
    }

    @Test
    void preservaCoordenadasRecebidasNoDto() {
        TimeDTO dto = cadastro();
        dto.setLatitude(-22.9035);
        dto.setLongitude(-43.2096);

        when(passwordEncoder.encode(dto.getSenha())).thenReturn("hash");
        when(timeRepository.save(any(Time.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Time time = timeService.salvar(dto);

        assertThat(time.getLatitude()).isEqualTo(-22.9035);
        assertThat(time.getLongitude()).isEqualTo(-43.2096);
        verify(googleMaps, never()).getLatLongPorEndereco(any(), any());
    }

    private TimeDTO cadastro() {
        TimeDTO dto = new TimeDTO();
        dto.setNomeResponsavel("Responsavel");
        dto.setCpf("111.222.333-44");
        dto.setEmail("time@arena.test");
        dto.setSenha("senha");
        dto.setConfirmarSenha("senha");
        dto.setNomeTime("Arena");
        dto.setCep("01001-000");
        dto.setLogradouro("Rua Arena");
        dto.setNumero("10");
        dto.setCidade("Sao Paulo");
        dto.setUf("SP");
        return dto;
    }
}
