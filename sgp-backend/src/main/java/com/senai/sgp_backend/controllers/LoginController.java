package com.senai.sgp_backend.controllers;

import com.senai.sgp_backend.dto.LoginRequestDTO;
import com.senai.sgp_backend.dto.LoginResponseDTO;
import com.senai.sgp_backend.models.Empresa;
import com.senai.sgp_backend.models.RefreshToken;
import com.senai.sgp_backend.repositories.EmpresaRepository;
import com.senai.sgp_backend.repositories.RefreshTokenRepository;
import com.senai.sgp_backend.security.JwtService;
import com.senai.sgp_backend.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data) {
        // Busca pela combinação exata de CNPJ e Nome do Responsável
        return empresaRepository.findByCnpjAndNomeResponsavelIgnoreCase(data.cnpj(), data.nomeResponsavel())
                .map(empresa -> {
                    String token = jwtService.gerarToken(empresa);
                    RefreshToken refreshToken = refreshTokenService.criarRefreshToken(empresa);

                    return ResponseEntity.ok(new LoginResponseDTO(
                            token,
                            refreshToken.getToken(),
                            empresa.getId(),
                            empresa.getRazaoSocial(),
                            empresa.getCnpj(),
                            empresa.getEmail(),
                            empresa.getRole().name()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {

        String tokenDeRenovacao = request.get("refreshToken");

        return refreshTokenRepository.findByToken(tokenDeRenovacao)
                .map(refreshTokenService::validarExpiracao)
                .map(RefreshToken::getEmpresa)
                .map(empresa -> {
                    String novoAccessToken = jwtService.gerarToken(empresa);
                    return ResponseEntity.ok(Map.of(
                            "token", novoAccessToken,
                            "refreshToken", tokenDeRenovacao));
                })
                .orElseThrow(() -> new RuntimeException("Refresh Token inválido ou não encontrado."));
    }
}