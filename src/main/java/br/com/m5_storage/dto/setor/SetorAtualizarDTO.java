package br.com.m5_storage.dto.setor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorAtualizarDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome,

        @Size(max = 255)
        String descricao
) {}