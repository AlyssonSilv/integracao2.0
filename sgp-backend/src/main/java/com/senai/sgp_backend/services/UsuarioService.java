package com.senai.sgp_backend.services;

import com.senai.sgp_backend.dto.UsuarioResponseDTO;
import com.senai.sgp_backend.models.Usuario;
import com.senai.sgp_backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criarUsuario(Usuario usuario) {
        // REFATORADO: Verificação de e-mail único antes do cadastro
        if (repository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario salvo = repository.save(usuario);
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    // REFATORADO: Método para listar apenas colaboradores da empresa logada
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarPorEmpresa(Long empresaId) {
        return repository.findByEmpresaId(empresaId).stream()
                .map(UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}