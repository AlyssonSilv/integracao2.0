package com.senai.sgp_backend.dto;

import com.senai.sgp_backend.models.Empresa;

public record EmpresaResponseDTO(
    Long id,
    String razaoSocial,
    String cnpj,
    String email,
    String telefone,
    String nomeResponsavel,
    String role,
    String logoUrl // 1. Adicionado o campo no record
) {
    public static EmpresaResponseDTO fromEntity(Empresa empresa) {
        return new EmpresaResponseDTO(
            empresa.getId(),
            empresa.getRazaoSocial(),
            empresa.getCnpj(),
            empresa.getEmail(),
            empresa.getTelefone(),
            empresa.getNomeResponsavel(),
            // MAPEADO: Envia o papel da empresa para o Frontend
            empresa.getRole() != null ? empresa.getRole().name() : "USER",
            empresa.getLogoUrl() // 2. Mapeado o novo campo aqui
        );
    }
}