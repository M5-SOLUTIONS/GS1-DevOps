package br.com.m5_storage.service;

import br.com.m5_storage.dto.recurso.RecursoAtualizarDTO;
import br.com.m5_storage.dto.recurso.RecursoCadastroDTO;
import br.com.m5_storage.dto.recurso.RecursoListagemDTO;
import br.com.m5_storage.entity.alerta.Alerta;
import br.com.m5_storage.entity.recurso.Recurso;
import br.com.m5_storage.entity.recurso.StatusRecurso;
import br.com.m5_storage.entity.setor.Setor;
import br.com.m5_storage.entity.usuario.Operator;
import br.com.m5_storage.entity.usuario.Usuario;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.exception.OperadorNecessarioException;
import br.com.m5_storage.repository.AlertaRepository;
import br.com.m5_storage.repository.MovimentacaoRepository;
import br.com.m5_storage.repository.RecursoRepository;
import br.com.m5_storage.repository.SetorRepository;
import br.com.m5_storage.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final SetorRepository setorRepository;
    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    public RecursoService(RecursoRepository recursoRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          SetorRepository setorRepository,
                          AlertaRepository alertaRepository,
                          UsuarioRepository usuarioRepository) {
        this.recursoRepository = recursoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.setorRepository = setorRepository;
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── operações de escrita: apenas Operator ────────────────

    @Transactional
    public RecursoListagemDTO createRecurso(RecursoCadastroDTO dto, Long usuarioId) {
        exigirOperator(usuarioId);

        if (dto.quantidade() > dto.capacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Quantidade (" + dto.quantidade() + ") não pode ser maior "
                            + "que a capacidade máxima (" + dto.capacidadeMaxima() + ")."
            );
        }

        if (dto.minimo() >= dto.capacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Mínimo (" + dto.minimo() + ") deve ser menor "
                            + "que a capacidade máxima (" + dto.capacidadeMaxima() + ")."
            );
        }

        Setor setor = setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Setor não encontrado com id: " + dto.setorId()
                ));

        Recurso recurso = Recurso.builder()
                .setor(setor)
                .nome(dto.nome())
                .categoria(dto.categoria())
                .quantidade(dto.quantidade())
                .minimo(dto.minimo())
                .capacidadeMaxima(dto.capacidadeMaxima())
                .critico(dto.critico() != null && dto.critico())
                .status(calcularStatus(dto.quantidade(), dto.minimo()))
                .ultimaAtualizacao(LocalDateTime.now())
                .build();

        return toDTO(recursoRepository.save(recurso));
    }

    @Transactional
    public RecursoListagemDTO updateRecurso(Long id, RecursoAtualizarDTO dto, Long usuarioId) {
        exigirOperator(usuarioId);

        if (dto.quantidade() > dto.capacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Quantidade (" + dto.quantidade() + ") não pode ser maior "
                            + "que a capacidade máxima (" + dto.capacidadeMaxima() + ")."
            );
        }

        if (dto.minimo() >= dto.capacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Mínimo (" + dto.minimo() + ") deve ser menor "
                            + "que a capacidade máxima (" + dto.capacidadeMaxima() + ")."
            );
        }

        Recurso recurso = findOrThrow(id);

        recurso.setNome(dto.nome());
        recurso.setCategoria(dto.categoria());
        recurso.setQuantidade(dto.quantidade());
        recurso.setMinimo(dto.minimo());
        recurso.setCapacidadeMaxima(dto.capacidadeMaxima());
        recurso.setCritico(dto.critico() != null && dto.critico());
        recurso.setStatus(calcularStatus(dto.quantidade(), dto.minimo()));
        recurso.setUltimaAtualizacao(LocalDateTime.now());

        recursoRepository.save(recurso);
        sincronizarAlertas(recurso);

        return toDTO(recurso);
    }

    @Transactional
    public void deleteRecurso(Long id, Long usuarioId) {
        exigirOperator(usuarioId);
        findOrThrow(id);

        if (movimentacaoRepository.existsByRecursoId(id)) {
            throw new DataIntegrityViolationException(
                    "Não é possível remover o recurso pois existem movimentações vinculadas."
            );
        }

        recursoRepository.deleteById(id);
    }

    // ── operações de leitura: qualquer usuário ───────────────

    @Transactional(readOnly = true)
    public List<RecursoListagemDTO> readAllRecursos() {
        return recursoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RecursoListagemDTO readRecursoById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<RecursoListagemDTO> readRecursosByStatus(StatusRecurso status) {
        return recursoRepository.findByStatus(status).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RecursoListagemDTO> readRecursosBySetor(Long setorId) {
        return recursoRepository.findBySetorId(setorId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RecursoListagemDTO> readRecursosByBase(Long baseId) {
        return recursoRepository.findBySetor_BaseId(baseId).stream().map(this::toDTO).toList();
    }

    // ── helpers públicos usados por MovimentacaoService ──────

    public Recurso findOrThrow(Long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Recurso não encontrado com id: " + id
                ));
    }

    public StatusRecurso calcularStatus(Double quantidade, Double minimo) {
        double tolerancia = 0.0001;

        if (quantidade > minimo + tolerancia)              return StatusRecurso.OK;
        if (Math.abs(quantidade - minimo) <= tolerancia)   return StatusRecurso.ATENCAO;
        return StatusRecurso.CRITICO;
    }

    public void sincronizarAlertas(Recurso recurso) {
        if (!recurso.getCritico()) return;

        StatusRecurso status = recurso.getStatus();

        if (status == StatusRecurso.CRITICO || status == StatusRecurso.ATENCAO) {
            List<Alerta> alertasAtivos = alertaRepository
                    .findByRecursoIdAndResolvidoFalse(recurso.getId());

            if (alertasAtivos.isEmpty()) {
                alertaRepository.save(Alerta.builder()
                        .recurso(recurso)
                        .setor(recurso.getSetor())
                        .mensagem("Recurso " + recurso.getNome()
                                + " atingiu nível " + status.name()
                                + ". Quantidade: " + recurso.getQuantidade())
                        .nivel(status.name())
                        .resolvido(false)
                        .dataAlerta(LocalDateTime.now())
                        .build());
            } else {
                alertasAtivos.forEach(a -> {
                    a.setNivel(status.name());
                    a.setMensagem("Recurso " + recurso.getNome()
                            + " em nível " + status.name()
                            + ". Quantidade: " + recurso.getQuantidade());
                    alertaRepository.save(a);
                });
            }

        } else if (status == StatusRecurso.OK) {
            alertaRepository.findByRecursoIdAndResolvidoFalse(recurso.getId())
                    .forEach(a -> {
                        a.setResolvido(true);
                        alertaRepository.save(a);
                    });
        }
    }

    public RecursoListagemDTO toDTO(Recurso r) {
        return new RecursoListagemDTO(
                r.getId(),
                r.getNome(),
                r.getCategoria(),
                r.getQuantidade(),
                r.getMinimo(),
                r.getCapacidadeMaxima(),
                r.getCritico(),
                r.getStatus(),
                r.getUltimaAtualizacao(),
                r.getSetor().getId(),
                r.getSetor().getInfo().getNome(),
                r.getSetor().getBase().getId()
        );
    }

    // ── verificação de permissão ─────────────────────────────

    private void exigirOperator(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Usuário não encontrado com id: " + usuarioId
                ));

        if (!(usuario instanceof Operator)) {
            throw new OperadorNecessarioException();
        }
    }
}