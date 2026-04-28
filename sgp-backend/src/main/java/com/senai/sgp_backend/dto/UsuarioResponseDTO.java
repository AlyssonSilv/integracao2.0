package com.senai.sgp_backend.dto;

import com.senai.sgp_backend.models.Usuario;

public record UsuarioResponseDTO (
    Long id,
    String nome,
    String email,
    String cargo
){

   public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo()
        );
    }   

}
    

