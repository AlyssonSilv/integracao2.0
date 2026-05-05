package com.senai.sgp_backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO ajustado para refletir exatamente as chaves enviadas pelo Power Automate.
 * As validações @NotBlank garantem que o Spring interrompa a requisição 
 * antes de tentar salvar no banco se algum dado crucial estiver faltando.
 */
public record WebhookFormsDTO(
    @NotBlank String nomeDaEmpresa,
    @NotBlank String cnpjDaEmpresa, 
    String telefoneEmpresa,
    String NomeDoResponsavel,      
    @NotBlank String emailDeContato,
    String telefoneDeContato,
    @NotBlank String treinamento,
    String quantidadeFuncionarios,
    String listaParticipantes,
    String dataSugerida,
    String validadeEHS,           
    String descricao,
    String confirmacaoInformacoes 
) {}