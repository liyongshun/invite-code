import axios from 'axios';

const API_URL = '/api';

// 创建axios实例
const api = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// 请求拦截器 - 添加认证头
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 验证邀请码
export const verifyInviteCode = (code, userId = '') => {
  return api.post('/invite-codes/verify', { code, userId });
};

// 生成邀请码
export const generateInviteCodes = (count, description = '') => {
  return api.post('/invite-codes/generate', { count, description });
};

// 获取所有邀请码（分页）
export const getAllInviteCodes = (page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') => {
  return api.get(`/invite-codes?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`);
};

// 获取邀请码详情
export const getInviteCode = (id) => {
  return api.get(`/invite-codes/${id}`);
};

// 获取邀请码使用记录
export const getInviteCodeUsageRecords = (id, page = 0, size = 10) => {
  return api.get(`/invite-codes/${id}/usage-records?page=${page}&size=${size}`);
};

// 禁用邀请码
export const disableInviteCode = (id) => {
  return api.put(`/invite-codes/${id}/disable`);
};

// 启用邀请码
export const enableInviteCode = (id) => {
  return api.put(`/invite-codes/${id}/enable`);
};

// 管理员登录
export const adminLogin = (username, password) => {
  // 这里简化处理，实际应该调用后端登录接口
  return new Promise((resolve, reject) => {
    if (username === 'admin' && password === 'admin123') {
      const mockToken = 'mock-jwt-token';
      localStorage.setItem('token', mockToken);
      resolve({ data: { success: true, data: { token: mockToken } } });
    } else {
      reject({ response: { data: { success: false, message: '用户名或密码错误' } } });
    }
  });
}; 