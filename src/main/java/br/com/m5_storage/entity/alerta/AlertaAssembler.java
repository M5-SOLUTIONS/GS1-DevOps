package br.com.m5_storage.entity.alerta;

import br.com.m5_storage.controller.AlertaController;
import br.com.m5_storage.dto.alerta.AlertaListagemDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AlertaAssembler
        implements RepresentationModelAssembler<AlertaListagemDTO, EntityModel<AlertaListagemDTO>> {

    @Override
    public EntityModel<AlertaListagemDTO> toModel(AlertaListagemDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(AlertaController.class)
                        .listarAtivos()).withRel("alertas-ativos"),

                linkTo(methodOn(AlertaController.class)
                        .listarPorRecurso(dto.recursoId())).withRel("alertas-recurso"),

                linkTo(methodOn(AlertaController.class)
                        .listarPorSetor(dto.setorId())).withRel("alertas-setor")
        );
    }
}