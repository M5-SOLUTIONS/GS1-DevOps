package br.com.m5_storage.dto.recurso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecursoAtualizarDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "Categoria é obrigatória")
        @Size(max = 50)
        String categoria,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Double quantidade,

        @NotNull(message = "Mínimo é obrigatório")
        @Positive(message = "Mínimo deve ser maior que zero")
        Double minimo,

        @NotNull(message = "Capacidade máxima é obrigatória")
        @Positive(message = "Capacidade máxima deve ser maior que zero")
        Double capacidadeMaxima,

        Boolean critico
) {}