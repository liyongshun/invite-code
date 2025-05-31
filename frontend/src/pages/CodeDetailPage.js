import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Typography, Descriptions, Button, Table, Tag, Spin, Divider, message, Modal } from 'antd';
import { ArrowLeftOutlined, ExclamationCircleOutlined, PoweroffOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { getInviteCode, getInviteCodeUsageRecords, disableInviteCode, enableInviteCode } from '../api/inviteCodeApi';

const { Title } = Typography;
const { confirm } = Modal;

const CodeDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [recordsLoading, setRecordsLoading] = useState(true);
  const [inviteCode, setInviteCode] = useState(null);
  const [usageRecords, setUsageRecords] = useState([]);
  const [recordsPagination, setRecordsPagination] = useState({
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
    
    fetchInviteCodeDetails();
    fetchUsageRecords();
  }, [id, navigate]);

  const fetchInviteCodeDetails = async () => {
    setLoading(true);
    try {
      const response = await getInviteCode(id);
      setInviteCode(response.data.data);
    } catch (error) {
      message.error('获取邀请码详情失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsageRecords = async (page = 0, size = 10) => {
    setRecordsLoading(true);
    try {
      const response = await getInviteCodeUsageRecords(id, page, size);
      const data = response.data.data;
      
      setUsageRecords(data.content);
      setRecordsPagination({
        current: page + 1,
        pageSize: size,
        total: data.totalElements
      });
    } catch (error) {
      message.error('获取使用记录失败');
      console.error(error);
    } finally {
      setRecordsLoading(false);
    }
  };

  const handleRecordsTableChange = (pagination) => {
    fetchUsageRecords(pagination.current - 1, pagination.pageSize);
  };

  const handleDisable = () => {
    confirm({
      title: '确认禁用',
      icon: <ExclamationCircleOutlined />,
      content: '禁用后，该邀请码将无法使用。是否继续？',
      onOk: async () => {
        try {
          await disableInviteCode(id);
          message.success('邀请码已禁用');
          fetchInviteCodeDetails();
        } catch (error) {
          message.error('操作失败');
        }
      }
    });
  };

  const handleEnable = () => {
    confirm({
      title: '确认启用',
      icon: <ExclamationCircleOutlined />,
      content: '启用后，该邀请码将可以正常使用。是否继续？',
      onOk: async () => {
        try {
          await enableInviteCode(id);
          message.success('邀请码已启用');
          fetchInviteCodeDetails();
        } catch (error) {
          message.error('操作失败');
        }
      }
    });
  };

  const recordsColumns = [
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      render: (text) => text || '-'
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress'
    },
    {
      title: '浏览器信息',
      dataIndex: 'userAgent',
      key: 'userAgent',
      ellipsis: true
    },
    {
      title: '使用时间',
      dataIndex: 'usedAt',
      key: 'usedAt',
      render: (text) => new Date(text).toLocaleString()
    }
  ];

  return (
    <div className="container">
      <Card className="admin-container">
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 20 }}>
          <Button 
            icon={<ArrowLeftOutlined />} 
            style={{ marginRight: 16 }}
            onClick={() => navigate('/admin')}
          >
            返回列表
          </Button>
          <Title level={2} style={{ margin: 0 }}>邀请码详情</Title>
        </div>

        <Spin spinning={loading}>
          {inviteCode && (
            <>
              <Descriptions bordered column={2}>
                <Descriptions.Item label="邀请码" span={2}>
                  <span className="code-value">{inviteCode.code}</span>
                  {' '}
                  <Tag color={inviteCode.active ? 'green' : 'red'}>
                    {inviteCode.active ? '有效' : '已禁用'}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="批次ID">
                  {inviteCode.batchId}
                </Descriptions.Item>
                <Descriptions.Item label="使用次数">
                  {inviteCode.usageCount}
                </Descriptions.Item>
                <Descriptions.Item label="创建时间">
                  {new Date(inviteCode.createdAt).toLocaleString()}
                </Descriptions.Item>
                <Descriptions.Item label="创建人">
                  {inviteCode.createdBy}
                </Descriptions.Item>
              </Descriptions>

              <div style={{ marginTop: 20, display: 'flex', justifyContent: 'flex-end' }}>
                {inviteCode.active ? (
                  <Button 
                    danger 
                    icon={<PoweroffOutlined />}
                    onClick={handleDisable}
                  >
                    禁用邀请码
                  </Button>
                ) : (
                  <Button 
                    type="primary" 
                    icon={<CheckCircleOutlined />}
                    onClick={handleEnable}
                  >
                    启用邀请码
                  </Button>
                )}
              </div>
            </>
          )}
        </Spin>

        <Divider orientation="left">使用记录</Divider>
        
        <Spin spinning={recordsLoading}>
          <Table 
            columns={recordsColumns} 
            dataSource={usageRecords} 
            rowKey="id"
            pagination={recordsPagination}
            onChange={handleRecordsTableChange}
          />
        </Spin>
      </Card>
    </div>
  );
};

export default CodeDetailPage; 