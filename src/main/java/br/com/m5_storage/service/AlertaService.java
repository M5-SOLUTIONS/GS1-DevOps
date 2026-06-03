package br.com.m5_storage.service;

import br.com.m5_storage.dto.alerta.AlertaListagemDTO;
import br.com.m5_storage.entity.alerta.Alerta;
import br.com.m5_storage.entity.setor.Setor;
import br.com.m5_storage.entity.usuario.Operator;
import br.com.m5_storage.entity.usuario.Usuario;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.exception.OperadorNecessarioException;
import br.com.m5_storage.repository.AlertaRepository;
import br.com.m5_storage.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── leitura: qualquer usuário ────────────────────────────

    @Transactional(readOnly = true)
    public List<AlertaListagemDTO> readAlertasAtivos() {
        return alertaRepository.findByResolvidoFalseOrderByDataAlertaDesc()
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaListagemDTO> readAlertasByRecurso(Long recursoId) {
        return alertaRepository.findByRecursoIdAndResolvidoFalse(recursoId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaListagemDTO> readAlertasBySetor(Long setorId) {
        return alertaRepository
                .findByRecurso_Setor_IdAndResolvidoFalseOrderByDataAlertaDesc(setorId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaListagemDTO> readAlertasByBase(Long baseId) {
        return alertaRepository
                .findByRecurso_Setor_BaseIdAndResolvidoFalseOrderByDataAlertaDesc(baseId)
                .stream().map(this::toDTO).toList();
    }

    // ── escrita: apenas Operator ─────────────────────────────

    @Transactional
    public AlertaListagemDTO resolverAlerta(Long id, Long usuarioId) {
        exigirOperator(usuarioId);

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Alerta não encontrado com id: " + id
                ));

        if (alerta.getResolvido()) {
            throw new IllegalArgumentException(
                    "O alerta com id " + id + " já foi resolvido."
            );
        }

        alerta.setResolvido(true);
        return toDTO(alertaRepository.save(alerta));
    }

    // ── helpers ──────────────────────────────────────────────

    private void exigirOperator(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Usuário não encontrado com id: " + usuarioId
                ));

        if (!(usuario instanceof Operator)) {
            throw new OperadorNecessarioException(
                    "Apenas Operators podem resolver alertas."
            );
        }
    }

    private AlertaListagemDTO toDTO(Alerta a) {
        Setor setor = a.getRecurso().getSetor();
        return new AlertaListagemDTO(
                a.getId(),
                a.getRecurso().getId(),
                a.getRecurso().getNome(),
                setor.getId(),
                setor.getInfo().getNome(),
                setor.getBase().getId(),
                a.getMensagem(),
                a.getNivel(),
                a.getResolvido(),
                a.getDataAlerta()
        );
    }
}