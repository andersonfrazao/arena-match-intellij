package br.com.arenamatch.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.TimeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder; // Injeta o encoder

    public Time autenticar(LoginDTO loginDTO) {
        Optional<Time> timeOpt = timeRepository.findByEmail(loginDTO.getEmail());

        if (timeOpt.isPresent()) {
            Time time = timeOpt.get();
            // Compara a senha digitada (loginDTO) com a hash do banco (time.getSenha)
            if (passwordEncoder.matches(loginDTO.getSenha(), time.getSenha())) {
                return time;
            }
        }
        throw new RuntimeException("Usuário ou senha inválidos");
    }
}