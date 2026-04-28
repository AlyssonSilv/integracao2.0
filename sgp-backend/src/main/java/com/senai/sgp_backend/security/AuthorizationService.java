package com.senai.sgp_backend.security;

import com.senai.sgp_backend.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    private EmpresaRepository repository;

    @Override
    public UserDetails loadUserByUsername(String cnpj) throws UsernameNotFoundException {
        // Remove qualquer caractere não numérico caso o frontend envie com máscara por engano
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        return repository.findByCnpj(cnpjLimpo)
                .orElseThrow(() -> new UsernameNotFoundException("Empresa não encontrada com o CNPJ: " + cnpjLimpo));
    }
}