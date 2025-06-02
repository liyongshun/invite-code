import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, InputNumber, Input, Button, Typography, Card, Space, List, message, Spin } from 'antd';
import { ArrowLeftOutlined, CopyOutlined } from '@ant-design/icons';
import { generateInviteCodes } from '../api/inviteCodeApi';

const { Title, Paragraph } = Typography;

const GenerateCodePage = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [generatedCodes, setGeneratedCodes] = useState([]);

  useEffect(() => {
    // 检查是否已登录
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/admin/login');
    }
  }, [navigate]);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      console.log('发送请求生成邀请码，参数:', values);
      const response = await generateInviteCodes(values.count, values.description);
      console.log('生成邀请码响应:', response);
      
      if (response && response.data) {
        const data = response.data.data;
        console.log('响应数据:', data);
        
        if (Array.isArray(data) && data.length > 0) {
          setGeneratedCodes(data);
          message.success(`成功生成 ${data.length} 个邀请码`);
        } else {
          console.error('响应数据格式不正确:', data);
          message.error('生成邀请码失败: 响应数据格式不正确');
        }
      } else {
        console.error('响应格式不正确:', response);
        message.error('生成邀请码失败: 响应格式不正确');
      }
    } catch (error) {
      console.error('生成邀请码异常:', error);
      if (error.response) {
        console.error('错误响应:', error.response);
        message.error(`生成邀请码失败: ${error.response.data?.message || '服务器错误'}`);
      } else if (error.request) {
        console.error('请求未收到响应:', error.request);
        message.error('生成邀请码失败: 服务器无响应');
      } else {
        console.error('请求配置错误:', error.message);
        message.error(`生成邀请码失败: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
      .then(() => {
        message.success('已复制到剪贴板');
      })
      .catch(() => {
        message.error('复制失败');
      });
  };

  const copyAllCodes = () => {
    const codes = generatedCodes.map(item => item.code).join('\n');
    navigator.clipboard.writeText(codes)
      .then(() => {
        message.success('已复制所有邀请码到剪贴板');
      })
      .catch(() => {
        message.error('复制失败');
      });
  };

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
          <Title level={2} style={{ margin: 0 }}>生成邀请码</Title>
        </div>

        <Form
          form={form}
          name="generate_codes"
          onFinish={onFinish}
          layout="vertical"
          initialValues={{ count: 10 }}
        >
          <Form.Item
            label="生成数量"
            name="count"
            rules={[
              { required: true, message: '请输入需要生成的邀请码数量' },
              { type: 'number', min: 1, max: 100, message: '请输入1-100之间的数字' }
            ]}
          >
            <InputNumber min={1} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="批次描述"
            name="description"
          >
            <Input placeholder="可选，用于标记此批邀请码的用途" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading}>
              生成邀请码
            </Button>
          </Form.Item>
        </Form>

        {generatedCodes.length > 0 && (
          <div className="mt-20">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <Title level={4}>生成的邀请码</Title>
              <Button 
                icon={<CopyOutlined />} 
                onClick={copyAllCodes}
              >
                复制全部
              </Button>
            </div>
            
            <List
              bordered
              dataSource={generatedCodes}
              renderItem={item => (
                <List.Item
                  actions={[
                    <Button 
                      key="copy" 
                      type="link" 
                      icon={<CopyOutlined />}
                      onClick={() => copyToClipboard(item.code)}
                    >
                      复制
                    </Button>
                  ]}
                >
                  <span className="code-value">{item.code}</span>
                </List.Item>
              )}
            />
          </div>
        )}
      </Card>
    </div>
  );
};

export default GenerateCodePage; 