import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Table, Button, Card, Typography, Space, Tag, Spin, Modal, message } from 'antd';
import { PlusOutlined, ExclamationCircleOutlined, PoweroffOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { getAllInviteCodes, disableInviteCode, enableInviteCode } from '../api/inviteCodeApi';

const { Title } = Typography;
const { confirm } = Modal;

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [inviteCodes, setInviteCodes] = useState([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  });

  useEffect(() => {
    // 检查是否已登录
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/admin/login');
      return;
    }
    
    fetchInviteCodes();
  }, [navigate]);

  const fetchInviteCodes = async (page = 0, size = 10) => {
    setLoading(true);
    try {
      const response = await getAllInviteCodes(page, size);
      const data = response.data.data;
      
      setInviteCodes(data.content);
      setPagination({
        current: page + 1,
        pageSize: size,
        total: data.totalElements
      });
    } catch (error) {
      message.error('获取邀请码列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pagination) => {
    fetchInviteCodes(pagination.current - 1, pagination.pageSize);
  };

  const handleDisable = (id) => {
    confirm({
      title: '确认禁用',
      icon: <ExclamationCircleOutlined />,
      content: '禁用后，该邀请码将无法使用。是否继续？',
      onOk: async () => {
        try {
          await disableInviteCode(id);
          message.success('邀请码已禁用');
          fetchInviteCodes(pagination.current - 1, pagination.pageSize);
        } catch (error) {
          message.error('操作失败');
        }
      }
    });
  };

  const handleEnable = (id) => {
    confirm({
      title: '确认启用',
      icon: <ExclamationCircleOutlined />,
      content: '启用后，该邀请码将可以正常使用。是否继续？',
      onOk: async () => {
        try {
          await enableInviteCode(id);
          message.success('邀请码已启用');
          fetchInviteCodes(pagination.current - 1, pagination.pageSize);
        } catch (error) {
          message.error('操作失败');
        }
      }
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/admin/login');
  };

  const columns = [
    {
      title: '邀请码',
      dataIndex: 'code',
      key: 'code',
      render: (text) => <span className="code-value">{text}</span>
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active) => (
        <Tag color={active ? 'green' : 'red'}>
          {active ? '有效' : '已禁用'}
        </Tag>
      )
    },
    {
      title: '使用次数',
      dataIndex: 'usageCount',
      key: 'usageCount'
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => new Date(text).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Link to={`/admin/codes/${record.id}`}>查看详情</Link>
          {record.active ? (
            <Button 
              type="text" 
              danger 
              icon={<PoweroffOutlined />}
              onClick={() => handleDisable(record.id)}
            >
              禁用
            </Button>
          ) : (
            <Button 
              type="text" 
              icon={<CheckCircleOutlined />} 
              style={{ color: 'green' }}
              onClick={() => handleEnable(record.id)}
            >
              启用
            </Button>
          )}
        </Space>
      )
    }
  ];

  return (
    <div className="container">
      <Card className="admin-container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <Title level={2}>邀请码管理</Title>
          <Space>
            <Button 
              type="primary" 
              icon={<PlusOutlined />}
              onClick={() => navigate('/admin/generate')}
            >
              生成邀请码
            </Button>
            <Button onClick={handleLogout}>退出登录</Button>
          </Space>
        </div>

        <Spin spinning={loading}>
          <Table 
            columns={columns} 
            dataSource={inviteCodes} 
            rowKey="id"
            pagination={pagination}
            onChange={handleTableChange}
          />
        </Spin>
      </Card>
    </div>
  );
};

export default AdminDashboard; 