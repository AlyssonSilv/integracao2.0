package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.WebhookFormsDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.repositories.EmpresaRepository;
import java.time.LocalDate;
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
            // Retorna um erro 403 (Proibido) com uma mensagem clara para o React capturar
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("A agenda do dia foi encerrada. O horário limite para envio de solicitações é até as 16:30. Por favor, retorne amanhã.");
        }

        // Se estiver dentro do horário permitido, cria a solicitação normalmente
        return ResponseEntity.ok(solicitacaoService.criarSolicitacao(solicitacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }
    // ----------------------------------

    // LISTAGEM PARA ADMIN (Com ou sem filtro)
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

        String novoStatus = body.get("status");
        String instrutor = body.get("instrutor");
        String sala = body.get("sala");
        String horario = body.get("horario");

        // Repassa todos os dados para o Service
        solicitacaoService.atualizarStatus(id, novoStatus, instrutor, sala, horario);

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
                ? Integer.parseInt(body.get("quantidadeParticipantes").toString())
                : 0;

        String dataStr = body.get("dataSugerida");
        java.time.LocalDate dataSugerida = null;
        if (dataStr != null && !dataStr.trim().isEmpty()) {
            dataSugerida = java.time.LocalDate.parse(dataStr);
        }

        solicitacaoService.editarAgendamento(id, status, instrutor, sala, horario, dataSugerida, listaParticipantes,
                quantidadeParticipantes);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> receberWebhookForms(@RequestBody WebhookFormsDTO payload) {
        try {
            // 1. Busca a empresa no banco de dados local usando o CNPJ preenchido no Forms
            Empresa empresa = empresaRepository.findByCnpj(payload.cnpjEmpresa())
                    .orElseThrow(() -> new RuntimeException("Atenção: A empresa com CNPJ " + payload.cnpjEmpresa()
                            + " não está cadastrada no sistema."));

            // 2. Monta a nova Solicitação com os dados que vieram da nuvem
            Solicitacao novaSolicitacao = new Solicitacao();
            novaSolicitacao.setTreinamento(payload.titulo());
            novaSolicitacao.setTreinamentoOutros(payload.descricao());

            // Converte a data que vem como texto (YYYY-MM-DD) para LocalDate
            if (payload.dataSugerida() != null && !payload.dataSugerida().trim().isEmpty()) {
                novaSolicitacao.setDataSugerida(LocalDate.parse(payload.dataSugerida()));
            }

            novaSolicitacao.setListaParticipantes(payload.listaParticipantes());

            // Calcula a quantidade de participantes (obrigatório no banco)
            if (payload.listaParticipantes() != null) {
                int totalParticipantes = (int) java.util.Arrays.stream(payload.listaParticipantes().split("\\R"))
                        .filter(nome -> !nome.trim().isEmpty())
                        .count();
                novaSolicitacao.setQuantidadeParticipantes(totalParticipantes);
            } else {
                novaSolicitacao.setQuantidadeParticipantes(0);
            }

            novaSolicitacao.setEmpresa(empresa);

            // 3. Salva a demanda
            return ResponseEntity.ok(solicitacaoService.criarSolicitacao(novaSolicitacao));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar formulário: " + e.getMessage());
        }
    }
}