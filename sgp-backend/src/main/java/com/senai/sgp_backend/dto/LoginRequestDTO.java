package com.senai.sgp_backend.dto;

// Alterado para exigir o CNPJ e o Nome do Responsável
public record LoginRequestDTO(String cnpj, String nomeResponsavel) {
}