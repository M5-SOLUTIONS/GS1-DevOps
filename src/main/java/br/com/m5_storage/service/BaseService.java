package br.com.m5_storage.service;

import br.com.m5_storage.dto.base.BaseAtualizarDTO;
import br.com.m5_storage.dto.base.BaseCadastroDTO;
import br.com.m5_storage.dto.base.BaseListagemDTO;
import br.com.m5_storage.entity.base.Base;
import br.com.m5_storage.exception.IdNaoEncontradoException;
import br.com.m5_storage.repository.BaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BaseService {

    private final BaseRepository baseRepository;

    public BaseService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    @Transactional
    public BaseListagemDTO createBase(BaseCadastroDTO dto) {
        Base base = Base.builder()
                .nome(dto.nome())
                .build();

        Base salvo = baseRepository.save(base);
        return toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<BaseListagemDTO> readAllBases() {
        return baseRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BaseListagemDTO readBaseById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public BaseListagemDTO updateBase(Long id, BaseAtualizarDTO dto) {
        Base base = findOrThrow(id);

        base.setNome(dto.nome());

        return toDTO(baseRepository.save(base));
    }

    @Transactional
    public void deleteBase(Long id) {
        baseRepository.delete(findOrThrow(id));
    }

    // ───────────────────────── helpers

    private Base findOrThrow(Long id) {
        return baseRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(
                        "Base não encontrada com id: " + id
                ));
    }

    private BaseListagemDTO toDTO(Base base) {
        return new BaseListagemDTO(
                base.getId(),
                base.getNome()
        );
    }
}