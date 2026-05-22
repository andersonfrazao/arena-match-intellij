package br.com.arenamatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConta;
import br.com.arenamatch.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void autenticaContaAtivaComSenhaValida() {
        LoginDTO login = login();
        Time time = time(StatusConta.ATIVO);

        when(timeRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(time));
        when(passwordEncoder.matches(login.getSenha(), time.getSenha())).thenReturn(true);

        assertThat(authService.autenticar(login)).isSameAs(time);
    }

    @Test
    void recusaContaInativaComMensagemDeContato() {
        LoginDTO login = login();
        Time time = time(StatusConta.INATIVO);

        when(timeRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(time));
        when(passwordEncoder.matches(login.getSenha(), time.getSenha())).thenReturn(true);

        assertThatThrownBy(() -> authService.autenticar(login))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getReason()).contains("arenamatch.app@gmail.com");
                });
    }

    @Test
    void preservaErroDeCredencialInvalida() {
        LoginDTO login = login();
        Time time = time(StatusConta.ATIVO);

        when(timeRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(time));
        when(passwordEncoder.matches(login.getSenha(), time.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> authService.autenticar(login))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    private LoginDTO login() {
        LoginDTO login = new LoginDTO();
        login.setEmail("time@arena.test");
        login.setSenha("senha");
        return login;
    }

    private Time time(StatusConta statusConta) {
        Time time = new Time();
        time.setEmail("time@arena.test");
        time.setSenha("hash");
        time.setStatusConta(statusConta);
        return time;
    }
}
