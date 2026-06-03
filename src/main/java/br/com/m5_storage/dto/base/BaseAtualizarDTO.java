package br.com.m5_storage.dto.base;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BaseAtualizarDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome
) {}