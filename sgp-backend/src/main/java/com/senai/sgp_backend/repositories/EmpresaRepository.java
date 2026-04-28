package com.senai.sgp_backend.repositories;

import com.senai.sgp_backend.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<UserDetails> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    // Novo método para buscar por CNPJ e Razão Social ignorando diferenças de
    // maiúsculas/minúsculas
    Optional<Empresa> findByCnpjAndRazaoSocialIgnoreCase(String cnpj, String razaoSocial);

    // Adicione este método para validar a combinação CNPJ + Nome
    Optional<Empresa> findByCnpjAndNomeResponsavelIgnoreCase(String cnpj, String nomeResponsavel);
}