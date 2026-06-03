package br.com.m5_storage.repository;

import br.com.m5_storage.entity.alerta.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByRecursoIdAndResolvidoFalse(Long recursoId);

    List<Alerta> findByResolvidoFalseOrderByDataAlertaDesc();

    List<Alerta> findByRecurso_Setor_IdAndResolvidoFalseOrderByDataAlertaDesc(Long setorId);

    List<Alerta> findByRecurso_Setor_BaseIdAndResolvidoFalseOrderByDataAlertaDesc(Long baseId);
}