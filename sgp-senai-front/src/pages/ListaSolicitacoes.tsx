import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ListaSolicitacoes: React.FC = () => {
  const [solicitacoes, setSolicitacoes] = useState([]);
  const [loading, setLoading] = useState(true);

  // Estados para o Modal de Alunos
  const [modalAberto, setModalAberto] = useState(false);
  const [alunosSelecionados, setAlunosSelecionados] = useState("");
  const [protocoloAtivo, setProtocoloAtivo] = useState("");

  useEffect(() => {
    const buscarDados = async () => {
      try {
        const empresaStorage = localStorage.getItem('empresa_logada');
        if (!empresaStorage) return;
        const empresa = JSON.parse(empresaStorage);
        const response = await api.get(`/solicitacoes/empresa/${empresa.id}`);
        setSolicitacoes(response.data);
      } catch (error) {
        console.error("Erro ao carregar:", error);
      } finally {
        setLoading(false);
      }
    };
    buscarDados();
  }, []);

  const abrirDetalhes = (lista: string, protocolo: string) => {
    setAlunosSelecionados(lista);
    setProtocoloAtivo(protocolo);
    setModalAberto(true);
  };

  // Função para formatar a data corrigindo o problema de fuso horário (Timezone)
  const formatarDataManual = (dataBanco: string) => {
    if (!dataBanco) return "--/--/----";
    // O banco envia "AAAA-MM-DD" ou "AAAA-MM-DDT00:00:00"
    // Pegamos apenas a parte da data e quebramos pelo hífen
    const [ano, mes, dia] = dataBanco.split('T')[0].split('-');
    return `${dia}/${mes}/${ano}`;
  };

  if (loading) return <div className="pad">Carregando...</div>;

  return (
    <section className="card" style={{ position: 'relative' }}>
      <header className="pad border-b" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ margin: 0 }}>Minhas Solicitações</h2>
          <p className="p" style={{ margin: '4px 0 0 0' }}>Acompanhe o status dos treinamentos.</p>
        </div>
        <Link to="/nova" className="btn primary" style={{ whiteSpace: 'nowrap' }}>+ Nova Solicitação</Link>
      </header>

      <table className="table">
        <thead>
          <tr>
            <th>Protocolo</th>
            <th>Treinamento</th>
            <th>Participantes</th>
            <th>Data Sugerida</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {solicitacoes.map((req: any) => (
            <tr key={req.id}>
              <td><b>{req.protocolo}</b></td>
              <td>{req.treinamento}</td>
              <td>{req.quantidadeParticipantes} alunos</td>
              {/* APLICAÇÃO DA CORREÇÃO DE DATA AQUI */}
              <td>{formatarDataManual(req.dataSugerida)}</td>
              <td><span className={`badge ${req.status === 'Nova' ? 'badge-nova' : 'badge-pendente'}`}>{req.status}</span></td>
              <td>
                <button
                  className="btn ghost sm"
                  onClick={() => abrirDetalhes(req.listaParticipantes, req.protocolo)}
                >
                  Ver Alunos
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* MODAL SIMPLES PARA VER ALUNOS */}
      {modalAberto && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          backgroundColor: 'rgba(0,0,0,0.8)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="card pad" style={{
            width: '450px',
            backgroundColor: '#1a1a1a',
            color: '#fff',
            borderRadius: '12px',
            border: '1px solid #333'
          }}>
            <h3 style={{ borderBottom: '1px solid #333', paddingBottom: '10px', color: '#fff' }}>
              Alunos - {protocoloAtivo}
            </h3>

            <div style={{
              backgroundColor: '#2d2d2d',
              padding: '20px',
              borderRadius: '8px',
              marginTop: '15px',
              maxHeight: '300px',
              overflowY: 'auto',
              color: '#e0e0e0',
              fontSize: '16px',
              lineHeight: '1.6',
              whiteSpace: 'pre-line',
              textAlign: 'left',
              border: '1px solid #444'
            }}>
              {alunosSelecionados || "Nenhum nome cadastrado."}
            </div>

            <button
              className="btn primary"
              style={{ marginTop: '20px', width: '100%', padding: '12px' }}
              onClick={() => setModalAberto(false)}
            >
              Entendido
            </button>
          </div>
        </div>
      )}
    </section>
  );
};

export default ListaSolicitacoes;