package br.com.m5_storage.controller;

import br.com.m5_storage.entity.movimentacao.MovimentacaoAssembler;
import br.com.m5_storage.dto.movimentacao.MovimentacaoCadastroDTO;
import br.com.m5_storage.dto.movimentacao.MovimentacaoListagemDTO;
import br.com.m5_storage.entity.movimentacao.TipoMovimentacao;
import br.com.m5_storage.service.MovimentacaoService;
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
@RequestMapping("/movimentacoes")
@Tag(name = "Movimentações", description = "Registro de consumo e reabastecimento de recursos")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final MovimentacaoAssembler assembler;

    public MovimentacaoController(MovimentacaoService movimentacaoService,
                                  MovimentacaoAssembler assembler) {
        this.movimentacaoService = movimentacaoService;
        this.assembler = assembler;
    }

    // ── escrita: apenas Operator ─────────────────────────────

    @Operation(summary = "Registra uma movimentação — apenas Operator", responses = {
            @ApiResponse(responseCode = "201", description = "Movimentação registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = MovimentacaoListagemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente"),
            @ApiResponse(responseCode = "403", description = "Usuário não é Operator"),
            @ApiResponse(responseCode = "404", description = "Recurso ou usuário não encontrado")
    })
    @PostMapping
    public ResponseEntity<EntityModel<MovimentacaoListagemDTO>> registrar(
            @RequestBody @Valid MovimentacaoCadastroDTO dto) {

        MovimentacaoListagemDTO criado = movimentacaoService.registrarMovimentacao(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/recurso/{id}")
                .buildAndExpand(criado.recursoId()).toUri();

        return ResponseEntity.created(location).body(assembler.toModel(criado));
    }

    // ── leitura: qualquer usuário ────────────────────────────

    @Operation(summary = "Lista histórico de movimentações de um recurso", responses = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MovimentacaoListagemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    @GetMapping("/recurso/{recursoId}")
    public ResponseEntity<CollectionModel<EntityModel<MovimentacaoListagemDTO>>> listarPorRecurso(
            @PathVariable Long recursoId) {

        List<EntityModel<MovimentacaoListagemDTO>> lista =
                movimentacaoService.readMovimentacoesByRecurso(recursoId)
                        .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(MovimentacaoController.class).listarPorRecurso(recursoId)).withSelfRel()));
    }

    @Operation(summary = "Lista histórico de movimentações de um usuário", responses = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MovimentacaoListagemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<MovimentacaoListagemDTO>>> listarPorUsuario(
            @PathVariable Long usuarioId) {

        List<EntityModel<MovimentacaoListagemDTO>> lista =
                movimentacaoService.readMovimentacoesByUsuario(usuarioId)
                        .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(MovimentacaoController.class).listarPorUsuario(usuarioId)).withSelfRel()));
    }

    @Operation(summary = "Lista histórico de movimentações de um setor", responses = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MovimentacaoListagemDTO.class)))
    })
    @GetMapping("/setor/{setorId}")
    public ResponseEntity<CollectionModel<EntityModel<MovimentacaoListagemDTO>>> listarPorSetor(
            @PathVariable Long setorId) {

        List<EntityModel<MovimentacaoListagemDTO>> lista =
                movimentacaoService.readMovimentacoesBySetor(setorId)
                        .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(MovimentacaoController.class).listarPorSetor(setorId)).withSelfRel()));
    }

    @Operation(summary = "Lista movimentações de um setor filtradas por tipo")
    @GetMapping("/setor/{setorId}/tipo/{tipo}")
    public ResponseEntity<CollectionModel<EntityModel<MovimentacaoListagemDTO>>> listarPorSetorETipo(
            @PathVariable Long setorId,
            @PathVariable TipoMovimentacao tipo) {

        List<EntityModel<MovimentacaoListagemDTO>> lista =
                movimentacaoService.readMovimentacoesBySetorAndTipo(setorId, tipo)
                        .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(MovimentacaoController.class).listarPorSetorETipo(setorId, tipo)).withSelfRel(),
                linkTo(methodOn(MovimentacaoController.class).listarPorSetor(setorId)).withRel("setor")));
    }

    @Operation(summary = "Lista histórico de movimentações de uma base")
    @GetMapping("/base/{baseId}")
    public ResponseEntity<CollectionModel<EntityModel<MovimentacaoListagemDTO>>> listarPorBase(
            @PathVariable Long baseId) {

        List<EntityModel<MovimentacaoListagemDTO>> lista =
                movimentacaoService.readMovimentacoesByBase(baseId)
                        .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(MovimentacaoController.class).listarPorBase(baseId)).withSelfRel()));
    }
}