package br.com.arenamatch.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConta;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;

    public Time autenticar(LoginDTO loginDTO) {
        Optional<Time> timeOpt = timeRepository.findByEmail(loginDTO.getEmail());

        if (timeOpt.isPresent()) {
            Time time = timeOpt.get();
            if (passwordEncoder.matches(loginDTO.getSenha(), time.getSenha())) {
                validarContaAtiva(time);
                return time;
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos.");
    }

    private void validarContaAtiva(Time time) {
        if (time.getStatusConta() == StatusConta.INATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A conta foi desativada. Entre em contato pelo e-mail arenamatch.app@gmail.com.");
        }
    }
}
