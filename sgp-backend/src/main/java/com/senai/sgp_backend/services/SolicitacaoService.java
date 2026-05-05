package com.senai.sgp_backend.services;

import com.senai.sgp_backend.dto.SolicitacaoResponseDTO;
import com.senai.sgp_backend.dto.WebhookFormsDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.models.Solicitacao;
import com.senai.sgp_backend.repositories.EmpresaRepository;
import com.senai.sgp_backend.repositories.SolicitacaoRepository;

import jakarta.validation.Valid;

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

    @Transactional
    public SolicitacaoResponseDTO processarWebhook(WebhookFormsDTO payload) {

        // 1. LIMPEZA IMEDIATA: Garante que o CNPJ tenha apenas 14 números
        // Isso resolve o erro "O CNPJ deve conter exatamente 14 dígitos numéricos"
        String cnpjLimpo = payload.cnpjDaEmpresa().replaceAll("\\D", "");

        // 2. BUSCA OU CRIA AUTOMATICAMENTE
        Empresa empresa = empresaRepository.findByCnpj(cnpjLimpo)
                .orElseGet(() -> {
                    Empresa nova = new Empresa();
                    nova.setCnpj(cnpjLimpo);
                    nova.setRazaoSocial(payload.nomeDaEmpresa());
                    nova.setNomeResponsavel(payload.NomeDoResponsavel());
                    nova.setTelefone(payload.telefoneEmpresa());

                    // SATISFAZ O BANCO: Preenche e-mail e senha sem exigir login do usuário
                    // Resolve "O e-mail é obrigatório" e "A senha é obrigatória"
                    nova.setEmail(payload.emailDeContato());
                    nova.setSenha(passwordEncoder.encode("SENAI@2026"));

                    // Usa o Enum interno da sua classe Empresa para evitar erro de import
                    nova.setRole(Empresa.EmpresaRole.USER);

                    return empresaRepository.save(nova);
                });

        // 3. VINCULA A SOLICITAÇÃO
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setEmpresa(empresa);
        solicitacao.setTreinamento(payload.treinamento());
        solicitacao.setListaParticipantes(payload.listaParticipantes());
        solicitacao.setDescricao(payload.descricao());

        if (payload.dataSugerida() != null && !payload.dataSugerida().isEmpty()) {
            solicitacao.setDataSugerida(LocalDate.parse(payload.dataSugerida()));
        }

        solicitacao.setStatus("Nova");
        solicitacao.setProtocolo("CTE-" + System.currentTimeMillis());

        // 4. CÁLCULO DE PARTICIPANTES
        if (payload.listaParticipantes() != null) {
            int totalReal = (int) Arrays.stream(payload.listaParticipantes().split("\\R"))
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

    public void confirmarSolicitacao(Long id) throws Exception {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new Exception("Solicitação não encontrada"));

        boolean dataOcupada = solicitacaoRepository.existsByDataSugeridaAndStatus(solicitacao.getDataSugerida(),
                "CONFIRMADO");

        if (dataOcupada) {
            throw new Exception("A data sugerida já possui um agendamento confirmado na agenda.");
        }

        solicitacao.setStatus("CONFIRMADO");
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

    public Object criarSolicitacao(Solicitacao solicitacao) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'criarSolicitacao'");
    }
}