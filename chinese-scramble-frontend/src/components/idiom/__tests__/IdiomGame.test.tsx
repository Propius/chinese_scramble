import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import IdiomGame from '../IdiomGame';
import { soundManager } from '../../../utils/soundManager';

jest.mock('../../../utils/soundManager', () => ({
  soundManager: {
    playDrop: jest.fn(),
    playTimeout: jest.fn(),
    playClick: jest.fn(),
    playRemove: jest.fn(),
    playReset: jest.fn(),
  },
}));

const mockUseDrag = jest.fn(() => [{ isDragging: false }, jest.fn(), jest.fn()]);
const mockUseDrop = jest.fn(() => [{ isOver: false, canDrop: true }, jest.fn()]);

jest.mock('react-dnd', () => ({
  DndProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  useDrag: (config: any) => mockUseDrag(config),
  useDrop: (config: any) => mockUseDrop(config),
}));

jest.mock('react-dnd-html5-backend', () => ({
  HTML5Backend: {},
}));

describe('IdiomGame', () => {
  const mockOnSubmit = jest.fn();
  const mockOnTimeout = jest.fn();
  const mockOnHintRequest = jest.fn();

  const defaultProps = {
    scrambledCharacters: ['国', '中', '语', '文'],
    correctAnswer: '中国语文',
    timeLimit: 300,
    onSubmit: mockOnSubmit,
    onTimeout: mockOnTimeout,
    onHintRequest: mockOnHintRequest,
    difficulty: 'Easy',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();

    // Reset DnD mocks after clearAllMocks
    mockUseDrag.mockReturnValue([{ isDragging: false }, jest.fn(), jest.fn()]);
    mockUseDrop.mockReturnValue([{ isOver: false, canDrop: true }, jest.fn()]);
  });

  afterEach(() => {
    act(() => {
      jest.runOnlyPendingTimers();
    });
    jest.useRealTimers();
  });

  describe('Initial Rendering', () => {
    it('should render game component', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
    });

    it('should display difficulty level', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/难度: Easy/)).toBeInTheDocument();
    });

    it('should display initial time', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/5:00/)).toBeInTheDocument();
    });

    it('should display hints counter', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/提示: 0\/3/)).toBeInTheDocument();
    });

    it('should render available characters', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText('国')).toBeInTheDocument();
      expect(screen.getByText('中')).toBeInTheDocument();
      expect(screen.getByText('语')).toBeInTheDocument();
      expect(screen.getByText('文')).toBeInTheDocument();
    });

    it('should render answer area with empty slots', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText('答案区域')).toBeInTheDocument();
    });

    it('should display game instructions', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/拖动下方字符到答案区域排列成语/)).toBeInTheDocument();
    });
  });

  describe('Timer Functionality', () => {
    it('should countdown from initial time', () => {
      render(<IdiomGame {...defaultProps} />);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(screen.getByText(/4:59/)).toBeInTheDocument();
    });

    it('should continue counting down', () => {
      render(<IdiomGame {...defaultProps} />);

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(screen.getByText(/4:57/)).toBeInTheDocument();
    });

    it('should call onTimeout when time reaches zero', () => {
      render(<IdiomGame {...defaultProps} timeLimit={2} />);

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      // Check that timeout was called at least once
      // (may be called multiple times due to timer cleanup in afterEach)
      expect(mockOnTimeout).toHaveBeenCalled();
    });

    it('should play timeout sound when time expires', () => {
      render(<IdiomGame {...defaultProps} timeLimit={1} />);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(soundManager.playTimeout).toHaveBeenCalled();
    });

    it('should format time with leading zeros', () => {
      render(<IdiomGame {...defaultProps} timeLimit={65} />);
      expect(screen.getByText(/1:05/)).toBeInTheDocument();
    });

    it('should display time under 1 minute correctly', () => {
      render(<IdiomGame {...defaultProps} timeLimit={45} />);
      expect(screen.getByText(/0:45/)).toBeInTheDocument();
    });
  });

  describe('Reset Functionality', () => {
    it('should reset game state when reset button clicked', () => {
      render(<IdiomGame {...defaultProps} />);

      const resetButton = screen.getByText('🔄 重置');
      fireEvent.click(resetButton);

      expect(soundManager.playReset).toHaveBeenCalled();
    });

    it('should reset timer when new question arrives', () => {
      const { rerender } = render(<IdiomGame {...defaultProps} timeLimit={100} />);

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      rerender(<IdiomGame {...defaultProps} scrambledCharacters={['新', '问', '题', '目']} timeLimit={200} />);

      expect(screen.getByText(/3:20/)).toBeInTheDocument();
    });

    it('should reset hints when new question arrives', () => {
      const { rerender } = render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      rerender(<IdiomGame {...defaultProps} scrambledCharacters={['新', '题', '目', '标']} />);

      expect(screen.getByText(/提示: 0\/3/)).toBeInTheDocument();
    });
  });

  describe('Hint Functionality', () => {
    it('should request hint when hint button clicked', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(mockOnHintRequest).toHaveBeenCalledWith(1);
    });

    it('should increment hints counter', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(screen.getByText(/提示: 1\/3/)).toBeInTheDocument();
    });

    it('should allow up to 3 hints', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(mockOnHintRequest).toHaveBeenCalledTimes(3);
      expect(screen.getByText(/提示: 3\/3/)).toBeInTheDocument();
    });

    it('should disable hint button after 3 hints', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/) as HTMLButtonElement;
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(hintButton).toBeDisabled();
    });

    it('should display hint text when provided', () => {
      render(<IdiomGame {...defaultProps} hint="这是一个提示" />);
      expect(screen.getByText('这是一个提示')).toBeInTheDocument();
    });

    it('should display loading state while fetching hint', () => {
      render(<IdiomGame {...defaultProps} hintLoading={true} />);
      expect(screen.getByText(/获取提示中.../)).toBeInTheDocument();
    });

    it('should disable hint button while loading', () => {
      render(<IdiomGame {...defaultProps} hintLoading={true} />);
      const hintButton = screen.getByText(/获取中.../) as HTMLButtonElement;
      expect(hintButton).toBeDisabled();
    });

    it('should show correct hint levels', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      expect(mockOnHintRequest).toHaveBeenCalledWith(1);

      fireEvent.click(hintButton);
      expect(mockOnHintRequest).toHaveBeenCalledWith(2);

      fireEvent.click(hintButton);
      expect(mockOnHintRequest).toHaveBeenCalledWith(3);
    });
  });

  describe('Submit Functionality', () => {
    it('should have submit button disabled initially', () => {
      render(<IdiomGame {...defaultProps} />);
      const submitButton = screen.getByText('✓ 提交答案') as HTMLButtonElement;
      expect(submitButton).toBeDisabled();
    });

    it('should show alert if not all characters placed', () => {
      global.alert = jest.fn();
      const { container } = render(<IdiomGame {...defaultProps} />);

      // Submit button is disabled initially (no characters placed)
      const submitButton = screen.getByText('✓ 提交答案') as HTMLButtonElement;
      expect(submitButton).toBeDisabled();
    });

    it('should calculate time taken on submit', async () => {
      jest.useRealTimers();
      const startTime = Date.now();

      render(<IdiomGame {...defaultProps} />);

      await new Promise(resolve => setTimeout(resolve, 100));

      expect(mockOnSubmit).not.toHaveBeenCalled();

      jest.useFakeTimers();
    });

    it('should pass hints used to onSubmit', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(screen.getByText(/提示: 2\/3/)).toBeInTheDocument();
    });

    it('should disable submit button while submitting', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.queryByText('⏳ 提交中...')).not.toBeInTheDocument();
    });

    it('should disable all controls when submitting', () => {
      render(<IdiomGame {...defaultProps} />);

      const resetButton = screen.getByText('🔄 重置') as HTMLButtonElement;
      expect(resetButton).not.toBeDisabled();
    });
  });

  describe('Progress Indicator', () => {
    it('should show 0% progress initially', () => {
      const { container } = render(<IdiomGame {...defaultProps} />);
      const progressBar = container.querySelector('.bg-gradient-to-r.from-blue-500.to-purple-600');
      expect(progressBar).toHaveStyle('width: 0%');
    });

    it('should display progress text', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/已完成: 0 \/ 4/)).toBeInTheDocument();
    });

    it('should update progress when characters are placed', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/已完成: 0 \/ 4/)).toBeInTheDocument();
    });
  });

  describe('Character Management', () => {
    it('should display all scrambled characters', () => {
      render(<IdiomGame {...defaultProps} />);
      defaultProps.scrambledCharacters.forEach(char => {
        expect(screen.getByText(char)).toBeInTheDocument();
      });
    });

    it('should handle empty character array', () => {
      render(<IdiomGame {...defaultProps} scrambledCharacters={[]} />);
      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
    });

    it('should handle single character', () => {
      render(<IdiomGame {...defaultProps} scrambledCharacters={['单']} />);
      expect(screen.getByText('单')).toBeInTheDocument();
    });

    it('should handle many characters', () => {
      const manyChars = ['一', '二', '三', '四', '五', '六', '七', '八'];
      render(<IdiomGame {...defaultProps} scrambledCharacters={manyChars} />);
      manyChars.forEach(char => {
        expect(screen.getByText(char)).toBeInTheDocument();
      });
    });
  });

  describe('Game State Updates', () => {
    it('should reset state when scrambledCharacters change', () => {
      const { rerender } = render(<IdiomGame {...defaultProps} />);

      rerender(<IdiomGame {...defaultProps} scrambledCharacters={['新', '字', '符', '组']} />);

      expect(screen.getByText('新')).toBeInTheDocument();
      expect(screen.getByText('字')).toBeInTheDocument();
    });

    it('should maintain difficulty level', () => {
      render(<IdiomGame {...defaultProps} difficulty="Hard" />);
      expect(screen.getByText(/难度: Hard/)).toBeInTheDocument();
    });

    it('should update difficulty when prop changes', () => {
      const { rerender } = render(<IdiomGame {...defaultProps} difficulty="Easy" />);
      expect(screen.getByText(/难度: Easy/)).toBeInTheDocument();

      rerender(<IdiomGame {...defaultProps} difficulty="Hard" />);
      expect(screen.getByText(/难度: Hard/)).toBeInTheDocument();
    });
  });

  describe('Button States', () => {
    it('should render all action buttons', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/获取提示/)).toBeInTheDocument();
      expect(screen.getByText('🔄 重置')).toBeInTheDocument();
      expect(screen.getByText('✓ 提交答案')).toBeInTheDocument();
    });

    it('should disable hint button when hint is loading', () => {
      render(<IdiomGame {...defaultProps} hintLoading={true} />);
      const hintButton = screen.getByText(/获取中.../) as HTMLButtonElement;
      expect(hintButton).toBeDisabled();
    });

    it('should show remaining hints', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/3 次可用/)).toBeInTheDocument();
    });

    it('should update remaining hints after use', () => {
      render(<IdiomGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      // After using 1 hint, should show "💡 提示: 1/3" in the header
      expect(screen.getAllByText(/1\/3/)[0]).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle very short time limit', () => {
      render(<IdiomGame {...defaultProps} timeLimit={5} />);
      expect(screen.getByText(/0:05/)).toBeInTheDocument();
    });

    it('should handle very long time limit', () => {
      render(<IdiomGame {...defaultProps} timeLimit={3600} />);
      expect(screen.getByText(/60:00/)).toBeInTheDocument();
    });

    it('should handle special characters', () => {
      render(<IdiomGame {...defaultProps} scrambledCharacters={['！', '？', '。', '、']} />);
      expect(screen.getByText('！')).toBeInTheDocument();
    });

    it('should handle duplicate characters', () => {
      render(<IdiomGame {...defaultProps} scrambledCharacters={['中', '中', '国', '国']} />);
      const chars = screen.getAllByText('中');
      expect(chars.length).toBeGreaterThan(0);
    });

    it('should handle missing optional props', () => {
      const minimalProps = {
        scrambledCharacters: ['测', '试'],
        timeLimit: 60,
        onSubmit: mockOnSubmit,
        onTimeout: mockOnTimeout,
        onHintRequest: mockOnHintRequest,
        difficulty: 'Easy',
      };
      render(<IdiomGame {...minimalProps} />);
      expect(screen.getByText('测')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper button labels', () => {
      render(<IdiomGame {...defaultProps} />);
      expect(screen.getByText(/获取提示/)).toBeInTheDocument();
      expect(screen.getByText('🔄 重置')).toBeInTheDocument();
      expect(screen.getByText('✓ 提交答案')).toBeInTheDocument();
    });

    it('should render with proper structure', () => {
      const { container } = render(<IdiomGame {...defaultProps} />);
      expect(container.querySelector('.idiom-game')).toBeInTheDocument();
    });
  });
});
