package br.com.m5_storage.repository;

import br.com.m5_storage.entity.setor.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Long> {

    List<Setor> findByBaseId(Long baseId);
}