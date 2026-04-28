package com.senai.sgp_backend.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.senai.sgp_backend.models.Solicitacao;
import com.senai.sgp_backend.repositories.SolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final TemplateEngine templateEngine;

    public PdfService(SolicitacaoRepository solicitacaoRepository, TemplateEngine templateEngine) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.templateEngine = templateEngine;
    }

    public void exportarAgendaConfirmada(OutputStream outputStream) {
        try {
            // 1. Define a data de amanhã
            LocalDate amanha = LocalDate.now().plusDays(1);

            // 2. Busca apenas solicitações CONFIRMADAS para AMANHÃ
            List<Solicitacao> confirmadas = solicitacaoRepository.findByStatusAndDataSugerida("CONFIRMADO", amanha);

            // Ordenação: Treinamentos normais primeiro, "Outros" sempre no final
            confirmadas.sort((s1, s2) -> {
                boolean isOutros1 = s1.getTreinamento() != null
                        && s1.getTreinamento().equalsIgnoreCase("Outros (especificar)");
                boolean isOutros2 = s2.getTreinamento() != null
                        && s2.getTreinamento().equalsIgnoreCase("Outros (especificar)");

                if (isOutros1 && !isOutros2)
                    return 1;
                if (!isOutros1 && isOutros2)
                    return -1;

                // Como todos são do mesmo dia, ordenamos agora pelo horário (se houver)
                if (s1.getHorario() != null && s2.getHorario() != null) {
                    return s1.getHorario().compareTo(s2.getHorario());
                }

                return 0;
            });

            System.out.println("========================================");
            System.out.println("GERANDO PDF DA AGENDA - Data Alvo: " + amanha);
            System.out.println("Solicitações para amanhã: " + confirmadas.size());

            // 3. Prepara os dados para o HTML
            Context context = new Context();
            context.setVariable("solicitacoes", confirmadas);

            // Enviamos a data formatada para o HTML (ex: 24/04/2026)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            context.setVariable("dataAgenda", amanha.format(formatter));

            // 4. O Thymeleaf processa o arquivo "agenda.html"
            String htmlProcessado = templateEngine.process("agenda", context);
            System.out.println("Thymeleaf processou o template 'agenda.html' com sucesso.");

            // 5. Lógica para encontrar as imagens na pasta static
            String baseUri = "";
            try {
                URL res = getClass().getResource("/static/");
                if (res != null) {
                    baseUri = res.toURI().toString();
                } else {
                    File staticDir = new File("src/main/resources/static/");
                    if (staticDir.exists()) {
                        baseUri = staticDir.toURI().toString();
                    }
                }
            } catch (Exception e) {
                System.err.println("Aviso: Falha ao resolver o baseUri.");
            }

            // 6. Converte o HTML para PDF
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlProcessado, baseUri);
            builder.toStream(outputStream);
            builder.run();

            System.out.println("PDF da agenda de amanhã gerado com sucesso!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO NA GERAÇÃO DO PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao converter HTML para PDF: " + e.getMessage(), e);
        }
    }
}