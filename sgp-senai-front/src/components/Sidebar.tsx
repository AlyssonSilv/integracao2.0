import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';

const Sidebar: React.FC = () => {
    const navigate = useNavigate();

    // Recupera os dados da empresa/admin logado
    const dados = localStorage.getItem('empresa_logada');
    const empresa = dados ? JSON.parse(dados) : null;
    const isAdmin = empresa?.role === 'ADMIN';

    const handleLogout = (e: React.MouseEvent) => {
        e.preventDefault();
        if (window.confirm("Tem certeza que deseja sair do sistema?")) {
            localStorage.clear();
            navigate('/login');
        }
    };

    return (
        <aside className="rail">
            {/* LOGO E IDENTIFICAÇÃO DO PAPEL */}
            <div className="brand" style={{ padding: '20px 16px', borderBottom: '1px solid var(--line)' }}>
                <img
                    src="https://logodownload.org/wp-content/uploads/2019/08/senai-logo-1.png"
                    alt="SENAI"
                    style={{ height: '30px' }}
                />
                <div style={{ marginTop: '8px' }}>
                    <span className="badge" style={{
                        fontSize: '10px',
                        background: isAdmin ? 'var(--orange)' : 'var(--blue)',
                        color: '#fff',
                        border: 'none'
                    }}>
                        {isAdmin ? 'PAINEL ADMINISTRATIVO' : 'PORTAL DA INDÚSTRIA'}
                    </span>
                </div>
            </div>

            <nav className="menu" style={{ marginTop: '16px' }}>
                <small>Navegação principal</small>

                {/* MENU EXCLUSIVO PARA ADMINISTRADOR (SENAI) */}
                {isAdmin ? (
                    <>
                        <NavLink to="/admin/analitico" className={({ isActive }) => (isActive ? 'active' : '')}>
                            📈 Painel Analítico
                        </NavLink>

                        {/* ALTERADO: de /lista para /admin/lista */}
                        <NavLink to="/admin/lista" className={({ isActive }) => (isActive ? 'active' : '')}>
                            📋 Gestão de Demandas
                        </NavLink>

                        <NavLink to="/admin/industrias" className={({ isActive }) => (isActive ? 'active' : '')}>
                            🏢 Indústrias Cadastradas
                        </NavLink>
                    </>
                ) : (
                    /* MENU EXCLUSIVO PARA EMPRESAS (USER) */
                    <>
                        <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
                            📊 Meu Dashboard
                        </NavLink>
                        <NavLink to="/nova" className={({ isActive }) => (isActive ? 'active' : '')}>
                            📝 Nova Solicitação
                        </NavLink>
                        <NavLink to="/lista" className={({ isActive }) => (isActive ? 'active' : '')}>
                            📁 Minhas Solicitações
                        </NavLink>
                    </>
                )}

                <div style={{ height: '1px', background: 'var(--line)', margin: '20px 12px' }}></div>

                <small>Sistema</small>
                <a href="#logout" onClick={handleLogout} style={{ color: 'var(--orange)' }}>
                    🚪 Encerrar Sessão
                </a>
            </nav>
        </aside>
    );
};

export default Sidebar;