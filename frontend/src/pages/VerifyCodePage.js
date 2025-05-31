import React, { useState } from 'react';
import { Button, Form, Input, Alert, Typography, Spin } from 'antd';
import { verifyInviteCode } from '../api/inviteCodeApi';

const { Title, Paragraph } = Typography;

const VerifyCodePage = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const response = await verifyInviteCode(values.code, values.userId);
      const data = response.data;
      setResult({
        success: data.success,
        message: data.message
      });
      if (data.success) {
        form.resetFields();
      }
    } catch (error) {
      setResult({
        success: false,
        message: error.response?.data?.message || '验证失败，请稍后再试'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="verify-code-container">
      <Title level={2} className="text-center">AI IDE 邀请码验证</Title>
      <Paragraph className="text-center">
        请输入您收到的邀请码以获取使用资格
      </Paragraph>

      <Form
        form={form}
        name="verify_code"
        onFinish={onFinish}
        layout="vertical"
      >
        <Form.Item
          name="code"
          rules={[{ required: true, message: '请输入邀请码' }]}
        >
          <Input 
            placeholder="请输入邀请码" 
            size="large"
            maxLength={16}
          />
        </Form.Item>

        <Form.Item name="userId">
          <Input 
            placeholder="请输入用户ID（可选）" 
            size="large"
          />
        </Form.Item>

        <Form.Item>
          <Button 
            type="primary" 
            htmlType="submit" 
            size="large" 
            block
            loading={loading}
          >
            验证邀请码
          </Button>
        </Form.Item>
      </Form>

      {result && (
        <div className={`result-container ${result.success ? 'success' : 'error'}`}>
          {result.success ? (
            <>
              <Title level={4}>邀请码验证成功！</Title>
              <Paragraph>
                恭喜您获得了访问AI IDE的资格。您现在可以下载并使用我们的IDE工具。
              </Paragraph>
              <Button type="primary" href="https://company-ai-ide.example.com/download">
                下载AI IDE
              </Button>
            </>
          ) : (
            <>
              <Title level={4}>邀请码验证失败</Title>
              <Paragraph>{result.message}</Paragraph>
            </>
          )}
        </div>
      )}

      <div className="text-center mt-20">
        <Button type="link" href="/admin/login">管理员入口</Button>
      </div>
    </div>
  );
};

export default VerifyCodePage; 