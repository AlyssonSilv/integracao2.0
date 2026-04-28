package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.EmpresaResponseDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.services.EmpresaService; // Import do Service
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService; // REFATORADO: Agora injetamos apenas o Service

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> cadastrar(@RequestBody @Valid Empresa empresa) {
        // REFATORADO: Chamamos o Service, que contém a lógica de segurança e validação
        EmpresaResponseDTO salva = empresaService.salvarEmpresa(empresa);
        return ResponseEntity.ok(salva);
    }

    @GetMapping
    public List<EmpresaResponseDTO> listarTodas() {
        // REFATORADO: Chamamos a listagem através do Service
        return empresaService.listarTodas();
    }
}