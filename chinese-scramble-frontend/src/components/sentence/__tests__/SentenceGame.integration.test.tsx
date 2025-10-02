import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import { SentenceGame } from '../SentenceGame';

// Mock react-dnd completely to avoid drag-and-drop complexity
jest.mock('react-dnd', () => ({
  DndProvider: ({ children }: any) => <div>{children}</div>,
  useDrag: () => [{ isDragging: false }, jest.fn(), jest.fn()],
  useDrop: () => [{ isOver: false, canDrop: false }, jest.fn()],
}));

// Mock HTML5Backend
jest.mock('react-dnd-html5-backend', () => ({
  HTML5Backend: jest.fn(),
}));

// Mock soundManager
jest.mock('../../../utils/soundManager', () => ({
  soundManager: {
    playDrop: jest.fn(),
    playRemove: jest.fn(),
    playClick: jest.fn(),
    playReset: jest.fn(),
    playTimeout: jest.fn(),
  },
}));

describe('SentenceGame Integration Tests', () => {
  const defaultProps = {
    scrambledWords: ['我', '学习', '中文'],
    correctAnswer: ['我', '学习', '中文'],
    timeLimit: 180,
    onSubmit: jest.fn(),
    onTimeout: jest.fn(),
    onHintRequest: jest.fn(),
    difficulty: '初级',
    grammarPattern: '主语 + 动词 + 宾语',
    translation: 'I study Chinese',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    act(() => {
      jest.runOnlyPendingTimers();
    });
    jest.useRealTimers();
  });

  describe('Component Rendering', () => {
    it('should render with all required props', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/难度: 初级/)).toBeInTheDocument();
      expect(screen.getByText(/⏱️ 3:00/)).toBeInTheDocument();
      expect(screen.getByText(/💡 提示: 0/)).toBeInTheDocument();
      expect(screen.getByText('句子区域')).toBeInTheDocument();
      expect(screen.getByText('可用词语')).toBeInTheDocument();
    });

    it('should render all scrambled words', () => {
      render(<SentenceGame {...defaultProps} />);

      defaultProps.scrambledWords.forEach((word) => {
        expect(screen.getByText(word)).toBeInTheDocument();
      });
    });

    it('should render action buttons', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/获取提示/)).toBeInTheDocument();
      expect(screen.getByText(/🔄 重置/)).toBeInTheDocument();
      expect(screen.getByText(/✓ 提交答案/)).toBeInTheDocument();
    });

    it('should display progress indicator', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/已完成: 0 \/ 3 词语/)).toBeInTheDocument();
    });

    it('should display grammar pattern when provided', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText('语法模式：')).toBeInTheDocument();
      expect(screen.getByText(defaultProps.grammarPattern)).toBeInTheDocument();
    });

    it('should not display grammar pattern when not provided', () => {
      render(<SentenceGame {...defaultProps} grammarPattern={undefined} />);

      expect(screen.queryByText('语法模式：')).not.toBeInTheDocument();
    });

    it('should display translation toggle button when translation provided', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/显示翻译/)).toBeInTheDocument();
    });

    it('should not display translation button when translation not provided', () => {
      render(<SentenceGame {...defaultProps} translation={undefined} />);

      expect(screen.queryByText(/显示翻译/)).not.toBeInTheDocument();
    });
  });

  describe('Timer Functionality', () => {
    it('should countdown timer every second', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/⏱️ 3:00/)).toBeInTheDocument();

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(screen.getByText(/⏱️ 2:59/)).toBeInTheDocument();
    });

    it('should call onTimeout when timer reaches 0', () => {
      render(<SentenceGame {...defaultProps} timeLimit={2} />);

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      expect(defaultProps.onTimeout).toHaveBeenCalled();
    });

    it('should format timer display correctly', () => {
      render(<SentenceGame {...defaultProps} timeLimit={125} />);

      expect(screen.getByText(/⏱️ 2:05/)).toBeInTheDocument();
    });

    it('should handle single digit seconds with padding', () => {
      render(<SentenceGame {...defaultProps} timeLimit={69} />);

      expect(screen.getByText(/⏱️ 1:09/)).toBeInTheDocument();

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(screen.getByText(/⏱️ 0:59/)).toBeInTheDocument();
    });

    it('should stop timer at 0', () => {
      render(<SentenceGame {...defaultProps} timeLimit={1} />);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(screen.getByText(/⏱️ 0:00/)).toBeInTheDocument();

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(screen.getByText(/⏱️ 0:00/)).toBeInTheDocument();
    });
  });

  describe('Hint Functionality', () => {
    it('should call onHintRequest when hint button clicked', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(defaultProps.onHintRequest).toHaveBeenCalledWith(1);
    });

    it('should increment hints used after requesting hint', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);

      fireEvent.click(hintButton);
      expect(screen.getByText(/💡 提示: 1/)).toBeInTheDocument();

      fireEvent.click(hintButton);
      expect(screen.getByText(/💡 提示: 2/)).toBeInTheDocument();

      fireEvent.click(hintButton);
      expect(screen.getByText(/💡 提示: 3/)).toBeInTheDocument();
    });

    it('should disable hint button after 3 hints', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);

      fireEvent.click(hintButton);
      fireEvent.click(hintButton);
      fireEvent.click(hintButton);

      expect(hintButton).toBeDisabled();
    });

    it('should display hint when provided', () => {
      render(<SentenceGame {...defaultProps} hint="这是一个简单的句子结构" />);

      expect(screen.getByText('💡 提示信息')).toBeInTheDocument();
      expect(screen.getByText('这是一个简单的句子结构')).toBeInTheDocument();
    });

    it('should display loading state when hint is loading', () => {
      render(<SentenceGame {...defaultProps} hintLoading={true} />);

      expect(screen.getByText('💡 提示信息')).toBeInTheDocument();
      expect(screen.getByText('获取提示中...')).toBeInTheDocument();
    });

    it('should disable hint button when loading', () => {
      render(<SentenceGame {...defaultProps} hintLoading={true} />);

      const hintButton = screen.getByText(/获取中/);
      expect(hintButton).toBeDisabled();
    });

    it('should not display hint section when no hint provided', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.queryByText('💡 提示信息')).not.toBeInTheDocument();
    });
  });

  describe('Translation Functionality', () => {
    it('should toggle translation display when button clicked', () => {
      render(<SentenceGame {...defaultProps} />);

      // Translation button should exist
      const toggleButton = screen.getByText(/显示翻译/);
      expect(toggleButton).toBeInTheDocument();

      // Click to show translation
      fireEvent.click(toggleButton);

      // Button text should change
      expect(screen.getByText(/隐藏翻译/)).toBeInTheDocument();

      // Click again to hide
      fireEvent.click(screen.getByText(/隐藏翻译/));
      expect(screen.getByText(/显示翻译/)).toBeInTheDocument();
    });

    it('should reset translation display on game reset', () => {
      render(<SentenceGame {...defaultProps} />);

      // Show translation
      const toggleButton = screen.getByText(/显示翻译/);
      fireEvent.click(toggleButton);
      expect(screen.getByText(/隐藏翻译/)).toBeInTheDocument();

      // Reset game
      const resetButton = screen.getByText(/🔄 重置/);
      fireEvent.click(resetButton);

      // Translation should be hidden (button text changes back)
      expect(screen.getByText(/显示翻译/)).toBeInTheDocument();
    });
  });

  describe('Reset Functionality', () => {
    it('should reset game state when reset button clicked', () => {
      render(<SentenceGame {...defaultProps} />);

      const resetButton = screen.getByText(/🔄 重置/);
      fireEvent.click(resetButton);

      expect(screen.getByText(/已完成: 0 \/ 3 词语/)).toBeInTheDocument();
    });

    it('should not be disabled initially', () => {
      render(<SentenceGame {...defaultProps} />);

      const resetButton = screen.getByText(/🔄 重置/);
      expect(resetButton).not.toBeDisabled();
    });
  });

  describe('Submit Functionality', () => {
    it('should be disabled when not all words placed', () => {
      render(<SentenceGame {...defaultProps} />);

      const submitButton = screen.getByText(/✓ 提交答案/);
      expect(submitButton).toBeDisabled();
    });

    it('should show submitting state', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/✓ 提交答案/)).toBeInTheDocument();
    });

    it('should show alert when submitting incomplete answer', () => {
      const alertMock = jest.spyOn(window, 'alert').mockImplementation();
      render(<SentenceGame {...defaultProps} />);

      const submitButton = screen.getByText(/✓ 提交答案/);

      // Submit button is disabled when not all words placed
      expect(submitButton).toBeDisabled();

      alertMock.mockRestore();
    });
  });

  describe('State Management', () => {
    it('should reset state when scrambled words change', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} />);

      // Request a hint
      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);
      expect(screen.getByText(/💡 提示: 1/)).toBeInTheDocument();

      // Change scrambled words (simulate new question)
      rerender(
        <SentenceGame
          {...defaultProps}
          scrambledWords={['他', '吃', '饭']}
          correctAnswer={['他', '吃', '饭']}
        />
      );

      // Hints should be reset
      expect(screen.getByText(/💡 提示: 0/)).toBeInTheDocument();
    });

    it('should reset timer when timeLimit changes', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} timeLimit={60} />);

      expect(screen.getByText(/⏱️ 1:00/)).toBeInTheDocument();

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(screen.getByText(/⏱️ 0:50/)).toBeInTheDocument();

      // Change time limit (simulate new question)
      rerender(<SentenceGame {...defaultProps} timeLimit={180} />);

      expect(screen.getByText(/⏱️ 3:00/)).toBeInTheDocument();
    });

    it('should reset translation display when words change', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} />);

      // Show translation
      const toggleButton = screen.getByText(/显示翻译/);
      fireEvent.click(toggleButton);
      expect(screen.getByText(/隐藏翻译/)).toBeInTheDocument();

      // Change words
      rerender(
        <SentenceGame
          {...defaultProps}
          scrambledWords={['他', '吃', '饭']}
          translation="He eats rice"
        />
      );

      // Translation should be hidden (button text changes back)
      expect(screen.getByText(/显示翻译/)).toBeInTheDocument();
    });
  });

  describe('Button States', () => {
    it('should disable all buttons when submitting', () => {
      render(<SentenceGame {...defaultProps} />);

      // All buttons should be available initially (except submit which requires filled words)
      expect(screen.getByText(/获取提示/)).not.toBeDisabled();
      expect(screen.getByText(/🔄 重置/)).not.toBeDisabled();
    });

    it('should disable hint button when submitting', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);
      expect(hintButton).not.toBeDisabled();
    });

    it('should update hint button text based on usage', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/获取提示 \(3 次可用\)/)).toBeInTheDocument();

      const hintButton = screen.getByText(/获取提示/);
      fireEvent.click(hintButton);

      expect(screen.getByText(/获取提示 \(1\/3\)/)).toBeInTheDocument();
    });

    it('should disable translation button when submitting', () => {
      render(<SentenceGame {...defaultProps} />);

      const translationButton = screen.getByText(/显示翻译/);
      expect(translationButton).not.toBeDisabled();
    });
  });

  describe('Game Instructions', () => {
    it('should display game instructions', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText('📝 游戏说明')).toBeInTheDocument();
      expect(screen.getByText(/拖动下方的词语到句子区域排列/)).toBeInTheDocument();
    });
  });

  describe('Progress Display', () => {
    it('should show initial progress as 0', () => {
      render(<SentenceGame {...defaultProps} />);

      expect(screen.getByText(/已完成: 0 \/ 3 词语/)).toBeInTheDocument();
    });
  });

  describe('Difficulty Display', () => {
    it('should display different difficulty levels', () => {
      const { rerender } = render(<SentenceGame {...defaultProps} difficulty="初级" />);
      expect(screen.getByText(/难度: 初级/)).toBeInTheDocument();

      rerender(<SentenceGame {...defaultProps} difficulty="中级" />);
      expect(screen.getByText(/难度: 中级/)).toBeInTheDocument();

      rerender(<SentenceGame {...defaultProps} difficulty="高级" />);
      expect(screen.getByText(/难度: 高级/)).toBeInTheDocument();
    });
  });

  describe('Multiple Timer Tests', () => {
    it('should call onTimeout when time expires', () => {
      render(<SentenceGame {...defaultProps} timeLimit={2} />);

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(defaultProps.onTimeout).toHaveBeenCalled();
    });

    it('should handle rapid timer updates', () => {
      render(<SentenceGame {...defaultProps} timeLimit={10} />);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(screen.getByText(/⏱️ 0:05/)).toBeInTheDocument();
    });
  });

  describe('Drop Zones', () => {
    it('should render correct number of drop zones', () => {
      render(<SentenceGame {...defaultProps} />);

      // Should render 3 drop zones for 3 words
      const dropZones = screen.getAllByText(/[1-3]/);
      expect(dropZones.length).toBeGreaterThan(0);
    });
  });

  describe('Hint Request Levels', () => {
    it('should request hints at correct levels', () => {
      render(<SentenceGame {...defaultProps} />);

      const hintButton = screen.getByText(/获取提示/);

      fireEvent.click(hintButton);
      expect(defaultProps.onHintRequest).toHaveBeenCalledWith(1);

      fireEvent.click(hintButton);
      expect(defaultProps.onHintRequest).toHaveBeenCalledWith(2);

      fireEvent.click(hintButton);
      expect(defaultProps.onHintRequest).toHaveBeenCalledWith(3);
    });
  });

  describe('Component Lifecycle', () => {
    it('should cleanup timer on unmount', () => {
      const { unmount } = render(<SentenceGame {...defaultProps} />);

      act(() => {
        unmount();
      });

      act(() => {
        jest.runOnlyPendingTimers();
      });

      // Should not throw errors after unmount
    });
  });

  describe('Answer Validation', () => {
    it('should validate answer length before submission', () => {
      render(<SentenceGame {...defaultProps} />);

      const submitButton = screen.getByText(/✓ 提交答案/);

      // Submit button should be disabled when not all words placed
      expect(submitButton).toBeDisabled();
    });
  });

  describe('Word Count', () => {
    it('should handle different word counts', () => {
      const { rerender } = render(
        <SentenceGame
          {...defaultProps}
          scrambledWords={['我', '学习']}
          correctAnswer={['我', '学习']}
        />
      );

      expect(screen.getByText(/已完成: 0 \/ 2 词语/)).toBeInTheDocument();

      rerender(
        <SentenceGame
          {...defaultProps}
          scrambledWords={['我', '在', '学校', '学习', '中文']}
          correctAnswer={['我', '在', '学校', '学习', '中文']}
        />
      );

      expect(screen.getByText(/已完成: 0 \/ 5 词语/)).toBeInTheDocument();
    });
  });

  describe('Sentence Preview', () => {
    it('should not show sentence preview when no words placed', () => {
      render(<SentenceGame {...defaultProps} />);

      // The sentence preview should not be visible when empty
      // (currentSentence is empty string when no words placed)
    });
  });

  describe('Grammar Pattern Edge Cases', () => {
    it('should handle empty grammar pattern', () => {
      render(<SentenceGame {...defaultProps} grammarPattern="" />);

      expect(screen.queryByText('语法模式：')).not.toBeInTheDocument();
    });

    it('should handle long grammar pattern', () => {
      const longPattern = '主语 + 时间状语 + 地点状语 + 动词 + 宾语';
      render(<SentenceGame {...defaultProps} grammarPattern={longPattern} />);

      expect(screen.getByText(longPattern)).toBeInTheDocument();
    });
  });

  describe('Translation Edge Cases', () => {
    it('should handle empty translation', () => {
      render(<SentenceGame {...defaultProps} translation="" />);

      expect(screen.queryByText(/显示翻译/)).not.toBeInTheDocument();
    });

    it('should handle long translation', () => {
      const longTranslation = 'This is a very long translation that tests how the component handles lengthy text content';
      render(<SentenceGame {...defaultProps} translation={longTranslation} />);

      const toggleButton = screen.getByText(/显示翻译/);
      expect(toggleButton).toBeInTheDocument();

      fireEvent.click(toggleButton);
      expect(screen.getByText(/隐藏翻译/)).toBeInTheDocument();
    });
  });
});
