package br.com.arenamatch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Jogo;
import br.com.arenamatch.enums.StatusJogo;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {
    
    // Busca todos os jogos onde o time está envolvido (como mandante ou visitante)
    @Query("SELECT j FROM Jogo j WHERE j.timeMandante.id = :timeId OR j.timeVisitante.id = :timeId ORDER BY j.dataJogo ASC")
    List<Jogo> findJogosByTimeId(@Param("timeId") Long timeId);
    
 // Adicione os imports necessários:
    // import br.com.arenamatch.enums.StatusJogo;
    // import java.time.LocalDate;

    boolean existsByTimeMandanteIdAndTimeVisitanteIdAndDataJogoAndStatusIn(
            Long idMandante, Long idVisitante, LocalDate dataJogo, List<StatusJogo> status);
}