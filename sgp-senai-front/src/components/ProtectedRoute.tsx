import React from 'react';
import { Navigate } from 'react-router-dom';

interface Props {
  children: React.ReactNode; // Alterado de JSX.Element para React.ReactNode
}

const ProtectedRoute = ({ children }: Props) => {
  const empresaLogada = localStorage.getItem('empresa_logada');

  if (!empresaLogada) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>; // Usamos o Fragment <> para garantir a renderização
};

export default ProtectedRoute;