package com.senai.sgp_backend.security;

import com.senai.sgp_backend.models.Empresa;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        var token = this.recoverToken(request);

        if (token != null) {
            // Decodifica o token sem ir ao banco
            var decodedJWT = jwtService.validarEDecodificarToken(token);
            
            if (decodedJWT != null) {
                String cnpj = decodedJWT.getSubject();
                String roleStr = decodedJWT.getClaim("role").asString();

                // Instancia uma Empresa "fictícia" em memória apenas para o contexto de segurança do Spring
                Empresa empresaAuth = new Empresa();
                empresaAuth.setCnpj(cnpj);
                if (roleStr != null) {
                    empresaAuth.setRole(Empresa.EmpresaRole.valueOf(roleStr));
                }

                // Cria a autenticação injetando as Authorities baseadas na Role extraída do Token
                var authentication = new UsernamePasswordAuthenticationToken(empresaAuth, null, empresaAuth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null)
            return null;
        return authHeader.replace("Bearer ", "");
    }
}