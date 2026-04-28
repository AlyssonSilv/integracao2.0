import React, { useEffect, useState } from 'react';
import api from '../services/api';


const GestaoDemandas: React.FC = () => {
    const [solicitacoes, setSolicitacoes] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const carregarTudo = async () => {
        try {
            const res = await api.get('/solicitacoes/admin/todas');
            setSolicitacoes(res.data);
            setLoading(false);
        } catch (err) { console.error(err); }
    };

    const alterarStatus = async (id: number, novoStatus: string) => {
        try {
            await api.put(`/solicitacoes/${id}/status`, { status: novoStatus });
            alert("Status atualizado com sucesso!");
            carregarTudo(); // Recarrega a lista
        } catch (err) { alert("Erro ao atualizar status."); }
    };

    useEffect(() => { carregarTudo(); }, []);

    return (
        <div className="wrap">
            <div className="h1">Gestão de Demandas SENAI</div>
            <p className="p">Administre o fluxo de solicitações de todas as indústrias.</p>

            <div className="card pad" style={{ marginTop: '20px' }}>
                <table>
                    <thead>
                        <tr>
                            <th>Protocolo</th>
                            <th>Indústria</th>
                            <th>Treinamento</th>
                            <th>Data Sugerida</th>
                            <th>Status Atual</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        {solicitacoes.map(sol => (
                            <tr key={sol.id}>
                                <td style={{ fontWeight: 'bold' }}>{sol.protocolo}</td>
                                <td>{sol.nomeEmpresa}</td>
                                <td>{sol.treinamento}</td>
                                <td>{new Date(sol.dataSugerida).toLocaleDateString('pt-BR')}</td>
                                <td>
                                    <span className={`badge badge-${sol.status.toLowerCase()}`}>{sol.status}</span>
                                </td>
                                <td>
                                    <select
                                        className="select select-status"
                                        aria-label={`Alterar status da solicitação ${sol.protocolo}`}
                                        value={sol.status}
                                        onChange={(e) => alterarStatus(sol.id, e.target.value)}
                                    >
                                        <option value="Nova">Nova</option>
                                        <option value="Agendada">Agendada</option>
                                        <option value="Concluída">Concluída</option>
                                        <option value="Cancelada">Cancelada</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default GestaoDemandas;