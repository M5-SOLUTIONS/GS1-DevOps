package br.com.m5_storage.dto.usuario;

public record UsuarioListagemDTO(
        Long id,
        String nome,
        String email,
        Long baseId,
        TipoUsuario tipoUsuario
) {}