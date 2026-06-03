package br.com.m5_storage.service;

import br.com.m5_storage.dto.usuario.TipoUsuario;
import br.com.m5_storage.dto.usuario.UsuarioAtualizarDTO;
import br.com.m5_storage.dto.usuario.UsuarioCadastroDTO;
import br.com.m5_storage.dto.usuario.UsuarioListagemDTO;
import br.com.m5_storage.entity.base.Base;
import br.com.m5_storage.entity.usuario.Operator;
import br.com.m5_storage.entity.usuario.Usuario;
import br.com.m5_storage.entity.usuario.Viewer;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.repository.BaseRepository;
import br.com.m5_storage.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BaseRepository baseRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          BaseRepository baseRepository) {
        this.usuarioRepository = usuarioRepository;
        this.baseRepository = baseRepository;
    }

    @Transactional
    public UsuarioListagemDTO createUsuario(UsuarioCadastroDTO dto) {

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new DataIntegrityViolationException(
                    "Já existe um usuário com o email: " + dto.email()
            );
        }

        Base base = baseRepository.findById(dto.baseId())
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Base não encontrada com id: " + dto.baseId()
                ));

        Usuario usuario = criarInstancia(dto.tipoUsuario());
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setBase(base);

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioListagemDTO> readAllUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioListagemDTO> readUsuariosByBase(Long baseId) {
        return usuarioRepository.findByBaseId(baseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioListagemDTO readUsuarioById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public UsuarioListagemDTO updateUsuario(Long id, UsuarioAtualizarDTO dto) {
        Usuario usuario = findOrThrow(id);

        if (!usuario.getEmail().equals(dto.email())
                && usuarioRepository.existsByEmail(dto.email())) {
            throw new DataIntegrityViolationException(
                    "Já existe um usuário com o email: " + dto.email()
            );
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deleteUsuario(Long id) {
        usuarioRepository.delete(findOrThrow(id));
    }

    // ── helpers ──────────────────────────────────────────────

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Usuário não encontrado com id: " + id
                ));
    }

    private Usuario criarInstancia(TipoUsuario tipo) {
        return switch (tipo) {
            case OPERATOR -> new Operator();
            case VIEWER   -> new Viewer();
        };
    }

    private TipoUsuario resolverTipo(Usuario u) {
        if (u instanceof Operator) return TipoUsuario.OPERATOR;
        if (u instanceof Viewer)   return TipoUsuario.VIEWER;
        throw new IllegalStateException("Tipo de usuário desconhecido: " + u.getClass().getSimpleName());
    }

    private UsuarioListagemDTO toDTO(Usuario u) {
        return new UsuarioListagemDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getBase().getId(),
                resolverTipo(u)
        );
    }
}