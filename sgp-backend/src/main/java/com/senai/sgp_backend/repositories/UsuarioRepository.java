package com.senai.sgp_backend.repositories;

import com.senai.sgp_backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);

    // REFATORADO: Busca usuários vinculados apenas a uma empresa específica
    List<Usuario> findByEmpresaId(Long empresaId);

    // REFATORADO: Auxilia na validação de e-mails duplicados
    boolean existsByEmail(String email);
}