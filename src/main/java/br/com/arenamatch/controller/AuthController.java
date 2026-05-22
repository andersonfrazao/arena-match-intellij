package br.com.arenamatch.controller;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Time> login(@RequestBody LoginDTO loginDTO) {
        Time timeLogado = authService.autenticar(loginDTO);
        return ResponseEntity.ok(timeLogado);
    }
}
