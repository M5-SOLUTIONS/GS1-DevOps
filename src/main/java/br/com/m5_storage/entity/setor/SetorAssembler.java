package br.com.m5_storage.entity.setor;

import br.com.m5_storage.controller.SetorController;
import br.com.m5_storage.dto.setor.SetorListagemDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class SetorAssembler
        implements RepresentationModelAssembler<SetorListagemDTO, EntityModel<SetorListagemDTO>> {

    @Override
    public EntityModel<SetorListagemDTO> toModel(SetorListagemDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(SetorController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(SetorController.class).listarTodos()).withRel("setores"),
                linkTo(methodOn(SetorController.class).listarPorBase(dto.baseId())).withRel("setores-da-base")
        );
    }
}