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

    // columnDefinition = "TEXT" permite textos longos com quebra de linha
    @Column(columnDefinition = "TEXT", nullable = false)
    private String listaParticipantes;

    @Column(nullable = false)
    private LocalDate dataSugerida;

    @Column(nullable = false)
    private String status; // Ex: "Nova", "Agendada", "Em Triagem"

    // --- NOVOS CAMPOS PARA A AGENDA DO CTA ---
    private String instrutor;
    private String sala;
    private String horario;
    // -----------------------------------------

    // Relacionamento: A solicitação pertence a uma Empresa (FAT)
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}