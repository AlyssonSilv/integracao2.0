import React, { useState, useEffect } from 'react';
import api from '../services/api';

const Usuarios: React.FC = () => {
  const [formData, setFormData] = useState({ nome: '', email: '', cargo: '', senha: '' });
  const [usuarios, setUsuarios] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const carregarUsuarios = async () => {
    try {
      const dados = localStorage.getItem('empresa_logada');
      if (!dados) return;
      const empresa = JSON.parse(dados);
      
      const response = await api.get(`/usuarios/empresa/${empresa.id}`);
      setUsuarios(response.data);
    } catch (error) {
      console.error("Erro ao carregar colaboradores:", error);
    }
  };

  useEffect(() => { carregarUsuarios(); }, []);

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/usuarios', formData);
      alert('Colaborador adicionado com sucesso!');
      setFormData({ nome: '', email: '', cargo: '', senha: '' });
      carregarUsuarios();
    } catch (error: any) {
      alert(error.response?.data || "Erro ao cadastrar usuário.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card pad" style={{ maxWidth: '980px' }}>
      <div className="h1">Usuários da Empresa</div>
      <div className="p">Cadastre os colaboradores que poderão solicitar treinamentos.</div>
      
      <form onSubmit={handleAddUser} className="grid" style={{ gridTemplateColumns: '1fr 1fr 1fr 1fr auto', gap: '14px', marginTop: '20px', alignItems: 'end' }}>
        <div>
          <label className="label">Nome</label>
          <input className="input" placeholder="Nome" required value={formData.nome} onChange={e => setFormData({...formData, nome: e.target.value})} />
        </div>
        <div>
          <label className="label">E-mail</label>
          <input className="input" type="email" placeholder="E-mail" required value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
        </div>
        <div>
          <label className="label">Cargo</label>
          <input className="input" placeholder="Cargo" required value={formData.cargo} onChange={e => setFormData({...formData, cargo: e.target.value})} />
        </div>
        <div>
          <label className="label">Senha</label>
          <input className="input" type="password" placeholder="Senha" required value={formData.senha} onChange={e => setFormData({...formData, senha: e.target.value})} />
        </div>
        <button type="submit" className="btn primary" disabled={loading} style={{ height: '45px' }}>
            {loading ? '...' : 'Adicionar'}
        </button>
      </form>

      <div className="h2" style={{ marginTop: '30px' }}>Usuários Cadastrados</div>
      <div style={{ overflowX: 'auto', marginTop: '10px' }}>
        <table>
          <thead>
            <tr><th>Nome</th><th>E-mail</th><th>Cargo</th></tr>
          </thead>
          <tbody>
            {usuarios.map((user) => (
              <tr key={user.id}>
                <td><strong>{user.nome}</strong></td>
                <td>{user.email}</td>
                <td>{user.cargo}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
};

export default Usuarios;