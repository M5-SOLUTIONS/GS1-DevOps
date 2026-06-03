package br.com.m5_storage.repository;

import br.com.m5_storage.entity.movimentacao.Movimentacao;
import br.com.m5_storage.entity.movimentacao.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    boolean existsByRecursoId(Long recursoId);

    List<Movimentacao> findByRecursoIdOrderByDataMovimentacaoDesc(Long recursoId);

    List<Movimentacao> findByUsuarioIdOrderByDataMovimentacaoDesc(Long usuarioId);

    List<Movimentacao> findBySetorIdOrderByDataMovimentacaoDesc(Long setorId);

    List<Movimentacao> findBySetorIdAndTipoMovimentacaoOrderByDataMovimentacaoDesc(
            Long setorId, TipoMovimentacao tipo);

    List<Movimentacao> findBySetor_BaseIdOrderByDataMovimentacaoDesc(Long baseId);
}