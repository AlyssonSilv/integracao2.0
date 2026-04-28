package com.senai.sgp_backend.dto;

import lombok.Data;

@Data
public class AtualizarStatusDTO {
    private String status;
    private String instrutor;
    private String sala;
    private String horario;
}