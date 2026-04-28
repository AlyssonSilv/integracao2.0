package com.senai.sgp_backend.dto;

public record LoginResponseDTO(
    String token,
    String refreshToken,
    Long id,
    String razaoSocial,
    String cnpj,
    String email,
    String role
) {}