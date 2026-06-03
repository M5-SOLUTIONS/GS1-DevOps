package br.com.m5_storage.controller;

import br.com.m5_storage.entity.alerta.AlertaAssembler;
import br.com.m5_storage.dto.alerta.AlertaListagemDTO;
import br.com.m5_storage.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/alertas")
@Tag(name = "Alertas", description = "Gerenciamento de alertas de recursos por setor e base")
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaAssembler assembler;

    public AlertaController(AlertaService alertaService, AlertaAssembler assembler) {
        this.alertaService = alertaService;
        this.assembler = assembler;
    }

    // ── leitura: qualquer usuário ────────────────────────────

    @Operation(summary = "Lista todos os alertas ativos (dashboard)", responses = {
            @ApiResponse(responseCode = "200", description = "Alertas ativos retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaListagemDTO.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AlertaListagemDTO>>> listarAtivos() {
        List<EntityModel<AlertaListagemDTO>> lista = alertaService.readAlertasAtivos()
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(AlertaController.class).listarAtivos()).withSelfRel()));
    }

    @Operation(summary = "Lista alertas ativos por recurso", responses = {
            @ApiResponse(responseCode = "200", description = "Alertas retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaListagemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    @GetMapping("/recurso/{recursoId}")
    public ResponseEntity<CollectionModel<EntityModel<AlertaListagemDTO>>> listarPorRecurso(
            @PathVariable Long recursoId) {

        List<EntityModel<AlertaListagemDTO>> lista = alertaService.readAlertasByRecurso(recursoId)
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(AlertaController.class).listarPorRecurso(recursoId)).withSelfRel(),
                linkTo(methodOn(AlertaController.class).listarAtivos()).withRel("todos-ativos")));
    }

    @Operation(summary = "Lista alertas ativos por setor", responses = {
            @ApiResponse(responseCode = "200", description = "Alertas do setor retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaListagemDTO.class)))
    })
    @GetMapping("/setor/{setorId}")
    public ResponseEntity<CollectionModel<EntityModel<AlertaListagemDTO>>> listarPorSetor(
            @PathVariable Long setorId) {

        List<EntityModel<AlertaListagemDTO>> lista = alertaService.readAlertasBySetor(setorId)
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(AlertaController.class).listarPorSetor(setorId)).withSelfRel(),
                linkTo(methodOn(AlertaController.class).listarAtivos()).withRel("todos-ativos")));
    }

    @Operation(summary = "Lista alertas ativos por base", responses = {
            @ApiResponse(responseCode = "200", description = "Alertas da base retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaListagemDTO.class)))
    })
    @GetMapping("/base/{baseId}")
    public ResponseEntity<CollectionModel<EntityModel<AlertaListagemDTO>>> listarPorBase(
            @PathVariable Long baseId) {

        List<EntityModel<AlertaListagemDTO>> lista = alertaService.readAlertasByBase(baseId)
                .stream().map(assembler::toModel).toList();

        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(AlertaController.class).listarPorBase(baseId)).withSelfRel(),
                linkTo(methodOn(AlertaController.class).listarAtivos()).withRel("todos-ativos")));
    }

    // ── escrita: apenas Operator ─────────────────────────────

    @Operation(summary = "Resolve um alerta manualmente — apenas Operator", responses = {
            @ApiResponse(responseCode = "200", description = "Alerta resolvido com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaListagemDTO.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é Operator"),
            @ApiResponse(responseCode = "404", description = "Alerta ou usuário não encontrado")
    })
    @PatchMapping("/{id}/resolver")
    public ResponseEntity<EntityModel<AlertaListagemDTO>> resolverAlerta(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        return ResponseEntity.ok(assembler.toModel(alertaService.resolverAlerta(id, usuarioId)));
    }
}