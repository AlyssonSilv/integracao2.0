package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.services.SolicitacaoService;
import com.senai.sgp_backend.services.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/analitico")
public class AdminAnaliticoController {

    private final SolicitacaoService solicitacaoService;
    private final PdfService pdfService;

    public AdminAnaliticoController(SolicitacaoService solicitacaoService, PdfService pdfService) {
        this.solicitacaoService = solicitacaoService;
        this.pdfService = pdfService;
    }

    @PutMapping("/solicitacoes/{id}/confirmar")
    public ResponseEntity<?> confirmarAgendamento(@PathVariable Long id) {
        try {
            solicitacaoService.confirmarSolicitacao(id);
            return ResponseEntity.ok().body("Agendamento confirmado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/agenda/pdf")
    public void gerarPdfAgenda(HttpServletResponse response) throws IOException {

        LocalDate amanha = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fileName = "AGENDA CTA-SENAI " + amanha.format(formatter) + ".pdf";

        response.setContentType("application/pdf");

        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        pdfService.exportarAgendaConfirmada(response.getOutputStream());
    }
}