import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Typography, Alert } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { adminLogin } from '../api/inviteCodeApi';

const { Title } = Typography;

const AdminLoginPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const onFinish = async (values) => {
    setLoading(true);
    setError('');
    
    try {
      const response = await adminLogin(values.username, values.password);
      console.log('登录响应:', response);
      
      if (response.data && response.data.success) {
        // 保存token到localStorage
        localStorage.setItem('token', response.data.data.token);
        navigate('/admin');
      } else {
        setError(response.data?.message || '登录失败，请检查用户名和密码');
      }
    } catch (err) {
      console.error('登录错误:', err);
      setError(err.response?.data?.message || '登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="verify-code-container">
      <Title level={2} className="text-center">管理员登录</Title>
      
      {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 24 }} />}
      
      <Form
        name="login"
        initialValues={{ remember: true }}
        onFinish={onFinish}
        size="large"
      >
        <Form.Item
          name="username"
          rules={[{ required: true, message: '请输入用户名' }]}
        >
          <Input 
            prefix={<UserOutlined />} 
            placeholder="用户名" 
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[{ required: true, message: '请输入密码' }]}
        >
          <Input.Password
            prefix={<LockOutlined />}
            placeholder="密码"
          />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>
            登录
          </Button>
        </Form.Item>
      </Form>

      <div className="text-center">
        <Button type="link" href="/">返回首页</Button>
      </div>
    </div>
  );
};

export default AdminLoginPage;
