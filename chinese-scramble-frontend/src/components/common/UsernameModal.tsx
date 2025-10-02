import React, { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';

interface UsernameModalProps {
  show: boolean;
  onSubmit: (username: string) => void;
  onClose?: () => void;
}

export const UsernameModal: React.FC<UsernameModalProps> = ({ show, onSubmit, onClose }) => {
  const [username, setUsername] = useState('');
  const [error, setError] = useState('');
  const [touched, setTouched] = useState(false);

  useEffect(() => {
    if (show) {
      setUsername('');
      setError('');
      setTouched(false);
    }
  }, [show]);

  const validateUsername = (value: string): string => {
    if (!value || value.trim().length === 0) {
      return '请输入用户名';
    }
    if (value.trim().length < 3) {
      return '用户名至少需要3个字符';
    }
    if (value.trim().length > 20) {
      return '用户名不能超过20个字符';
    }
    if (!/^[a-zA-Z0-9\u4e00-\u9fa5_]+$/.test(value.trim())) {
      return '用户名只能包含字母、数字、中文和下划线';
    }
    return '';
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setUsername(value);
    if (touched) {
      setError(validateUsername(value));
    }
  };

  const handleBlur = () => {
    // Only show validation on blur if user has typed something
    if (username.length > 0) {
      setTouched(true);
      setError(validateUsername(username));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setTouched(true);
    const validationError = validateUsername(username);
    if (validationError) {
      setError(validationError);
      return;
    }
    onSubmit(username.trim());
  };

  const handleClose = () => {
    if (onClose) {
      // Reset state when closing without submitting
      setUsername('');
      setError('');
      setTouched(false);
      onClose();
    }
  };

  const isValid = username.trim().length >= 3 && username.trim().length <= 20;

  return (
    <Modal
      show={show}
      onHide={handleClose}
      centered
      size="lg"
    >
      <Modal.Header closeButton className="border-0 pb-0" onHide={handleClose}>
        <Modal.Title className="w-100 text-center">
          <div className="d-flex flex-column align-items-center gap-2">
            <span style={{ fontSize: '3rem' }}>🀄</span>
            <h4 className="mb-0">欢迎来到汉字游戏</h4>
          </div>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body className="px-5 py-4">
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label className="fw-semibold">请输入您的用户名</Form.Label>
            <Form.Control
              type="text"
              placeholder="输入3-20个字符的用户名"
              value={username}
              onChange={handleChange}
              onBlur={handleBlur}
              isInvalid={touched && !!error}
              autoFocus
              style={{
                padding: '0.75rem',
                fontSize: '1rem',
                borderRadius: '8px',
              }}
            />
            <Form.Control.Feedback type="invalid">
              {error}
            </Form.Control.Feedback>
            <Form.Text className="text-muted">
              用户名将用于保存您的游戏成绩和排名
            </Form.Text>
          </Form.Group>
          <div className="d-grid gap-2">
            <Button
              variant="primary"
              type="submit"
              size="lg"
              disabled={!isValid}
              style={{
                borderRadius: '8px',
                padding: '0.75rem',
                fontWeight: '600',
              }}
            >
              开始游戏
            </Button>
          </div>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default UsernameModal;
