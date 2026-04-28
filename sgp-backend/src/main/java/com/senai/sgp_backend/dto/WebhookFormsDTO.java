package com.senai.sgp_backend.dto;

public record WebhookFormsDTO(
    String cnpjEmpresa, 
    String titulo,
    String descricao,
    String dataSugerida,
    String listaParticipantes
) {}