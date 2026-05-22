package br.com.arenamatch.controller;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.service.TimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/times")
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;

    @GetMapping("/cep/{cep}")
    public ResponseEntity<TimeDTO> consultarCep(@PathVariable String cep) {
        return ResponseEntity.ok(timeService.buscarCep(cep));
    }

    @PostMapping
    public ResponseEntity<Time> criarTime(@RequestBody TimeDTO dto) {
        return ResponseEntity.ok(timeService.salvar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeDTO> buscarTime(@PathVariable Long id) {
        return ResponseEntity.ok(timeService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Time> atualizarTime(@PathVariable Long id, @RequestBody TimeDTO dto) {
        return ResponseEntity.ok(timeService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Time> desativarTime(@PathVariable Long id) {
        return ResponseEntity.ok(timeService.desativar(id));
    }
}
