package br.com.m5_storage.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(max = 100)
        String senha,

        @NotNull(message = "Base é obrigatória")
        Long baseId,

        @NotNull(message = "Tipo de usuário é obrigatório")
        TipoUsuario tipoUsuario
) {}