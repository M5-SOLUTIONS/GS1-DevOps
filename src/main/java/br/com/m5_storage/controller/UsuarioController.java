package br.com.m5_storage.controller;

import br.com.m5_storage.dto.usuario.UsuarioAtualizarDTO;
import br.com.m5_storage.dto.usuario.UsuarioCadastroDTO;
import br.com.m5_storage.dto.usuario.UsuarioListagemDTO;
import br.com.m5_storage.entity.usuario.UsuarioAssembler;
import br.com.m5_storage.service.UsuarioService;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAssembler assembler;

    public UsuarioController(UsuarioService usuarioService, UsuarioAssembler assembler) {
        this.usuarioService = usuarioService;
        this.assembler = assembler;
    }

    @Operation(summary = "Cadastra um usuário", responses = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioListagemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UsuarioListagemDTO>> criar(
            @RequestBody @Valid UsuarioCadastroDTO dto) {

        UsuarioListagemDTO criado = usuarioService.createUsuario(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(assembler.toModel(criado));
    }

    @Operation(summary = "Lista todos os usuários", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioListagemDTO.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UsuarioListagemDTO>>> listarTodos() {
        List<EntityModel<UsuarioListagemDTO>> lista = usuarioService.readAllUsuarios()
                .stream()
                .map(assembler::toModel)
                .toList();

        CollectionModel<EntityModel<UsuarioListagemDTO>> collection = CollectionModel.of(lista,
                linkTo(methodOn(UsuarioController.class).listarTodos()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Busca um usuário pelo ID", responses = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UsuarioListagemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioListagemDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(usuarioService.readUsuarioById(id)));
    }

    @Operation(summary = "Lista usuários por base", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioListagemDTO.class)))
    })
    @GetMapping("/base/{baseId}")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioListagemDTO>>> listarPorBase(
            @PathVariable Long baseId) {

        List<EntityModel<UsuarioListagemDTO>> lista = usuarioService.readUsuariosByBase(baseId)
                .stream()
                .map(assembler::toModel)
                .toList();

        CollectionModel<EntityModel<UsuarioListagemDTO>> collection =
                CollectionModel.of(lista,
                        linkTo(methodOn(UsuarioController.class).listarPorBase(baseId)).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Atualiza um usuário", responses = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioListagemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioListagemDTO>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioAtualizarDTO dto) {

        return ResponseEntity.ok(assembler.toModel(usuarioService.updateUsuario(id, dto)));
    }

    @Operation(summary = "Remove um usuário", responses = {
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

}