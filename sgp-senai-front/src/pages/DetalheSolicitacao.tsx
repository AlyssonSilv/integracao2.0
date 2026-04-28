import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';

interface Solicitacao {
  id: number;
  protocolo: string;
  dataSugerida: string;
  treinamento: string;
  treinamentoOutros?: string;
  status: string;
  listaParticipantes: string;
  quantidadeParticipantes: number;
  nomeEmpresa?: string;
  empresa?: { razaoSocial: string };
  instrutor?: string;
  sala?: string;
  horario?: string;
}

const DetalheSolicitacao: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [solicitacao, setSolicitacao] = useState<Solicitacao | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);

  const [dadosAdmin, setDadosAdmin] = useState({
    instrutor: '',
    sala: '',
    horario: '',
    dataSugerida: '',
    listaParticipantes: '',
    quantidadeParticipantes: 0 // Adicionado para a contagem automática
  });

  const isAdmin = window.location.pathname.includes('/admin');

  const carregarSolicitacao = async () => {
    try {
      const response = await api.get(`/solicitacoes/${id}`);
      const data = response.data;
      setSolicitacao(data);

      setDadosAdmin({
        instrutor: data.instrutor || '',
        sala: data.sala || '',
        horario: data.horario || '',
        dataSugerida: data.dataSugerida || '',
        listaParticipantes: data.listaParticipantes || '',
        quantidadeParticipantes: data.quantidadeParticipantes || 0
      });

      if (isAdmin && data.status !== 'CONFIRMADO') {
        setIsEditing(true);
      }
    } catch (error) {
      console.error("Erro ao carregar detalhes:", error);
      toast.error("Erro ao carregar os detalhes da solicitação.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    carregarSolicitacao();
  }, [id]);

  // Hook mágico que conta as linhas sempre que o administrador edita o texto!
  useEffect(() => {
    const texto = dadosAdmin.listaParticipantes;
    let qtd = 0;

    if (texto && texto.trim() !== '') {
      // Separa por quebras de linha e retira linhas em branco/só com espaços
      const linhas = texto.split('\n').filter(linha => linha.trim() !== '');
      qtd = linhas.length;
    }

    // Atualiza a quantidade no estado APENAS se estiver em modo de edição e o valor for diferente
    // (Isso previne loops infinitos no React)
    if (isEditing && dadosAdmin.quantidadeParticipantes !== qtd) {
      setDadosAdmin(prev => ({
        ...prev,
        quantidadeParticipantes: qtd
      }));
    }
  }, [dadosAdmin.listaParticipantes, isEditing]);

  const handleSalvar = async () => {
    if (!dadosAdmin.instrutor || !dadosAdmin.sala || !dadosAdmin.horario || !dadosAdmin.dataSugerida) {
      toast.error("Por favor, preencha todos os campos (Data, Instrutor, Sala e Horário).");
      return;
    }

    try {
      await api.put(`/solicitacoes/${id}/editar`, {
        status: "CONFIRMADO",
        instrutor: dadosAdmin.instrutor,
        sala: dadosAdmin.sala,
        horario: dadosAdmin.horario,
        dataSugerida: dadosAdmin.dataSugerida,
        listaParticipantes: dadosAdmin.listaParticipantes,
        quantidadeParticipantes: dadosAdmin.quantidadeParticipantes // Envia a quantidade atualizada
      });
      toast.success("Agendamento confirmado com sucesso!");
      setIsEditing(false);
      carregarSolicitacao();
    } catch (error: any) {
      toast.error(error.response?.data || "Erro ao guardar agendamento.");
    }
  };

  if (loading) {
    return (
      <section className="card pad" style={{ maxWidth: '800px', textAlign: 'center', margin: '0 auto' }}>
        <div className="p">A carregar detalhes...</div>
      </section>
    );
  }

  if (!solicitacao) {
    return (
      <section className="card pad" style={{ maxWidth: '800px', textAlign: 'center', margin: '0 auto' }}>
        <div className="h1">Solicitação não encontrada</div>
        <button onClick={() => navigate(-1)} className="btn secondary" style={{ marginTop: '20px' }}>Voltar</button>
      </section>
    );
  }

  const nomeDaEmpresa = solicitacao.nomeEmpresa || solicitacao.empresa?.razaoSocial || 'Empresa não informada';

  // A quantidade exibida lá em cima vai depender se estamos editando ou apenas visualizando
  const quantidadeExibida = isEditing ? dadosAdmin.quantidadeParticipantes : solicitacao.quantidadeParticipantes;

  return (
    <section className="card pad" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div className="h1">Detalhes da Solicitação</div>
          <div className="p">Visualizando os dados do protocolo: <strong style={{ fontFamily: 'monospace' }}>{solicitacao.protocolo}</strong></div>
        </div>
        <span className="badge" style={{ backgroundColor: solicitacao.status === 'CONFIRMADO' ? '#dcfce7' : '#fef9c3', color: solicitacao.status === 'CONFIRMADO' ? '#166534' : '#854d0e', padding: '8px 16px', fontSize: '14px' }}>
          {solicitacao.status}
        </span>
      </div>

      <div style={{ marginTop: '24px', padding: '20px', border: '1px solid var(--line)', borderRadius: '12px', backgroundColor: '#fafafa' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Empresa</p>
            <p style={{ fontWeight: '600', marginTop: '4px' }}>{nomeDaEmpresa}</p>
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Treinamento</p>
            <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.treinamento === 'Outros' ? solicitacao.treinamentoOutros : solicitacao.treinamento}</p>
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Data Solicitada Pelo Cliente</p>
            <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.dataSugerida}</p>
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Qtd. Participantes</p>
            <p style={{ fontWeight: '600', marginTop: '4px' }}>
              {/* Mostra a quantidade calculada em tempo real se estiver editando */}
              <span style={{ color: isEditing ? '#004a8e' : 'inherit' }}>{quantidadeExibida} alunos</span>
            </p>
          </div>
        </div>
      </div>

      {/* ÁREA DO ADMIN / DADOS DO SENAI */}
      <div style={{ marginTop: '24px', padding: '20px', border: '1px solid var(--line)', borderRadius: '12px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid var(--line)', paddingBottom: '12px' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#004a8e' }}>Programação SENAI</h3>

          {isAdmin && solicitacao.status === 'CONFIRMADO' && !isEditing && (
            <button onClick={() => setIsEditing(true)} className="btn ghost" style={{ fontSize: '14px', padding: '6px 12px' }}>
              ✏️ Editar Dados
            </button>
          )}
        </div>

        {!isEditing ? (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Data Confirmada</p>
              <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.dataSugerida || 'A definir'}</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Horário</p>
              <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.horario || 'A definir'}</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Instrutor</p>
              <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.instrutor || 'A definir'}</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: 'var(--muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>Sala</p>
              <p style={{ fontWeight: '600', marginTop: '4px' }}>{solicitacao.sala || 'A definir'}</p>
            </div>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <label htmlFor="dataSugerida" className="form-label">Data do Treinamento</label>
              <input
                id="dataSugerida"
                type="date"
                className="form-input"
                title="Selecione a data sugerida"
                value={dadosAdmin.dataSugerida}
                onChange={e => setDadosAdmin({ ...dadosAdmin, dataSugerida: e.target.value })}
              />
            </div>
            <div>
              <label htmlFor="horario" className="form-label">Horário</label>
              <input
                id="horario"
                type="text"
                className="form-input"
                title="Introduza o horário do treinamento"
                placeholder="Ex: 08:00 às 17:00"
                value={dadosAdmin.horario}
                onChange={e => setDadosAdmin({ ...dadosAdmin, horario: e.target.value })}
              />
            </div>
            <div>
              <label htmlFor="instrutor" className="form-label">Instrutor Responsável</label>
              <input
                id="instrutor"
                type="text"
                className="form-input"
                title="Introduza o nome do instrutor responsável"
                placeholder="Ex: Erick Leonardo"
                value={dadosAdmin.instrutor}
                onChange={e => setDadosAdmin({ ...dadosAdmin, instrutor: e.target.value })}
              />
            </div>
            <div>
              <label htmlFor="sala" className="form-label">Sala / Local</label>
              <input
                id="sala"
                type="text"
                className="form-input"
                title="Introduza a sala ou o local"
                placeholder="Ex: Auditório"
                value={dadosAdmin.sala}
                onChange={e => setDadosAdmin({ ...dadosAdmin, sala: e.target.value })}
              />
            </div>
          </div>
        )}
      </div>

      {/* ÁREA DA LISTA DE PARTICIPANTES EDITÁVEL */}
      <div style={{ marginTop: '24px' }}>
        <strong style={{ display: 'block', marginBottom: '8px' }}>
          Lista de Alunos Inscritos: {isEditing && <span style={{ color: '#6b7280', fontSize: '14px', fontWeight: 'normal' }}>({dadosAdmin.quantidadeParticipantes} alunos detetados)</span>}
        </strong>

        {!isEditing ? (
          <div style={{ padding: '16px', border: '1px solid var(--line)', borderRadius: '12px', backgroundColor: '#fafafa', whiteSpace: 'pre-wrap', maxHeight: '200px', overflowY: 'auto' }}>
            {solicitacao.listaParticipantes || "A lista de participantes não foi enviada pela empresa."}
          </div>
        ) : (
          <div>
            <label htmlFor="listaParticipantes" className="form-label">Editar Lista de Participantes (Um nome por linha)</label>
            <textarea
              id="listaParticipantes"
              className="form-input"
              style={{ minHeight: '150px', resize: 'vertical' }}
              title="Edite a lista de participantes"
              placeholder="Digite os nomes dos participantes, um por linha..."
              value={dadosAdmin.listaParticipantes}
              onChange={(e) => setDadosAdmin({ ...dadosAdmin, listaParticipantes: e.target.value })}
            />
          </div>
        )}
      </div>

      {/* BOTÕES DE AÇÃO */}
      <div className="actions" style={{ marginTop: '32px', display: 'flex', gap: '12px', justifyContent: 'flex-end', borderTop: '1px solid var(--line)', paddingTop: '20px' }}>
        {isEditing ? (
          <>
            <button className="btn secondary" onClick={() => setIsEditing(false)}>Cancelar</button>
            <button className="btn primary" onClick={handleSalvar} style={{ backgroundColor: '#16a34a', borderColor: '#16a34a', color: 'white' }}>Confirmar Agendamento</button>
          </>
        ) : (
          <button className="btn secondary" onClick={() => navigate(-1)}>Voltar para a Lista</button>
        )}
      </div>
    </section>
  );
};

export default DetalheSolicitacao;