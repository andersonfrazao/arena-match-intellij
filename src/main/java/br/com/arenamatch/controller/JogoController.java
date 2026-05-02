package br.com.arenamatch.controller;

import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.enums.StatusJogo;
import br.com.arenamatch.service.JogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
public class JogoController {

    private final JogoService jogoService;

    @PostMapping("/convite")
    public ResponseEntity<JogoDTO> enviarConvite(@RequestBody JogoDTO dto) {
        return ResponseEntity.ok(jogoService.enviarConvite(dto));
    }

    @PutMapping("/{id}/responder")
    public ResponseEntity<JogoDTO> responderConvite(@PathVariable Long id, @RequestParam StatusJogo status) {
        return ResponseEntity.ok(jogoService.responderConvite(id, status));
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<JogoDTO>> listarPorTime(@PathVariable Long timeId) {
        return ResponseEntity.ok(jogoService.listarJogosDoTime(timeId));
    }
}