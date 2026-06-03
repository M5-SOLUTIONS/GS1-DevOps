package br.com.m5_storage.controller;

import br.com.m5_storage.dto.setor.SetorAtualizarDTO;
import br.com.m5_storage.dto.setor.SetorCadastroDTO;
import br.com.m5_storage.dto.setor.SetorListagemDTO;
import br.com.m5_storage.entity.setor.SetorAssembler;
import br.com.m5_storage.service.SetorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/setores")
@Tag(name = "Setores", description = "Gerenciamento de setores dentro das bases")
public class SetorController {

    private final SetorService setorService;
    private final SetorAssembler assembler;

    public SetorController(SetorService setorService, SetorAssembler assembler) {
        this.setorService = setorService;
        this.assembler = assembler;
    }

    @Operation(summary = "Cadastra um setor", responses = {
            @ApiResponse(responseCode = "201", description = "Setor cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = SetorListagemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Base não encontrada")
    })
    @PostMapping
    public ResponseEntity<EntityModel<SetorListagemDTO>> criar(
            @RequestBody @Valid SetorCadastroDTO dto) {

        SetorListagemDTO criado = setorService.createSetor(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();

        return ResponseEntity.created(location).body(assembler.toModel(criado));
    }

    @Operation(summary = "Lista todos os setores", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = SetorListagemDTO.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SetorListagemDTO>>> listarTodos() {
        List<EntityModel<SetorListagemDTO>> lista = setorService.readAllSetores()
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(SetorController.class).listarTodos()).withSelfRel()));
    }

    @Operation(summary = "Busca um setor pelo ID", responses = {
            @ApiResponse(responseCode = "200", description = "Setor encontrado",
                    content = @Content(schema = @Schema(implementation = SetorListagemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Setor não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<SetorListagemDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(setorService.readSetorById(id)));
    }

    @Operation(summary = "Lista setores por base", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = SetorListagemDTO.class)))
    })
    @GetMapping("/base/{baseId}")
    public ResponseEntity<CollectionModel<EntityModel<SetorListagemDTO>>> listarPorBase(
            @PathVariable Long baseId) {

        List<EntityModel<SetorListagemDTO>> lista = setorService.readSetoresByBase(baseId)
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(SetorController.class).listarPorBase(baseId)).withSelfRel(),
                linkTo(methodOn(SetorController.class).listarTodos()).withRel("todos")));
    }

    @Operation(summary = "Atualiza um setor", responses = {
            @ApiResponse(responseCode = "200", description = "Setor atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = SetorListagemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Setor não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<SetorListagemDTO>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid SetorAtualizarDTO dto) {

        return ResponseEntity.ok(assembler.toModel(setorService.updateSetor(id, dto)));
    }

    @Operation(summary = "Remove um setor", responses = {
            @ApiResponse(responseCode = "204", description = "Setor removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Setor não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        setorService.deleteSetor(id);
        return ResponseEntity.noContent().build();
    }
}