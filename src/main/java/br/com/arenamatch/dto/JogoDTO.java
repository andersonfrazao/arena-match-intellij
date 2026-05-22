package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusJogo;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JogoDTO {
    private Long id;
    private Long idMandante;
    private String nomeMandante;
    private Long idVisitante;
    private String nomeVisitante;
    private Long idSolicitante;
    private String nomeSolicitante;
    private LocalDate dataJogo;
    private String horaInicio;
    private String horaFim;
    private StatusJogo status;
}
