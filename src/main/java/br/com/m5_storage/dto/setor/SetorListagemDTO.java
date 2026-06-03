package br.com.m5_storage.dto.setor;

public record SetorListagemDTO(
        Long id,
        Long baseId,
        String baseNome,
        String nome,
        String descricao
) {}