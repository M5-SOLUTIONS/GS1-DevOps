package br.com.m5_storage.entity.usuario;

import br.com.m5_storage.controller.UsuarioController;
import br.com.m5_storage.dto.usuario.UsuarioListagemDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioAssembler
        implements RepresentationModelAssembler<UsuarioListagemDTO, EntityModel<UsuarioListagemDTO>> {

    @Override
    public EntityModel<UsuarioListagemDTO> toModel(UsuarioListagemDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarTodos()).withRel("usuarios"),
                linkTo(methodOn(UsuarioController.class).listarPorBase(dto.baseId())).withRel("usuarios-da-base")
        );
    }
}