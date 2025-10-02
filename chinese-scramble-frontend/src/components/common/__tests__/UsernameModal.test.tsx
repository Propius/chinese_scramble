import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UsernameModal from '../UsernameModal';

describe('UsernameModal', () => {
  const mockOnSubmit = jest.fn();

  beforeEach(() => {
    mockOnSubmit.mockClear();
  });

  it('should render modal when show is true', () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);
    expect(screen.getByText('欢迎来到汉字游戏')).toBeInTheDocument();
  });

  it('should not render modal when show is false', () => {
    render(<UsernameModal show={false} onSubmit={mockOnSubmit} />);
    expect(screen.queryByText('欢迎来到汉字游戏')).not.toBeInTheDocument();
  });

  it('should show validation error for empty username on submit', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByText('开始游戏');

    // Submit button should be disabled for empty username
    expect(submitButton).toBeDisabled();

    // Try to submit the form (won't work because button is disabled, but we can test the validation logic)
    const form = submitButton.closest('form');
    if (form) {
      fireEvent.submit(form);

      await waitFor(() => {
        expect(screen.getByText('请输入用户名')).toBeInTheDocument();
      });
    }
  });

  it('should show validation error for username too short', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');

    await userEvent.type(input, 'ab');
    fireEvent.blur(input);

    await waitFor(() => {
      expect(screen.getByText('用户名至少需要3个字符')).toBeInTheDocument();
    });
  });

  it('should show validation error for username too long', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');

    await userEvent.type(input, 'a'.repeat(21));
    fireEvent.blur(input);

    await waitFor(() => {
      expect(screen.getByText('用户名不能超过20个字符')).toBeInTheDocument();
    });
  });

  it('should show validation error for invalid characters', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');

    await userEvent.type(input, 'test@user');
    fireEvent.blur(input);

    await waitFor(() => {
      expect(screen.getByText('用户名只能包含字母、数字、中文和下划线')).toBeInTheDocument();
    });
  });

  it('should accept valid username', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');
    const submitButton = screen.getByText('开始游戏');

    await userEvent.type(input, 'validUser123');

    expect(submitButton).not.toBeDisabled();
  });

  it('should call onSubmit with trimmed username', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');
    const submitButton = screen.getByText('开始游戏');

    await userEvent.type(input, '  testUser  ');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith('testUser');
    });
  });

  it('should accept Chinese characters in username', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');
    const submitButton = screen.getByText('开始游戏');

    await userEvent.type(input, '测试用户');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith('测试用户');
    });
  });

  it('should disable submit button for invalid username', async () => {
    render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');
    const submitButton = screen.getByText('开始游戏');

    await userEvent.type(input, 'ab');

    expect(submitButton).toBeDisabled();
  });

  it('should reset form when modal is reopened', () => {
    const { rerender } = render(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText('输入3-20个字符的用户名');
    fireEvent.change(input, { target: { value: 'testuser' } });

    rerender(<UsernameModal show={false} onSubmit={mockOnSubmit} />);
    rerender(<UsernameModal show={true} onSubmit={mockOnSubmit} />);

    const newInput = screen.getByPlaceholderText('输入3-20个字符的用户名');
    expect(newInput).toHaveValue('');
  });
});
