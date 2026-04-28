import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const Dashboard: React.FC = () => {
  const [nomeEmpresa, setNomeEmpresa] = useState('Usuário');
  const [stats, setStats] = useState({
    total: 0,
    novas: 0,
    pendentes: 0,
    agendadas: 0
  });

  useEffect(() => {
    const carregarDashboard = async () => {
      try {
        // 1. Recuperar dados da empresa logada do localStorage
        const dados = localStorage.getItem('empresa_logada');
        if (!dados) return;
        const empresa = JSON.parse(dados);
        setNomeEmpresa(empresa.razaoSocial);

        // 2. MELHORIA: Consumo Otimizado
        // Em vez de buscar a lista completa e filtrar no frontend,
        // utilizamos o novo endpoint de estatísticas que já traz os números prontos.
        const response = await api.get(`/solicitacoes/stats/empresa/${empresa.id}`);
        setStats(response.data);

      } catch (error) {
        console.error("Erro ao carregar dados do Dashboard:", error);
      }
    };

    carregarDashboard();
  }, []);

  return (
    <div className="grid" style={{ gap: '20px' }}>
      
      {/* Mensagem de Boas-vindas Dinâmica */}
      <section className="card pad" style={{ gridColumn: 'span 12' }}>
        <div className="h1">Bem-vindo(a), {nomeEmpresa}!</div>
        <div className="p">Você está acessando o painel de controle da sua unidade.</div>
      </section>

      {/* Indicadores (KPIs) com Dados Reais vindo do Backend */}
      <section className="card pad" style={{ gridColumn: 'span 3' }}>
        <div className="kpi">
          <div className="dot" style={{ background: 'var(--primary)' }}></div>
          <div><div className="p">Total</div><div className="n">{stats.total}</div></div>
        </div>
      </section>

      <section className="card pad" style={{ gridColumn: 'span 3' }}>
        <div className="kpi">
          <div className="dot" style={{ background: 'var(--orange)' }}></div>
          <div><div className="p">Novas</div><div className="n">{stats.novas}</div></div>
        </div>
      </section>

      <section className="card pad" style={{ gridColumn: 'span 3' }}>
        <div className="kpi">
          <div className="dot" style={{ background: '#f59e0b' }}></div>
          <div><div className="p">Pendências</div><div className="n">{stats.pendentes}</div></div>
        </div>
      </section>

      <section className="card pad" style={{ gridColumn: 'span 3' }}>
        <div className="kpi">
          <div className="dot" style={{ background: '#0ea5e9' }}></div>
          <div><div className="p">Agendadas</div><div className="n">{stats.agendadas}</div></div>
        </div>
      </section>

      {/* Ações Rápidas */}
      <section className="card pad" style={{ gridColumn: 'span 7' }}>
        <div className="h2">Ações rápidas</div>
        <div className="actions" style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
          <Link to="/nova" className="btn primary">Nova Solicitação</Link>
          <Link to="/lista" className="btn secondary">Ver Minhas Solicitações</Link>
        </div>
      </section>

      {/* Avisos Informativos */}
      <section className="card pad" style={{ gridColumn: 'span 5' }}>
        <div className="h2">Avisos</div>
        <div className="p" style={{ marginTop: '10px', lineHeight: '1.6' }}>
          • Verifique os dados dos participantes antes de enviar.<br/>
          • O prazo de análise é de até 48 horas úteis.<br/>
          • Mantenha seu telefone de contato atualizado.
        </div>
      </section>

    </div>
  );
};

export default Dashboard;