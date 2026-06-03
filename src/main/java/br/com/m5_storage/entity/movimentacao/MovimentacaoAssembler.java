package br.com.m5_storage.entity.movimentacao;

import br.com.m5_storage.controller.MovimentacaoController;
import br.com.m5_storage.dto.movimentacao.MovimentacaoListagemDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class MovimentacaoAssembler
        implements RepresentationModelAssembler<MovimentacaoListagemDTO, EntityModel<MovimentacaoListagemDTO>> {

    @Override
    public EntityModel<MovimentacaoListagemDTO> toModel(MovimentacaoListagemDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(MovimentacaoController.class)
                        .listarPorRecurso(dto.recursoId())).withRel("movimentacoes-recurso"),

                linkTo(methodOn(MovimentacaoController.class)
                        .listarPorUsuario(dto.usuarioId())).withRel("movimentacoes-usuario"),

                linkTo(methodOn(MovimentacaoController.class)
                        .listarPorSetor(dto.setorId())).withRel("movimentacoes-setor")
        );
    }
}