package com.senai.sgp_backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "solicitacoes")
@Data
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String protocolo;

    @Column(nullable = false)
    private String treinamento;

    private String treinamentoOutros;

    @Column(nullable = false)
    private Integer quantidadeParticipantes;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String listaParticipantes;

    @Column(nullable = false)
    private LocalDate dataSugerida;

    @Column(nullable = false)
    private String status; 

    // --- CAMPO ADICIONADO PARA CORREÇÃO ---
    @Column(columnDefinition = "TEXT")
    private String descricao;
    // ---------------------------------------

    private String instrutor;
    private String sala;
    private String horario;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}