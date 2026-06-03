package br.com.m5_storage.controller;

import br.com.m5_storage.dto.base.BaseAtualizarDTO;
import br.com.m5_storage.dto.base.BaseCadastroDTO;
import br.com.m5_storage.dto.base.BaseListagemDTO;
import br.com.m5_storage.entity.base.BaseAssembler;
import br.com.m5_storage.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/bases")
@Tag(name = "Bases", description = "Gerenciamento de bases do sistema")
public class BaseController {

    private final BaseService baseService;
    private final BaseAssembler assembler;

    public BaseController(BaseService baseService, BaseAssembler assembler) {
        this.baseService = baseService;
        this.assembler = assembler;
    }

    @Operation(summary = "Cadastra uma base")
    @PostMapping
    public ResponseEntity<EntityModel<BaseListagemDTO>> criar(
            @RequestBody @Valid BaseCadastroDTO dto) {

        BaseListagemDTO criada = baseService.createBase(dto);

        URI location = linkTo(methodOn(BaseController.class)
                .buscarPorId(criada.id()))
                .toUri();

        return ResponseEntity.created(location)
                .body(assembler.toModel(criada));
    }

    @Operation(summary = "Lista todas as bases")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<BaseListagemDTO>>> listarTodas() {

        List<EntityModel<BaseListagemDTO>> lista = baseService.readAllBases()
                .stream()
                .map(assembler::toModel)
                .toList();

        CollectionModel<EntityModel<BaseListagemDTO>> collection =
                CollectionModel.of(lista,
                        linkTo(methodOn(BaseController.class).listarTodas()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Busca base por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<BaseListagemDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                assembler.toModel(baseService.readBaseById(id))
        );
    }

    @Operation(summary = "Atualiza base")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<BaseListagemDTO>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid BaseAtualizarDTO dto) {

        return ResponseEntity.ok(
                assembler.toModel(baseService.updateBase(id, dto))
        );
    }

    @Operation(summary = "Remove base")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        baseService.deleteBase(id);
        return ResponseEntity.noContent().build();
    }
}