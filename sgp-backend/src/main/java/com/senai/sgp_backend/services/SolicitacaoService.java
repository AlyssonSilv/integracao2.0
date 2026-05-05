package com.senai.sgp_backend.services;

import com.senai.sgp_backend.dto.SolicitacaoResponseDTO;
import com.senai.sgp_backend.dto.WebhookFormsDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.models.Solicitacao;
import com.senai.sgp_backend.repositories.EmpresaRepository;
import com.senai.sgp_backend.repositories.SolicitacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * PROCESSAMENTO DO WEBHOOK (Forms -> Power Automate -> API)
     * Higieniza dados e evita erros 400 de ConstraintViolation.
     */
    @Transactional
    public SolicitacaoResponseDTO processarWebhook(WebhookFormsDTO payload) {

        // --- MODO DETETIVE (RAIO-X) LIGADO ---
        System.out.println("=====================================================");
        System.out.println(">>> 1. PAYLOAD COMPLETO CHEGOU DO AUTOMATE: " + payload);
        System.out.println(">>> 2. CNPJ EXTRAÍDO DO JSON: '" + payload.cnpjDaEmpresa() + "'");
        System.out.println("=====================================================");

        // 1. Limpeza garantida do CNPJ
        String cnpjOriginal = payload.cnpjDaEmpresa();
        String cnpjLimpo = cnpjOriginal != null ? cnpjOriginal.replaceAll("\\D", "") : "";

        // --- TRAVA DE SEGURANÇA E DEBUG ---
        if (cnpjLimpo.length() != 14) {
            throw new RuntimeException(
                    "ERRO DE DADOS DO FORMS: O CNPJ enviado não tem 14 números! " +
                            "O que chegou do Automate foi: '" + cnpjOriginal + "'. " +
                            "Após limpar, ficou com " + cnpjLimpo.length() + " números (" + cnpjLimpo + ").");
        }

        // 2. Busca ou Cria a Empresa com dados higienizados
        Empresa empresa = empresaRepository.findByCnpj(cnpjLimpo)
                .orElseGet(() -> {
                    Empresa nova = new Empresa();
                    nova.setCnpj(cnpjLimpo);
                    nova.setRazaoSocial(payload.nomeDaEmpresa());
                    nova.setNomeResponsavel(payload.NomeDoResponsavel());
                    nova.setTelefone(payload.telefoneEmpresa());

                    // Tratamento de E-mail: Se vier vazio ou inválido, cria um padrão
                    String emailEnviado = payload.emailDeContato();
                    if (emailEnviado == null || !emailEnviado.contains("@")) {
                        nova.setEmail("contato_" + cnpjLimpo + "@senai.com.br");
                    } else {
                        nova.setEmail(emailEnviado);
                    }

                    nova.setSenha(passwordEncoder.encode("SENAI@2026"));
                    nova.setRole(Empresa.EmpresaRole.USER); // Enum interno correto

                    return empresaRepository.save(nova);
                });

        // 3. Cria e vincula a nova Solicitação
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setEmpresa(empresa);
        solicitacao.setTreinamento(payload.treinamento());
        solicitacao.setListaParticipantes(payload.listaParticipantes());
        solicitacao.setDescricao(payload.descricao());

        if (payload.dataSugerida() != null && !payload.dataSugerida().trim().isEmpty()) {
            solicitacao.setDataSugerida(LocalDate.parse(payload.dataSugerida()));
        }

        solicitacao.setStatus("Nova");
        solicitacao.setProtocolo("CTE-" + System.currentTimeMillis());

        // 4. Calcula quantidade de participantes dinamicamente
        if (payload.listaParticipantes() != null) {
            int totalReal = (int) Arrays.stream(payload.listaParticipantes().split("\\R"))
                    .filter(nome -> !nome.trim().isEmpty())
                    .count();
            solicitacao.setQuantidadeParticipantes(totalReal);
        } else {
            solicitacao.setQuantidadeParticipantes(0);
        }

        Solicitacao salva = solicitacaoRepository.save(solicitacao);
        return SolicitacaoResponseDTO.fromEntity(salva);
    }

    /**
     * CRIAÇÃO MANUAL (React -> API)
     */
    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(Solicitacao solicitacao) {
        if (solicitacao.getProtocolo() == null || solicitacao.getProtocolo().isEmpty()) {
            solicitacao.setProtocolo("CTE-" + System.currentTimeMillis());
        }
        solicitacao.setStatus("Nova");

        if (solicitacao.getListaParticipantes() != null) {
            int totalReal = (int) Arrays.stream(solicitacao.getListaParticipantes().split("\\R"))
                    .filter(nome -> !nome.trim().isEmpty())
                    .count();
            solicitacao.setQuantidadeParticipantes(totalReal);
        }

        Solicitacao salva = solicitacaoRepository.save(solicitacao);
        return SolicitacaoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponseDTO> listarTodas() {
        return solicitacaoRepository.findAll().stream()
                .map(SolicitacaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponseDTO> listarPorEmpresa(Long empresaId) {
        return solicitacaoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(SolicitacaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getEstatisticas(Long empresaId) {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", solicitacaoRepository.countByEmpresaId(empresaId));
        stats.put("novas", solicitacaoRepository.countByEmpresaIdAndStatus(empresaId, "Nova"));
        stats.put("pendentes", solicitacaoRepository.countByEmpresaIdAndStatus(empresaId, "Pendente"));
        stats.put("agendadas", solicitacaoRepository.countByEmpresaIdAndStatus(empresaId, "Agendada"));
        return stats;
    }

    @Transactional
    public void atualizarStatus(Long id, String novoStatus, String instrutor, String sala, String horario) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com o ID: " + id));

        solicitacao.setStatus(novoStatus);

        if ("CONFIRMADO".equals(novoStatus)) {
            solicitacao.setInstrutor(instrutor);
            solicitacao.setSala(sala);
            solicitacao.setHorario(horario);
        }

        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void editarAgendamento(Long id, String status, String instrutor, String sala, String horario,
            LocalDate dataSugerida, String listaParticipantes, Integer quantidadeParticipantes) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        solicitacao.setStatus(status);
        solicitacao.setInstrutor(instrutor);
        solicitacao.setSala(sala);
        solicitacao.setHorario(horario);
        solicitacao.setDataSugerida(dataSugerida);

        if (listaParticipantes != null) {
            solicitacao.setListaParticipantes(listaParticipantes);
            solicitacao.setQuantidadeParticipantes(quantidadeParticipantes != null ? quantidadeParticipantes : 0);
        }

        solicitacaoRepository.save(solicitacao);
    }

    public SolicitacaoResponseDTO buscarPorId(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        return SolicitacaoResponseDTO.fromEntity(solicitacao);
    }
}