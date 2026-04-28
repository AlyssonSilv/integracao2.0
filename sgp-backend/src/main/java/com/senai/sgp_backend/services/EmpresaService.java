package com.senai.sgp_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.senai.sgp_backend.dto.EmpresaResponseDTO;
import com.senai.sgp_backend.exceptions.CnpjJaCadastradoException;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.repositories.EmpresaRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {
    @Autowired
    private EmpresaRepository repository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public EmpresaResponseDTO salvarEmpresa(Empresa empresa) {
        // Limpa o CNPJ para garantir a comparação correta
        String cnpjLimpo = empresa.getCnpj().replaceAll("[^0-9]", "");

        // Validação de duplicidade usando a exceção customizada
        if (repository.findByCnpj(cnpjLimpo).isPresent()) {
            throw new CnpjJaCadastradoException("Este CNPJ já está cadastrado no sistema.");
        }

        empresa.setCnpj(cnpjLimpo);
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));

        Empresa salva = repository.save(empresa);
        return EmpresaResponseDTO.fromEntity(salva);
    }

    // Método para evitar erros de compilação no Controller
    public List<EmpresaResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(EmpresaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}