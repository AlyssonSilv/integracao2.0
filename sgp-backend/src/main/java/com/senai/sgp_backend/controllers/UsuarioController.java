package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.UsuarioResponseDTO;
import com.senai.sgp_backend.models.Empresa; // Certifique-se de importar o model Empresa
import com.senai.sgp_backend.models.Usuario;
import com.senai.sgp_backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder; // Importe necessário
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid Usuario usuario) {
        // CORREÇÃO: Recupera a Empresa que está autenticada no momento
        // O SecurityFilter colocou o objeto Empresa no SecurityContext
        Empresa empresaLogada = (Empresa) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Vincula a empresa ao novo usuário
        usuario.setEmpresa(empresaLogada);
        
        return ResponseEntity.ok(usuarioService.criarUsuario(usuario));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(empresaId));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
}