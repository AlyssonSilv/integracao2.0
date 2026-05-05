package com.senai.sgp_backend.models;

/**
 * Define os níveis de acesso do sistema SGP.
 * USER: Empresas que solicitam treinamentos.
 * ADMIN: Gestores do SENAI que aprovam e agendam.
 */
public enum Role {
    USER,
    ADMIN
}