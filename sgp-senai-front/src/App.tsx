import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from "react-hot-toast";

import Sidebar from './components/Sidebar';
import Analitico from './pages/Analitico';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import Dashboard from './pages/Dashboard';
import NovaSolicitacao from './pages/NovaSolicitacao';
import ListaSolicitacoes from './pages/ListaSolicitacoes';
import DetalheSolicitacao from './pages/DetalheSolicitacao';
import ProtectedRoute from './components/ProtectedRoute';
import GestaoDemandas from './pages/GestaoDemandas';
import Industrias from './pages/Industrias';
import './index.css';

function App() {
  const [isDarkMode, setIsDarkMode] = useState(false);

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }, [isDarkMode]);

  return (
    <BrowserRouter>
      {/* O Toaster precisa estar dentro do render principal */}
      <Toaster position="top-right" />
      
      <div className="app">
        <Sidebar />

        <main className="main">
          {/* Barra Superior */}
          <div className="top">
            <div className="bar">
              <div className="title">SENAI • Solicitação de Treinamentos</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <button
                  onClick={() => setIsDarkMode(!isDarkMode)}
                  className="btn ghost"
                  style={{ color: '#fff', border: '1px solid rgba(255,255,255,0.3)', padding: '6px 12px' }}
                >
                  {isDarkMode ? '☀️ Claro' : '🌙 Escuro'}
                </button>
                <div className="user">Painel de Controle</div>
              </div>
            </div>
            <div className="accent"></div>
          </div>

          <div className="wrap">
            <Routes>
              {/* --- ROTAS PÚBLICAS --- */}
              <Route path="/login" element={<Login />} />
              <Route path="/cadastro" element={<Cadastro />} />
              <Route path="/" element={<Navigate to="/login" />} />

              {/* --- ROTAS DE EMPRESA (USER) --- */}
              <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
              <Route path="/nova" element={<ProtectedRoute><NovaSolicitacao /></ProtectedRoute>} />
              <Route path="/lista" element={<ProtectedRoute><ListaSolicitacoes /></ProtectedRoute>} />
              <Route path="/detalhe/:id" element={<ProtectedRoute><DetalheSolicitacao /></ProtectedRoute>} />

              {/* --- ROTAS DE ADMINISTRADOR (SENAI) --- */}
              <Route path="/admin/analitico" element={<ProtectedRoute><Analitico /></ProtectedRoute>} />
              <Route path="/admin/lista" element={<ProtectedRoute><GestaoDemandas /></ProtectedRoute>} />
              <Route path="/admin/industrias" element={<ProtectedRoute><Industrias /></ProtectedRoute>} />
              {/* Nova Rota Adicionada para o clique do "Ver Detalhes" no Analítico */}
              <Route path="/admin/solicitacao/:id" element={<ProtectedRoute><DetalheSolicitacao /></ProtectedRoute>} />
            </Routes>
          </div>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App; 