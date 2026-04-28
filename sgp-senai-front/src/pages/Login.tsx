import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';

const Login: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [nomeResponsavel, setNomeResponsavel] = useState(''); 
  const [erro, setErro] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setLoading(true);

    const cnpjLimpo = cnpj.replace(/\D/g, '');

    try {
      const response = await api.post('/login', {
        cnpj: cnpjLimpo,
        nomeResponsavel: nomeResponsavel
      });

      const { token, refreshToken, id, razaoSocial, email, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', refreshToken);

      localStorage.setItem('empresa_logada', JSON.stringify({
        id,
        razaoSocial, 
        cnpj: cnpjLimpo,
        email,
        role
      }));

      // Redirecionamento baseado no papel do usuário
      if (role === 'ADMIN') {
        navigate('/admin/analitico');
      } else {
        navigate('/dashboard');
      }

    } catch (error: any) {
      console.error("Erro no login:", error);
      if (error.response && error.response.status === 401) {
        setErro("CNPJ ou Nome do Responsável incorretos. Verifique os dados do cadastro.");
      } else {
        setErro("Não foi possível conectar ao servidor. Tente novamente mais tarde.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      backgroundColor: 'var(--bg-color)'
    }}>
      <div className="card pad" style={{ width: '100%', maxWidth: '400px', backgroundColor: '#fff' }}>
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <h2 style={{ margin: 0, color: '#333' }}>Acesso ao Portal</h2>

          <p style={{ color: '#666', marginTop: '8px' }}>
            SENAI • Solicitação de Treinamentos Industriais
          </p>
        </div>

        {erro && (
          <div style={{
            backgroundColor: 'rgba(255, 68, 68, 0.1)',
            color: '#ff4444',
            padding: '10px',
            borderRadius: '6px',
            marginBottom: '16px',
            border: '1px solid rgba(255, 68, 68, 0.3)',
            fontSize: '14px'
          }}>
            {erro}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px' }}>
              CNPJ da Empresa
            </label>
            <input
              type="text"
              className="input"
              value={cnpj}
              onChange={(e) => setCnpj(e.target.value)}
              placeholder="00.000.000/0000-00"
              required
              style={{ width: '100%', padding: '10px' }}
            />
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px' }}>
              Nome do Responsável
            </label>
            <input
              type="text"
              className="input"
              value={nomeResponsavel}
              onChange={(e) => setNomeResponsavel(e.target.value)}
              placeholder="Digite seu nome completo"
              required
              style={{ width: '100%', padding: '10px' }}
            />
          </div>

          <button
            type="submit"
            className="btn primary"
            style={{ width: '100%', padding: '12px' }}
            disabled={loading}
          >
            {loading ? 'Validando...' : 'Acessar Painel'}
          </button>
        </form>

        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '14px' }}>
          <Link to="/cadastro" style={{ color: '#007bff', textDecoration: 'none' }}>
            Não possui cadastro? Cadastre sua empresa
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;