package com.senai.sgp_backend.dto;

import com.senai.sgp_backend.models.Solicitacao;
import java.time.LocalDate;

public record SolicitacaoResponseDTO(
        Long id,
        String protocolo,
        String treinamento,
        String treinamentoOutros, 
        Integer quantidadeParticipantes,
        String listaParticipantes,
        LocalDate dataSugerida,
        String status,
        String nomeEmpresa,
        String instrutor,
        String sala,
        String horario) {
    public static SolicitacaoResponseDTO fromEntity(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getProtocolo(),
                s.getTreinamento(),
                s.getTreinamentoOutros(), 
                s.getQuantidadeParticipantes(),
                s.getListaParticipantes(),
                s.getDataSugerida(),
                s.getStatus(),
                s.getEmpresa() != null ? s.getEmpresa().getRazaoSocial() : "Empresa não identificada",
                s.getInstrutor(),
                s.getSala(),
                s.getHorario());
    }
}