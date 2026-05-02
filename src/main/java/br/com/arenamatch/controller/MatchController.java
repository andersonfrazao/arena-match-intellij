package br.com.arenamatch.controller;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import br.com.arenamatch.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/buscar/{idTime}")
    public ResponseEntity<List<ResultadoBuscaDTO>> buscar(@PathVariable Long idTime, @RequestBody BuscaFiltroDTO filtro) {
        return ResponseEntity.ok(matchService.buscarAdversarios(idTime, filtro));
    }
}