package com.senai.sgp_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SolicitacaoDTO(
    Long id,
    @NotBlank(message = "O título é obrigatório") String titulo,
    @NotBlank(message = "A descrição é obrigatória") String descricao,
    @NotNull(message = "A data é obrigatória") LocalDate data,
    @NotBlank(message = "O status é obrigatório") String status,
    @NotNull(message = "ID da empresa é obrigatório") Long empresaId,
    Long usuarioId // Opcional no início
) {}