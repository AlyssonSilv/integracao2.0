import React, { useEffect, useState } from 'react';
import api from '../services/api';

const Industrias: React.FC = () => {
    const [empresas, setEmpresas] = useState<any[]>([]);
    const [filtro, setFiltro] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.get('/empresas')
            .then(res => {
                const apenasIndustrias = res.data.filter((e: any) => e.role !== 'ADMIN');
                setEmpresas(apenasIndustrias);
                setLoading(false);
            })
            .catch(err => console.error("Erro ao carregar indústrias", err));
    }, []);

    const empresasFiltradas = empresas.filter(emp =>
        emp.razaoSocial.toLowerCase().includes(filtro.toLowerCase()) ||
        emp.cnpj.includes(filtro)
    );

    // Função que renderiza o Avatar ampliado (64px)
    const renderAvatar = (emp: any) => {
        const email = emp.email || '';
        const dominio = email.includes('@') ? email.split('@')[1] : '';
        const dominiosGenericos = ['gmail.com', 'hotmail.com', 'outlook.com', 'yahoo.com', 'live.com', 'teste.com'];
        const iniciais = emp.razaoSocial ? emp.razaoSocial.substring(0, 2).toUpperCase() : '??';

        const urlLogo = !dominiosGenericos.includes(dominio) && dominio
            ? `https://cdn.brandfetch.io/${dominio}?w=256&h=256`
            : null;

        return (
            <div style={{
                width: '64px', height: '64px',
                background: 'var(--blue)', color: '#fff',
                borderRadius: '12px', display: 'flex',
                alignItems: 'center', justifyContent: 'center',
                fontWeight: 'bold', fontSize: '22px',
                position: 'relative', overflow: 'hidden',
                border: '1px solid var(--line)', flexShrink: 0,
                transform: 'translateZ(0)',
                WebkitBackfaceVisibility: 'hidden'
            }}>
                <span>{iniciais}</span>

                {urlLogo && (
                    <img
                        src={urlLogo}
                        alt="Logo"
                        style={{
                            position: 'absolute',
                            top: 0, left: 0,
                            width: '100%', height: '100%',
                            objectFit: 'contain',
                            backgroundColor: '#fff',
                            padding: '10px',
                            transition: 'opacity 0.4s ease',
                            opacity: 0,

                            // UNIFICADO: Resolve o erro de duplicidade
                            // @ts-ignore
                            imageRendering: '-webkit-optimize-contrast',

                            // Força o uso da GPU para evitar o borrão de interpolação da CPU
                            WebkitTransform: 'translateZ(0)',
                            filter: 'contrast(1.05) brightness(1.03)', // Ajuda a definir traços finos
                        }}
                        onLoad={(e) => {
                            (e.target as HTMLImageElement).style.opacity = '1';
                        }}
                        onError={(e) => {
                            const target = e.target as HTMLImageElement;
                            if (!target.src.includes('img.logo.dev')) {
                                target.src = `https://img.logo.dev/${dominio}?size=128`;
                            } else {
                                target.style.display = 'none';
                            }
                        }}
                    />
                )}
            </div>
        );
    };

    if (loading) return <div className="pad p">Carregando base de parceiros...</div>;

    return (
        <div className="wrap">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '32px' }}>
                <div>
                    <div className="h1">Indústrias Cadastradas</div>
                    <p className="p">Gerencie o portfólio de empresas parceiras e seus contatos.</p>
                </div>
                <div className="card" style={{ padding: '8px 16px', background: 'var(--soft)', border: 'none' }}>
                    <span className="h2" style={{ margin: 0, color: 'var(--blue)' }}>{empresas.length}</span>
                    <small style={{ marginLeft: '8px', color: 'var(--muted)' }}>Parceiros Totais</small>
                </div>
            </div>

            <div className="card pad" style={{ marginBottom: '24px' }}>
                <input
                    type="text"
                    className="input"
                    placeholder="🔍 Buscar por Razão Social ou CNPJ..."
                    value={filtro}
                    onChange={(e) => setFiltro(e.target.value)}
                    style={{ maxWidth: '400px' }}
                />
            </div>

            <div className="grid">
                {empresasFiltradas.length > 0 ? (
                    empresasFiltradas.map(emp => (
                        <div key={emp.id} className="card" style={{ gridColumn: 'span 4', display: 'flex', flexDirection: 'column' }}>
                            {/* Header do Card Refatorado para Logo Maior */}
                            <div style={{
                                padding: '24px 20px', // Mais espaço interno
                                background: 'var(--soft)',
                                borderBottom: '1px solid var(--line)',
                                display: 'flex',
                                alignItems: 'center', // Alinhamento centralizado com a logo
                                gap: '16px' // Mais espaço entre logo e texto
                            }}>
                                {renderAvatar(emp)}

                                <div style={{ overflow: 'hidden' }}>
                                    <div className="h2" style={{
                                        margin: 0,
                                        fontSize: '18px', // Fonte maior para acompanhar a logo
                                        lineHeight: '1.2',
                                        color: 'var(--text-color)'
                                    }}>
                                        {emp.razaoSocial}
                                    </div>
                                    <small style={{ color: 'var(--muted)', display: 'block', marginTop: '4px' }}>
                                        CNPJ: {emp.cnpj}
                                    </small>
                                </div>
                            </div>

                            <div className="pad" style={{ flex: 1 }}>
                                <div style={{ marginBottom: '16px' }}>
                                    <small className="label" style={{ marginBottom: '4px', display: 'block' }}>Responsável</small>
                                    <div className="p" style={{ margin: 0, fontWeight: '500' }}>{emp.nomeResponsavel}</div>
                                </div>

                                <div className="grid" style={{ gap: '10px' }}>
                                    <div style={{ gridColumn: 'span 12' }}>
                                        <a href={`mailto:${emp.email}`} className="btn secondary" style={{ width: '100%', justifyContent: 'center', fontSize: '13px' }}>
                                            ✉️ {emp.email}
                                        </a>
                                    </div>
                                    <div style={{ gridColumn: 'span 12' }}>
                                        <a href={`tel:${emp.telefone}`} className="btn ghost" style={{ width: '100%', justifyContent: 'center', fontSize: '13px', border: '1px solid var(--line)' }}>
                                            📞 {emp.telefone}
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))
                ) : (
                    <div style={{ gridColumn: 'span 12', textAlign: 'center', padding: '60px' }}>
                        <div className="p" style={{ color: 'var(--muted)' }}>Nenhuma indústria encontrada para "{filtro}"</div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Industrias;