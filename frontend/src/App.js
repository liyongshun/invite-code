import React from 'react';
import { Routes, Route } from 'react-router-dom';
import VerifyCodePage from './pages/VerifyCodePage';
import AdminLoginPage from './pages/AdminLoginPage';
import AdminDashboard from './pages/AdminDashboard';
import GenerateCodePage from './pages/GenerateCodePage';
import CodeDetailPage from './pages/CodeDetailPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<VerifyCodePage />} />
      <Route path="/admin/login" element={<AdminLoginPage />} />
      <Route path="/admin" element={<AdminDashboard />} />
      <Route path="/admin/generate" element={<GenerateCodePage />} />
      <Route path="/admin/codes/:id" element={<CodeDetailPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default App; 