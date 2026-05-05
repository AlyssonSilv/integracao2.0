package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.WebhookFormsDTO;
import com.senai.sgp_backend.dto.SolicitacaoResponseDTO;
import com.senai.sgp_backend.models.Solicitacao;
import com.senai.sgp_backend.services.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    @Autowired
    private SolicitacaoService solicitacaoService;

    // REMOVIDO: EmpresaRepository não deve mais ser injetado aqui, pois a lógica
    // está no Service

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody @Valid Solicitacao solicitacao) {
        ZoneId fusoHorario = ZoneId.of("America/Fortaleza");
        LocalTime agora = LocalTime.now(fusoHorario);
        LocalTime limite = LocalTime.of(16, 30);

        if (agora.isAfter(limite)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("A agenda do dia foi encerrada. O horário limite para envio de solicitações é até as 16:30.");
        }

        // Chame buscarPorId ou criarSolicitacao se ainda existir no service para o
        // formulário manual do React
        return ResponseEntity.ok(solicitacaoService.criarSolicitacao(solicitacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }

    @GetMapping("/admin/todas")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarParaAdmin(
            @RequestParam(required = false) Long empresaId) {
        if (empresaId != null) {
            return ResponseEntity.ok(solicitacaoService.listarPorEmpresa(empresaId));
        }
        return ResponseEntity.ok(solicitacaoService.listarTodas());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(solicitacaoService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/stats/empresa/{empresaId}")
    public ResponseEntity<Map<String, Long>> obterEstatisticas(@PathVariable Long empresaId) {
        return ResponseEntity.ok(solicitacaoService.getEstatisticas(empresaId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        solicitacaoService.atualizarStatus(id, body.get("status"), body.get("instrutor"), body.get("sala"),
                body.get("horario"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/editar")
    public ResponseEntity<Void> editarAgendamento(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        String instrutor = body.get("instrutor");
        String sala = body.get("sala");
        String horario = body.get("horario");
        String listaParticipantes = body.get("listaParticipantes");
        Integer quantidadeParticipantes = body.get("quantidadeParticipantes") != null
                ? Integer.parseInt(body.get("quantidadeParticipantes"))
                : 0;

        java.time.LocalDate dataSugerida = null;
        if (body.get("dataSugerida") != null && !body.get("dataSugerida").trim().isEmpty()) {
            dataSugerida = java.time.LocalDate.parse(body.get("dataSugerida"));
        }

        solicitacaoService.editarAgendamento(id, status, instrutor, sala, horario, dataSugerida, listaParticipantes,
                quantidadeParticipantes);
        return ResponseEntity.ok().build();
    }

    /**
     * MÉTODO DO WEBHOOK COMPLETAMENTE CORRIGIDO:
     * Agora ele apenas repassa o payload para o Service, que faz o trabalho pesado.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> receberWebhookForms(
            @RequestHeader(value = "X-Auth-Secret", required = false) String secret,
            @RequestBody WebhookFormsDTO payload) {

        // 1. Validação do Segredo
        String minhaSenhaSecreta = "Senai2026@Maranhao!";
        if (!minhaSenhaSecreta.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Acesso negado: Segredo de autenticação inválido.");
        }

        try {
            // 2. Chame o novo método que criamos no Service
            // Ele resolve a criação da empresa, senha, e-mail e limpeza de CNPJ
            SolicitacaoResponseDTO response = solicitacaoService.processarWebhook(payload);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Se houver erro de validação (ex: CNPJ inválido), retorna 400
            return ResponseEntity.badRequest().body("Erro ao processar formulário: " + e.getMessage());
        }
    }
}