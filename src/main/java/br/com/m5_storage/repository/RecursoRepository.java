package br.com.m5_storage.repository;

import br.com.m5_storage.entity.recurso.Recurso;
import br.com.m5_storage.entity.recurso.StatusRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    List<Recurso> findByStatus(StatusRecurso status);

    @Query("SELECT r FROM Recurso r WHERE r.critico = true AND r.quantidade <= r.minimo")
    List<Recurso> findRecursosCriticos();

    List<Recurso> findBySetorId(Long setorId);

    List<Recurso> findBySetor_BaseId(Long baseId);
}