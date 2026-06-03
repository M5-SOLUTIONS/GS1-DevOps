package br.com.m5_storage.entity.base;

import br.com.m5_storage.controller.BaseController;
import br.com.m5_storage.dto.base.BaseListagemDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class BaseAssembler
        implements RepresentationModelAssembler<BaseListagemDTO, EntityModel<BaseListagemDTO>> {

    @Override
    public EntityModel<BaseListagemDTO> toModel(BaseListagemDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(BaseController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(BaseController.class).listarTodas()).withRel("bases")
        );
    }
}