import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import GenerateCodePage from '../pages/GenerateCodePage';
import * as inviteCodeApi from '../api/inviteCodeApi';

// 模拟路由
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

// 模拟API请求
jest.mock('../api/inviteCodeApi');

describe('GenerateCodePage', () => {
  beforeEach(() => {
    // 模拟localStorage
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => 'mock-token'),
        setItem: jest.fn(),
        removeItem: jest.fn()
      },
      writable: true
    });
  });
  
  test('渲染生成邀请码页面', () => {
    render(
      <MemoryRouter>
        <GenerateCodePage />
      </MemoryRouter>
    );
    
    expect(screen.getByText('生成邀请码')).toBeInTheDocument();
    expect(screen.getByLabelText('生成数量')).toBeInTheDocument();
    expect(screen.getByLabelText('批次描述')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '生成邀请码' })).toBeInTheDocument();
  });
  
  test('成功生成邀请码', async () => {
    // 模拟API返回数据
    const mockResponseData = {
      data: {
        success: true,
        message: '成功生成3个邀请码',
        data: [
          { id: 1, code: 'ABC123', active: true },
          { id: 2, code: 'DEF456', active: true },
          { id: 3, code: 'GHI789', active: true }
        ]
      }
    };
    
    inviteCodeApi.generateInviteCodes.mockResolvedValue(mockResponseData);
    
    render(
      <MemoryRouter>
        <GenerateCodePage />
      </MemoryRouter>
    );
    
    // 输入数据
    const countInput = screen.getByLabelText('生成数量');
    fireEvent.change(countInput, { target: { value: 3 } });
    
    const descInput = screen.getByLabelText('批次描述');
    fireEvent.change(descInput, { target: { value: '测试批次' } });
    
    // 点击生成按钮
    const generateButton = screen.getByRole('button', { name: '生成邀请码' });
    fireEvent.click(generateButton);
    
    // 等待API响应
    await waitFor(() => {
      expect(inviteCodeApi.generateInviteCodes).toHaveBeenCalledWith(3, '测试批次');
      expect(screen.getByText('ABC123')).toBeInTheDocument();
      expect(screen.getByText('DEF456')).toBeInTheDocument();
      expect(screen.getByText('GHI789')).toBeInTheDocument();
    });
  });
  
  test('生成邀请码失败', async () => {
    // 模拟API失败
    inviteCodeApi.generateInviteCodes.mockRejectedValue({
      response: {
        data: {
          success: false,
          message: '生成邀请码失败'
        }
      }
    });
    
    render(
      <MemoryRouter>
        <GenerateCodePage />
      </MemoryRouter>
    );
    
    // 输入数据
    const countInput = screen.getByLabelText('生成数量');
    fireEvent.change(countInput, { target: { value: 3 } });
    
    // 点击生成按钮
    const generateButton = screen.getByRole('button', { name: '生成邀请码' });
    fireEvent.click(generateButton);
    
    // 等待API响应
    await waitFor(() => {
      expect(inviteCodeApi.generateInviteCodes).toHaveBeenCalledWith(3, '');
    });
  });
}); 