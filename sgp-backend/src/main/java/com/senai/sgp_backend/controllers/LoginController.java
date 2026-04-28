package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.SolicitacaoResponseDTO;
import com.senai.sgp_backend.dto.WebhookFormsDTO; // ✨ IMPORT NOVO
import com.senai.sgp_backend.models.Empresa; // ✨ IMPORT NOVO
import com.senai.sgp_backend.models.Solicitacao;
import com.senai.sgp_backend.repositories.EmpresaRepository; // ✨ IMPORT NOVO
import com.senai.sgp_backend.services.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    @Autowired
    private SolicitacaoService solicitacaoService;

    // ✨ INJEÇÃO NOVA: Necessária para buscar a empresa pelo CNPJ que vier do Forms
    @Autowired
    private EmpresaRepository empresaRepository;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody @Valid Solicitacao solicitacao) {
        // 1. Pega a hora atual usando o fuso horário correto (Maranhão / Brasília)
        ZoneId fusoHorario = ZoneId.of("America/Fortaleza");
        LocalTime agora = LocalTime.now(fusoHorario);

        // 2. Define o limite (16h30)
        LocalTime limite = LocalTime.of(16, 30);

        // 3. A Trava de Segurança Invencível do Backend
        if (agora.isAfter(limite)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("A agenda do dia foi encerrada. O horário limite para envio de solicitações é até as 16:30. Por favor, retorne amanhã.");
        }

        return ResponseEntity.ok(solicitacaoService.criarSolicitacao(solicitacao));
    }

    // ... (MANTENHA TODOS OS SEUS OUTROS MÉTODOS AQUI: buscarPorId,
    // listarParaAdmin, atualizarStatus, editarAgendamento, etc) ...

    // ✨ NOVA ROTA DO WEBHOOK DO FORMS ✨
    @PostMapping("/webhook")
    public ResponseEntity<?> receberWebhookForms(@RequestBody WebhookFormsDTO payload) {
        try {
            // 1. Busca a empresa no banco de dados local usando o CNPJ preenchido no Forms
            Empresa empresa = empresaRepository.findByCnpj(payload.cnpjEmpresa())
                    .orElseThrow(() -> new RuntimeException("Atenção: A empresa com CNPJ " + payload.cnpjEmpresa()
                            + " não está cadastrada no sistema do SENAI."));

            // 2. Monta a nova Solicitação com os dados que vieram da nuvem
            Solicitacao novaSolicitacao = new Solicitacao();
            novaSolicitacao.setTitulo(payload.titulo());
            novaSolicitacao.setDescricao(payload.descricao());

            // Converte a data que vem como texto (YYYY-MM-DD) para LocalDate
            if (payload.dataSugerida() != null && !payload.dataSugerida().trim().isEmpty()) {
                novaSolicitacao.setDataSugerida(LocalDate.parse(payload.dataSugerida()));
            }

            novaSolicitacao.setListaParticipantes(payload.listaParticipantes());
            novaSolicitacao.setEmpresa(empresa);

            // 3. Salva a demanda (o Service já cuida de gerar protocolo, status 'Nova',
            // etc.)
            return ResponseEntity.ok(solicitacaoService.criarSolicitacao(novaSolicitacao));

        } catch (Exception e) {
            // Retorna erro 400 se algo der errado (ex: CNPJ não cadastrado)
            return ResponseEntity.badRequest().body("Erro ao processar formulário: " + e.getMessage());
        }
    }
}