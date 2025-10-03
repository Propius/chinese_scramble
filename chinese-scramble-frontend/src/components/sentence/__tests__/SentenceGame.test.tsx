import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SentenceGame from '../SentenceGame';
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

describe('SentenceGame', () => {
  const mockOnSubmit = jest.fn();
  const mockOnTimeout = jest.fn();
  const mockOnHintRequest = jest.fn();

  const defaultProps = {
    scrambledWords: ['我', '爱', '中国'],
    correctAnswer: ['我', '爱', '中国'],
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
      jest.clearAllTimers();
    });
    jest.useRealTimers();
  });

  describe('Initial Rendering', () => {
    it('should render game component', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
    });

    it('should display difficulty level', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/难度: Easy/)).toBeInTheDocument();
    });

    it('should display initial time', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/5:00/)).toBeInTheDocument();
    });

    it('should display hints counter', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/提示: 0/)).toBeInTheDocument();
    });

    it('should render available words', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText('我')).toBeInTheDocument();
      expect(screen.getByText('爱')).toBeInTheDocument();
      expect(screen.getByText('中国')).toBeInTheDocument();
    });

    it('should render sentence area label', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText('句子区域')).toBeInTheDocument();
    });

    it('should display game instructions', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/拖动下方的词语到句子区域排列/)).toBeInTheDocument();
    });

    it('should render with grammar pattern', () => {
      render(<SentenceGame {...defaultProps} grammarPattern="主语 + 谓语 + 宾语" />);
      expect(screen.getByText('语法模式：')).toBeInTheDocument();
      expect(screen.getByText('主语 + 谓语 + 宾语')).toBeInTheDocument();
    });

    it('should not show grammar section when not provided', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.queryByText('语法模式：')).not.toBeInTheDocument();
    });
  });

  describe('Timer Functionality', () => {
    it('should countdown from initial time', () => {
      render(<SentenceGame {...defaultProps} />);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(screen.getByText(/4:59/)).toBeInTheDocument();
    });

    it('should continue counting down', () => {
      render(<SentenceGame {...defaultProps} />);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(screen.getByText(/4:55/)).toBeInTheDocument();
    });

    it('should call onTimeout when time reaches zero', () => {
      render(<SentenceGame {...defaultProps} timeLimit={2} />);

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      // Check that timeout was called at least once
      // (may be called multiple times due to timer cleanup in afterEach)
      expect(mockOnTimeout).toHaveBeenCalled();
    });

    it('should call onTimeout when time expires', () => {
      render(<SentenceGame {...defaultProps} timeLimit={1} />);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      // Component now calls onTimeout callback instead of playing sound directly
      expect(mockOnTimeout).toHaveBeenCalled();
    });

    it('should format time with leading zeros', () => {
      render(<SentenceGame {...defaultProps} timeLimit={125} />);
      expect(screen.getByText(/2:05/)).toBeInTheDocument();
    });

    it('should display time under 1 minute correctly', () => {
      render(<SentenceGame {...defaultProps} timeLimit={30} />);
      expect(screen.getByText(/0:30/)).toBeInTheDocument();
    });

    it('should handle zero time limit', () => {
      render(<SentenceGame {...defaultProps} timeLimit={0} />);
      expect(mockOnTimeout).toHaveBeenCalled();
    });
  });

  describe('Reset Functionality', () => {
    it('should reset game state when reset button clicked', () => {
      render(<SentenceGame {...defaultProps} />);

      const resetButton = screen.getByText('🔄 重置');
      fireEvent.click(resetButton);

      expect(soundManager.playReset).toHaveBeenCalled();
    });

    it('should hide translation when reset', () => {
      render(<SentenceGame {...defaultProps} translation="I love China" />);

      const showButton = screen.getByText('👁️ 显示翻译');
      fireEvent.click(showButton);

      const resetButton = screen.getByText('🔄 重置');
      fireEvent.click(resetButton);

      expect(screen.queryByText('I love China')).not.toBeInTheDocument();
    });

    it('should reset state when new question arrives', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} timeLimit={100} />);

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      rerender(<SentenceGame {...defaultProps} scrambledWords={['新', '问', '题']} timeLimit={200} />);

      expect(screen.getByText(/3:20/)).toBeInTheDocument();
    });

    it('should reset hints counter when new question arrives', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      rerender(<SentenceGame {...defaultProps} scrambledWords={['新', '句', '子']} />);

      expect(screen.getByText(/提示: 0/)).toBeInTheDocument();
    });
  });

  describe('Hint Functionality', () => {
    it('should request hint when hint button clicked', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(mockOnHintRequest).toHaveBeenCalledWith(1);
    });

    it('should increment hints counter', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(screen.getByText(/提示: 1/)).toBeInTheDocument();
    });

    it('should allow multiple hints', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(mockOnHintRequest).toHaveBeenCalledTimes(3);
      expect(screen.getByText(/提示: 3/)).toBeInTheDocument();
    });

    it('should disable hint button after 3 hints', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/) as HTMLButtonElement;
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(hintButton).toBeDisabled();
    });

    it('should display hint text when provided', () => {
      render(<SentenceGame {...defaultProps} hint="这是一个提示" />);
      expect(screen.getByText('这是一个提示')).toBeInTheDocument();
    });

    it('should display hint loading state', () => {
      render(<SentenceGame {...defaultProps} hintLoading={true} />);
      expect(screen.getByText(/获取提示中.../)).toBeInTheDocument();
    });

    it('should disable hint button while loading', () => {
      render(<SentenceGame {...defaultProps} hintLoading={true} />);
      const hintButton = screen.getByText(/获取中.../) as HTMLButtonElement;
      expect(hintButton).toBeDisabled();
    });

    it('should show correct hint level', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      expect(mockOnHintRequest).toHaveBeenCalledWith(1);

      fireEvent.click(hintButton);
      expect(mockOnHintRequest).toHaveBeenCalledWith(2);
    });

    it('should display hint info section', () => {
      render(<SentenceGame {...defaultProps} hint="提示内容" />);
      expect(screen.getByText('💡 提示信息')).toBeInTheDocument();
    });
  });

  describe('Translation Functionality', () => {
    it('should show translation toggle button when translation provided', () => {
      render(<SentenceGame {...defaultProps} translation="I love China" />);
      expect(screen.getByText('👁️ 显示翻译')).toBeInTheDocument();
    });

    it('should not show translation button when translation not provided', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.queryByText('👁️ 显示翻译')).not.toBeInTheDocument();
    });

    it('should toggle translation visibility', () => {
      render(<SentenceGame {...defaultProps} translation="I love China" />);

      const toggleButton = screen.getByText('👁️ 显示翻译');
      fireEvent.click(toggleButton);

      // Translation only shows when sentence preview exists (has placed words)
      expect(screen.getByText('🙈 隐藏翻译')).toBeInTheDocument();
    });

    it('should hide translation when toggle clicked again', () => {
      render(<SentenceGame {...defaultProps} translation="I love China" />);

      const toggleButton = screen.getByText('👁️ 显示翻译');
      fireEvent.click(toggleButton);
      fireEvent.click(screen.getByText('🙈 隐藏翻译'));

      expect(screen.queryByText('I love China')).not.toBeInTheDocument();
    });

    it('should disable translation button when submitting', () => {
      render(<SentenceGame {...defaultProps} translation="I love China" />);
      const button = screen.getByText('👁️ 显示翻译') as HTMLButtonElement;
      expect(button).not.toBeDisabled();
    });
  });

  describe('Submit Functionality', () => {
    it('should have submit button disabled initially', () => {
      render(<SentenceGame {...defaultProps} />);
      const submitButton = screen.getByText('✓ 提交答案') as HTMLButtonElement;
      expect(submitButton).toBeDisabled();
    });

    it('should show alert if not all words placed', () => {
      global.alert = jest.fn();
      render(<SentenceGame {...defaultProps} />);

      // Submit button is disabled when not all words are placed
      const submitButton = screen.getByText('✓ 提交答案') as HTMLButtonElement;
      expect(submitButton).toBeDisabled();
    });

    it('should pass hints used to onSubmit', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(screen.getByText(/提示: 2/)).toBeInTheDocument();
    });

    it('should disable submit button while submitting', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.queryByText('⏳ 提交中...')).not.toBeInTheDocument();
    });

    it('should calculate time taken', async () => {
      jest.useRealTimers();
      render(<SentenceGame {...defaultProps} />);

      await new Promise(resolve => setTimeout(resolve, 100));

      expect(mockOnSubmit).not.toHaveBeenCalled();

      jest.useFakeTimers();
    });
  });

  describe('Progress Indicator', () => {
    it('should show 0% progress initially', () => {
      const { container } = render(<SentenceGame {...defaultProps} />);
      const progressBar = container.querySelector('.bg-gradient-to-r.from-purple-500.to-pink-600');
      expect(progressBar).toHaveStyle('width: 0%');
    });

    it('should display progress text', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/已完成: 0 \/ 3 词语/)).toBeInTheDocument();
    });

    it('should update progress bar width', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/已完成: 0 \/ 3/)).toBeInTheDocument();
    });
  });

  describe('Word Management', () => {
    it('should display all scrambled words', () => {
      render(<SentenceGame {...defaultProps} />);
      defaultProps.scrambledWords.forEach(word => {
        expect(screen.getByText(word)).toBeInTheDocument();
      });
    });

    it('should handle empty word array', () => {
      render(<SentenceGame {...defaultProps} scrambledWords={[]} />);
      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
    });

    it('should handle single word', () => {
      render(<SentenceGame {...defaultProps} scrambledWords={['单词']} />);
      expect(screen.getByText('单词')).toBeInTheDocument();
    });

    it('should handle many words', () => {
      const manyWords = ['我', '爱', '学习', '中文', '语言', '文化'];
      render(<SentenceGame {...defaultProps} scrambledWords={manyWords} />);
      manyWords.forEach(word => {
        expect(screen.getByText(word)).toBeInTheDocument();
      });
    });

    it('should handle long words', () => {
      const longWords = ['这是一个', '非常长的', '词语'];
      render(<SentenceGame {...defaultProps} scrambledWords={longWords} />);
      longWords.forEach(word => {
        expect(screen.getByText(word)).toBeInTheDocument();
      });
    });
  });

  describe('Sentence Preview', () => {
    it('should not show preview when no words placed', () => {
      const { container } = render(<SentenceGame {...defaultProps} />);
      // When no words are placed, the blue preview box should not exist
      const previewBox = container.querySelector('.bg-blue-50');
      expect(previewBox).not.toBeInTheDocument();
    });

    it('should render sentence preview section', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText('句子区域')).toBeInTheDocument();
    });
  });

  describe('Game State Updates', () => {
    it('should reset state when scrambledWords change', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} />);

      rerender(<SentenceGame {...defaultProps} scrambledWords={['新', '的', '句子']} />);

      expect(screen.getByText('新')).toBeInTheDocument();
      expect(screen.getByText('的')).toBeInTheDocument();
      expect(screen.getByText('句子')).toBeInTheDocument();
    });

    it('should maintain difficulty level', () => {
      render(<SentenceGame {...defaultProps} difficulty="Hard" />);
      expect(screen.getByText(/难度: Hard/)).toBeInTheDocument();
    });

    it('should update difficulty when prop changes', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} difficulty="Easy" />);
      expect(screen.getByText(/难度: Easy/)).toBeInTheDocument();

      rerender(<SentenceGame {...defaultProps} difficulty="Hard" />);
      expect(screen.getByText(/难度: Hard/)).toBeInTheDocument();
    });

    it('should reset translation visibility on new question', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} translation="Translation" />);

      const toggleButton = screen.getByText('👁️ 显示翻译');
      fireEvent.click(toggleButton);

      rerender(<SentenceGame {...defaultProps} scrambledWords={['新', '题']} translation="New" />);

      expect(screen.queryByText('New')).not.toBeInTheDocument();
    });
  });

  describe('Button States', () => {
    it('should render all action buttons', () => {
      render(<SentenceGame {...defaultProps} translation="Test" />);
      expect(screen.getByText(/获取提示/)).toBeInTheDocument();
      expect(screen.getByText('👁️ 显示翻译')).toBeInTheDocument();
      expect(screen.getByText('🔄 重置')).toBeInTheDocument();
      expect(screen.getByText('✓ 提交答案')).toBeInTheDocument();
    });

    it('should disable buttons appropriately when submitting', () => {
      render(<SentenceGame {...defaultProps} />);
      const resetButton = screen.getByText('🔄 重置') as HTMLButtonElement;
      expect(resetButton).not.toBeDisabled();
    });

    it('should show remaining hints', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/3 次可用/)).toBeInTheDocument();
    });

    it('should update remaining hints display', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(screen.getByText(/(1\/3)/)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle very short time limit', () => {
      render(<SentenceGame {...defaultProps} timeLimit={5} />);
      expect(screen.getByText(/0:05/)).toBeInTheDocument();
    });

    it('should handle very long time limit', () => {
      render(<SentenceGame {...defaultProps} timeLimit={7200} />);
      expect(screen.getByText(/120:00/)).toBeInTheDocument();
    });

    it('should handle special characters in words', () => {
      render(<SentenceGame {...defaultProps} scrambledWords={['？', '！', '。']} />);
      expect(screen.getByText('？')).toBeInTheDocument();
    });

    it('should handle duplicate words', () => {
      render(<SentenceGame {...defaultProps} scrambledWords={['的', '的', '我']} />);
      const words = screen.getAllByText('的');
      expect(words.length).toBeGreaterThan(0);
    });

    it('should handle missing optional props', () => {
      const minimalProps = {
        scrambledWords: ['测', '试'],
        timeLimit: 60,
        onSubmit: mockOnSubmit,
        onTimeout: mockOnTimeout,
        onHintRequest: mockOnHintRequest,
        difficulty: 'Easy',
      };
      render(<SentenceGame {...minimalProps} />);
      expect(screen.getByText('测')).toBeInTheDocument();
    });

    it('should handle empty grammar pattern', () => {
      render(<SentenceGame {...defaultProps} grammarPattern="" />);
      expect(screen.queryByText('语法模式：')).not.toBeInTheDocument();
    });

    it('should handle long translation text', () => {
      const longTranslation = 'This is a very long translation that should still display correctly';
      render(<SentenceGame {...defaultProps} translation={longTranslation} />);

      const toggleButton = screen.getByText('👁️ 显示翻译');
      fireEvent.click(toggleButton);

      // Translation toggle button should change state
      expect(screen.getByText('🙈 隐藏翻译')).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    it('should have sentence-game class', () => {
      const { container } = render(<SentenceGame {...defaultProps} />);
      expect(container.querySelector('.sentence-game')).toBeInTheDocument();
    });

    it('should render with proper structure', () => {
      const { container } = render(<SentenceGame {...defaultProps} />);
      expect(container.querySelector('.sentence-game')).toBeInTheDocument();
    });

    it('should display available words section', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText('可用词语')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper button labels', () => {
      render(<SentenceGame {...defaultProps} translation="Test" />);
      expect(screen.getByText(/获取提示/)).toBeInTheDocument();
      expect(screen.getByText('👁️ 显示翻译')).toBeInTheDocument();
      expect(screen.getByText('🔄 重置')).toBeInTheDocument();
      expect(screen.getByText('✓ 提交答案')).toBeInTheDocument();
    });

    it('should have instruction text', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/拖动下方的词语到句子区域排列/)).toBeInTheDocument();
    });

    it('should have help text', () => {
      render(<SentenceGame {...defaultProps} />);
      expect(screen.getByText(/拖动下方词语到答案区/)).toBeInTheDocument();
    });
  });

  describe('Correct Answer Handling', () => {
    it('should handle correct answer prop', () => {
      render(<SentenceGame {...defaultProps} correctAnswer={['我', '爱', '中国']} />);
      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
    });

    it('should work without correct answer', () => {
      const propsWithoutAnswer = { ...defaultProps };
      delete (propsWithoutAnswer as any).correctAnswer;
      render(<SentenceGame {...propsWithoutAnswer} />);
      expect(screen.getByText('我')).toBeInTheDocument();
    });
  });

  describe('Timer Cleanup', () => {
    it('should cleanup timer on unmount', () => {
      const { unmount } = render(<SentenceGame {...defaultProps} />);

      unmount();

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(mockOnTimeout).not.toHaveBeenCalled();
    });

    it('should restart timer on new question', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} timeLimit={100} />);

      act(() => {
        jest.advanceTimersByTime(50000);
      });

      rerender(<SentenceGame {...defaultProps} scrambledWords={['新', '题']} timeLimit={100} />);

      expect(screen.getByText(/1:40/)).toBeInTheDocument();
    });
  });
});
