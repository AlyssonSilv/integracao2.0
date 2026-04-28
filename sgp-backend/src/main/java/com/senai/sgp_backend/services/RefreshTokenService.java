package com.senai.sgp_backend.services;

import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.models.RefreshToken;
import com.senai.sgp_backend.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken criarRefreshToken(Empresa empresa) {
        refreshTokenRepository.findByEmpresa(empresa).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
        });

        RefreshToken refreshToken = RefreshToken.builder()
                .empresa(empresa)
                .token(UUID.randomUUID().toString())
                .dataExpiracao(Instant.now().plusSeconds(604800)) // 7 dias
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken validarExpiracao(RefreshToken token) {
        if (token.getDataExpiracao().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush(); // Garante que o token expirado suma do banco
            throw new RuntimeException("Refresh token expirado. Faça login novamente.");
        }
        return token;
    }
}