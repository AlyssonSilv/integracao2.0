import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';

const NovaSolicitacao: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    treinamento: 'Operação de Empilhadeira',
    quantidade: 5,
    listaParticipantes: '',
    dataSugerida: '',
    treinamentoOutros: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  // Trava Visual (Baseada no navegador - Ajuda na UX, mas a verdadeira segurança está no Java)
  const horaAtual = new Date().getHours();
  const minutoAtual = new Date().getMinutes();
  const passouDoHorarioVisual = horaAtual > 16 || (horaAtual === 16 && minutoAtual >= 30);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (isSubmitting) return;

    if (!formData.listaParticipantes.trim()) {
      toast.error('Insira os nomes dos colaboradores.');
      return;
    }

    const nomesListados = formData.listaParticipantes
      .split('\n')
      .filter(nome => nome.trim() !== '')
      .length;

    if (nomesListados !== formData.quantidade) {
      toast.error(
        `Divergência detectada:\nVocê informou ${formData.quantidade} participante(s), mas listou ${nomesListados} nome(s). Por favor, corrija a lista ou a quantidade.`
      );
      return;
    }

    setIsSubmitting(true);

    try {
      const empresaStorage = localStorage.getItem('empresa_logada');
      if (!empresaStorage) {
        toast.error("Sessão expirada. Por favor, faça login novamente.");
        navigate('/login');
        return;
      }

      const empresa = JSON.parse(empresaStorage);

      const payload = {
        treinamento: formData.treinamento,
        treinamentoOutros: formData.treinamentoOutros,
        quantidadeParticipantes: formData.quantidade,
        listaParticipantes: formData.listaParticipantes,
        dataSugerida: formData.dataSugerida,
        status: "Nova",
        empresa: { id: empresa.id }
      };

      await api.post('/solicitacoes', payload);

      toast.success('Solicitação enviada com sucesso! O protocolo foi gerado pelo sistema.');
      navigate('/lista');

    } catch (error: any) {
      setIsSubmitting(false);

      // Se o Java bloquear por causa do horário, ele manda o status 403
      if (error.response && error.response.status === 403) {
        toast.error(error.response.data, { duration: 6000 }); // Exibe a mensagem que veio do Java
      } else {
        console.error("Erro ao enviar solicitação:", error);
        toast.error('Erro ao processar solicitação. Verifique se o servidor Java está ativo.');
      }
    }
  };

  // Se o relógio local do usuário disser que passou das 16:30, o formulário é desabilitado
  const formDesabilitado = isSubmitting || passouDoHorarioVisual;

  return (
    <section className="card pad" style={{ maxWidth: '980px' }}>
      <div className="h1">Nova Solicitação</div>

      {/* Aviso amigável caso tenha passado do horário */}
      {passouDoHorarioVisual && (
        <div style={{ backgroundColor: '#fee2e2', color: '#991b1b', padding: '16px', borderRadius: '8px', marginTop: '16px', fontWeight: 'bold' }}>
          ⚠️ Atenção: A agenda de hoje foi encerrada (limite às 16:30). O sistema não aceita novas solicitações neste horário. Por favor, retorne amanhã.
        </div>
      )}

      <form onSubmit={handleSubmit} className="grid" style={{ gridTemplateColumns: '1fr 1fr', gap: '14px', marginTop: '20px', opacity: passouDoHorarioVisual ? 0.6 : 1 }}>

        <div>
          <label htmlFor="treinamento" className="label">Treinamento</label>
          <select
            id="treinamento"
            className="select"
            title="Selecione o treinamento desejado"
            required
            value={formData.treinamento}
            onChange={e => setFormData({ ...formData, treinamento: e.target.value })}
            disabled={formDesabilitado}
          >
            <option value="Operação de Empilhadeira">Operação de Empilhadeira</option>
            <option value="NR 20">NR 20</option>
            <option value="Prevenção de Quedas/NR35">Prevenção de Quedas/NR35</option>
            <option value="Outros (especificar)">Outros (especificar)</option>
          </select>
        </div>

        <div>
          <label htmlFor="quantidade" className="label">Quantidade de Participantes</label>
          <input
            id="quantidade"
            className="input"
            type="number"
            min="1"
            title="Informe a quantidade de alunos"
            required
            value={formData.quantidade}
            onChange={e => setFormData({ ...formData, quantidade: parseInt(e.target.value) })}
            disabled={formDesabilitado}
          />
        </div>

        <div style={{ gridColumn: 'span 2' }}>
          <label htmlFor="listaParticipantes" className="label">Lista de Participantes (nome por linha)</label>
          <textarea
            id="listaParticipantes"
            className="textarea"
            placeholder="Nome 1&#10;Nome 2&#10;Nome 3"
            title="Digite um nome por linha"
            required
            value={formData.listaParticipantes}
            onChange={e => setFormData({ ...formData, listaParticipantes: e.target.value })}
            disabled={formDesabilitado}
          ></textarea>
        </div>

        <div>
          <label htmlFor="dataSugerida" className="label">Data sugerida</label>
          <input
            id="dataSugerida"
            className="input"
            type="date"
            title="Selecione uma data sugerida"
            required
            value={formData.dataSugerida}
            onChange={e => setFormData({ ...formData, dataSugerida: e.target.value })}
            disabled={formDesabilitado}
          />
        </div>

        {formData.treinamento === 'Outros (especificar)' && (
          <div>
            <label htmlFor="treinamentoOutros" className="label">Outros (especificar)</label>
            <input
              id="treinamentoOutros"
              className="input"
              placeholder="Descreva o treinamento"
              title="Especifique o treinamento"
              required
              value={formData.treinamentoOutros}
              onChange={e => setFormData({ ...formData, treinamentoOutros: e.target.value })}
              disabled={formDesabilitado}
            />
          </div>
        )}

        <div style={{ gridColumn: 'span 2', marginTop: '10px' }} className="actions">
          {!isSubmitting && (
            <Link to="/dashboard" className="btn secondary">Cancelar</Link>
          )}

          <button
            className="btn primary"
            type="submit"
            disabled={formDesabilitado}
            style={{ opacity: formDesabilitado ? 0.7 : 1, cursor: formDesabilitado ? 'not-allowed' : 'pointer' }}
          >
            {isSubmitting ? 'Enviando...' : (passouDoHorarioVisual ? 'Agenda Encerrada' : 'Enviar Solicitação')}
          </button>
        </div>
      </form>
    </section>
  );
};

export default NovaSolicitacao;