package br.com.m5_storage.dto.movimentacao;

import br.com.m5_storage.entity.movimentacao.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MovimentacaoCadastroDTO(

        @NotNull(message = "Recurso é obrigatório")
        Long recursoId,

        @NotNull(message = "Usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "Tipo de movimentação é obrigatório")
        TipoMovimentacao tipoMovimentacao,

        // Regra 13: quantidade > 0
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Double quantidade,

        @Size(max = 255)
        String descricao
) {}