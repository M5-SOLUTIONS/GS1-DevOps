package br.com.m5_storage.service;

import br.com.m5_storage.dto.setor.SetorAtualizarDTO;
import br.com.m5_storage.dto.setor.SetorCadastroDTO;
import br.com.m5_storage.dto.setor.SetorListagemDTO;
import br.com.m5_storage.entity.base.Base;
import br.com.m5_storage.entity.setor.Setor;
import br.com.m5_storage.entity.setor.SetorInfo;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.repository.BaseRepository;
import br.com.m5_storage.repository.SetorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final BaseRepository baseRepository;

    public SetorService(SetorRepository setorRepository, BaseRepository baseRepository) {
        this.setorRepository = setorRepository;
        this.baseRepository = baseRepository;
    }

    @Transactional
    public SetorListagemDTO createSetor(SetorCadastroDTO dto) {
        Base base = baseRepository.findById(dto.baseId())
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Base não encontrada com id: " + dto.baseId()
                ));

        Setor setor = Setor.builder()
                .base(base)
                .info(new SetorInfo(dto.nome(), dto.descricao()))
                .build();

        return toDTO(setorRepository.save(setor));
    }

    @Transactional(readOnly = true)
    public List<SetorListagemDTO> readAllSetores() {
        return setorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SetorListagemDTO> readSetoresByBase(Long baseId) {
        return setorRepository.findByBaseId(baseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SetorListagemDTO readSetorById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public SetorListagemDTO updateSetor(Long id, SetorAtualizarDTO dto) {
        Setor setor = findOrThrow(id);
        setor.setInfo(new SetorInfo(dto.nome(), dto.descricao()));
        return toDTO(setorRepository.save(setor));
    }

    @Transactional
    public void deleteSetor(Long id) {
        setorRepository.delete(findOrThrow(id));
    }

    // ── helpers ──────────────────────────────────────────────

    public Setor findOrThrow(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Setor não encontrado com id: " + id
                ));
    }

    public SetorListagemDTO toDTO(Setor s) {
        return new SetorListagemDTO(
                s.getId(),
                s.getBase().getId(),
                s.getBase().getNome(),
                s.getInfo().getNome(),
                s.getInfo().getDescricao()
        );
    }
}