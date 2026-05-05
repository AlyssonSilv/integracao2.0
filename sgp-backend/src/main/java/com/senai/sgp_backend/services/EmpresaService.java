package com.senai.sgp_backend.services;

import com.senai.sgp_backend.dto.EmpresaResponseDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // IMPORTANTE: Faltava este import

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional // Adicionado para garantir a transação
    public EmpresaResponseDTO salvarEmpresa(Empresa empresa) {
        // 1. Limpa o CNPJ (Garante 14 dígitos conforme sua @Pattern)
        if (empresa.getCnpj() != null) {
            empresa.setCnpj(empresa.getCnpj().replaceAll("\\D", ""));
        }

        // 2. Encripta a senha (Garante segurança e @Size min 6)
        if (empresa.getSenha() != null && !empresa.getSenha().startsWith("$2a$")) {
            empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));
        }

        // 3. Define o Role padrão usando o Enum interno da classe Empresa
        if (empresa.getRole() == null) {
            empresa.setRole(Empresa.EmpresaRole.USER);
        }

        // 4. Salva no banco e converte para DTO
        Empresa salva = empresaRepository.save(empresa);
        return EmpresaResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorCnpj(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("\\D", "");
        return empresaRepository.findByCnpj(cnpjLimpo);
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarTodas() {
        // Busca todas e mapeia cada uma para o DTO (Resolve o Type Mismatch do Controller)
        return empresaRepository.findAll().stream()
                .map(EmpresaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    } // Chave de fechamento que estava faltando

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada com o ID: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        Empresa empresa = buscarPorId(id);
        empresaRepository.delete(empresa);
    }
}