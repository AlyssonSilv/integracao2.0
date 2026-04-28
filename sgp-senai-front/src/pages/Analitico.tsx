import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Button from '../components/Button';
import { useNavigate } from 'react-router-dom';

interface Solicitacao {
    id: number;
    protocolo: string;
    dataSugerida: string;
    treinamento: string;
    status: string;
    listaParticipantes: string;
    nomeEmpresa: string;
    instrutor?: string;
    sala?: string;
    horario?: string;
}

const Analitico: React.FC = () => {
    const [solicitacoes, setSolicitacoes] = useState<Solicitacao[]>([]);
    const navigate = useNavigate(); // Hook para navegação

    const carregarSolicitacoes = async () => {
        try {
            const response = await api.get('/solicitacoes/admin/todas');
            setSolicitacoes(response.data);
        } catch (error) {
            console.error("Erro ao carregar solicitações:", error);
        }
    };

    useEffect(() => {
        carregarSolicitacoes();
    }, []);

    const handleGerarPdf = async () => {
        try {
            const response = await api.get('/admin/analitico/agenda/pdf', { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'agenda_atendimentos.pdf');
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            alert("Erro ao gerar o ficheiro PDF.");
        }
    };

    // A função de navegação substitui a abertura do modal
    const irParaDetalhes = (id: number) => {
        navigate(`/admin/solicitacao/${id}`);
    };

    return (
        <div className="p-6 w-full relative">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
                <h1 className="text-3xl font-extrabold text-gray-800 tracking-tight">Painel Analítico</h1>
                <Button variant="primary" onClick={handleGerarPdf} className="flex items-center gap-2">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                    Gerar Agenda (PDF)
                </Button>
            </div>

            <div className="overflow-x-auto bg-white rounded-xl shadow-md border border-gray-100">
                <table className="min-w-full text-left border-collapse whitespace-nowrap">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            <th className="py-4 px-5 text-sm font-semibold text-gray-600 uppercase">Protocolo</th>
                            <th className="py-4 px-5 text-sm font-semibold text-gray-600 uppercase">Empresa</th>
                            <th className="py-4 px-5 text-sm font-semibold text-gray-600 uppercase">Data Sugerida</th>
                            <th className="py-4 px-5 text-sm font-semibold text-gray-600 uppercase">Status</th>
                            <th className="py-4 px-5 text-sm font-semibold text-gray-600 uppercase text-center">Ações</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {solicitacoes.map(sol => (
                            <tr key={sol.id} className="hover:bg-blue-50/50 transition-colors">
                                <td className="py-3 px-5 text-gray-800 font-medium">{sol.protocolo}</td>
                                <td className="py-3 px-5 text-gray-700">{sol.nomeEmpresa}</td>
                                <td className="py-3 px-5 text-gray-700">{sol.dataSugerida}</td>
                                <td className="py-3 px-5">
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold border ${sol.status === 'CONFIRMADO' ? 'bg-green-100 text-green-700 border-green-200' : 'bg-yellow-100 text-yellow-800 border-yellow-200'}`}>
                                        {sol.status}
                                    </span>
                                </td>
                                <td className="py-3 px-5 text-center">
                                    {/* O clique agora navega para a página de detalhes usando a função irParaDetalhes */}
                                    <Button variant="ghost" onClick={() => irParaDetalhes(sol.id)}>
                                        Ver Detalhes
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {solicitacoes.length === 0 && (
                            <tr>
                                <td colSpan={5} className="py-8 text-center text-gray-500">
                                    Nenhuma solicitação encontrada.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Analitico;