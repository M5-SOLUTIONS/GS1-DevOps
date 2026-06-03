package br.com.m5_storage.service;

import br.com.m5_storage.dto.movimentacao.MovimentacaoCadastroDTO;
import br.com.m5_storage.dto.movimentacao.MovimentacaoListagemDTO;
import br.com.m5_storage.entity.movimentacao.Movimentacao;
import br.com.m5_storage.entity.movimentacao.TipoMovimentacao;
import br.com.m5_storage.entity.recurso.Recurso;
import br.com.m5_storage.entity.recurso.StatusRecurso;
import br.com.m5_storage.entity.setor.Setor;
import br.com.m5_storage.entity.usuario.Operator;
import br.com.m5_storage.entity.usuario.Usuario;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.exception.OperadorNecessarioException;
import br.com.m5_storage.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoService recursoService;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               RecursoRepository recursoRepository,
                               UsuarioRepository usuarioRepository,
                               RecursoService recursoService) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.recursoRepository = recursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.recursoService = recursoService;
    }

    @Transactional
    public MovimentacaoListagemDTO registrarMovimentacao(MovimentacaoCadastroDTO dto) {

        Recurso recurso = recursoRepository.findById(dto.recursoId())
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Recurso não encontrado com id: " + dto.recursoId()
                ));

        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Usuário não encontrado com id: " + dto.usuarioId()
                ));

        if (!(usuario instanceof Operator)) {
            throw new OperadorNecessarioException(
                    "Apenas Operators podem registrar movimentações."
            );
        }

        Setor setor = recurso.getSetor();

        if (dto.tipoMovimentacao() == TipoMovimentacao.CONSUMO) {
            realizarConsumo(recurso, dto.quantidade());
        } else {
            realizarReabastecimento(recurso, dto.quantidade());
        }

        Movimentacao movimentacao = Movimentacao.builder()
                .recurso(recurso)
                .usuario(usuario)
                .setor(setor)
                .tipoMovimentacao(dto.tipoMovimentacao())
                .quantidade(dto.quantidade())
                .descricao(dto.descricao())
                .dataMovimentacao(LocalDateTime.now())
                .build();

        return toDTO(movimentacaoRepository.save(movimentacao));
    }

    // ── leitura: qualquer usuário ────────────────────────────

    @Transactional(readOnly = true)
    public List<MovimentacaoListagemDTO> readMovimentacoesByRecurso(Long recursoId) {
        return movimentacaoRepository
                .findByRecursoIdOrderByDataMovimentacaoDesc(recursoId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoListagemDTO> readMovimentacoesByUsuario(Long usuarioId) {
        return movimentacaoRepository
                .findByUsuarioIdOrderByDataMovimentacaoDesc(usuarioId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoListagemDTO> readMovimentacoesBySetor(Long setorId) {
        return movimentacaoRepository
                .findBySetorIdOrderByDataMovimentacaoDesc(setorId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoListagemDTO> readMovimentacoesBySetorAndTipo(Long setorId,
                                                                         TipoMovimentacao tipo) {
        return movimentacaoRepository
                .findBySetorIdAndTipoMovimentacaoOrderByDataMovimentacaoDesc(setorId, tipo)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoListagemDTO> readMovimentacoesByBase(Long baseId) {
        return movimentacaoRepository
                .findBySetor_BaseIdOrderByDataMovimentacaoDesc(baseId)
                .stream().map(this::toDTO).toList();
    }

    // ── lógica de estoque ────────────────────────────────────

    private void realizarConsumo(Recurso recurso, Double quantidade) {
        double novaQuantidade = recurso.getQuantidade() - quantidade;

        if (novaQuantidade < 0) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: " + recurso.getQuantidade()
                            + ", solicitado: " + quantidade
            );
        }

        recurso.setQuantidade(novaQuantidade);
        atualizarStatusEAlertas(recurso);
    }

    private void realizarReabastecimento(Recurso recurso, Double quantidade) {
        double novaQuantidade = recurso.getQuantidade() + quantidade;

        if (novaQuantidade > recurso.getCapacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Quantidade excede a capacidade máxima do recurso. "
                            + "Capacidade: " + recurso.getCapacidadeMaxima()
                            + ", atual: " + recurso.getQuantidade()
                            + ", solicitado: " + quantidade
            );
        }

        recurso.setQuantidade(novaQuantidade);
        atualizarStatusEAlertas(recurso);
    }

    private void atualizarStatusEAlertas(Recurso recurso) {
        StatusRecurso novoStatus = recursoService.calcularStatus(
                recurso.getQuantidade(), recurso.getMinimo()
        );
        recurso.setStatus(novoStatus);
        recurso.setUltimaAtualizacao(LocalDateTime.now());
        recursoRepository.save(recurso);
        recursoService.sincronizarAlertas(recurso);
    }

    // ── helpers ──────────────────────────────────────────────

    private MovimentacaoListagemDTO toDTO(Movimentacao m) {
        return new MovimentacaoListagemDTO(
                m.getId(),
                m.getRecurso().getId(),
                m.getRecurso().getNome(),
                m.getSetor().getId(),
                m.getSetor().getInfo().getNome(),
                m.getUsuario().getId(),
                m.getUsuario().getNome(),
                m.getTipoMovimentacao(),
                m.getQuantidade(),
                m.getDescricao(),
                m.getDataMovimentacao()
        );
    }
}