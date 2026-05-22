package br.com.arenamatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Time;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {
    Optional<Time> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    

    @Query("SELECT DISTINCT t FROM Time t JOIN FETCH t.disponibilidades "
            + "WHERE t.id <> :idTimeLogado AND t.statusConta = br.com.arenamatch.enums.StatusConta.ATIVO")
    List<Time> findAllOutrosTimesComDisponibilidade(@Param("idTimeLogado") Long idTimeLogado);
}
