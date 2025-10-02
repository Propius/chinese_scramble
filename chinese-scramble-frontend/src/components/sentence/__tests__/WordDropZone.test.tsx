import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { useDrop } from 'react-dnd';
import WordDropZone from '../WordDropZone';

jest.mock('react-dnd', () => ({
  DndProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  useDrop: jest.fn(),
}));

jest.mock('react-dnd-html5-backend', () => ({
  HTML5Backend: {},
}));

const mockUseDrop = useDrop as jest.MockedFunction<typeof useDrop>;

const renderWithDnd = (component: React.ReactElement) => {
  return render(component);
};

describe('WordDropZone', () => {
  const mockOnDrop = jest.fn();

  beforeEach(() => {
    jest.useFakeTimers();
    mockUseDrop.mockReturnValue([
      { isOver: false, canDrop: true },
      jest.fn(),
    ] as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('Empty State Rendering', () => {
    it('should render empty drop zone', () => {
      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should display index number when empty', () => {
      renderWithDnd(<WordDropZone index={5} onDrop={mockOnDrop} />);
      expect(screen.getByText('6')).toBeInTheDocument();
    });

    it('should display first index as 1', () => {
      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should have dashed border when empty', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('border-4');
      expect(zone).toHaveClass('border-dashed');
      expect(zone).toHaveClass('border-amber-300');
    });

    it('should have yellow gradient when empty', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({
        background: 'linear-gradient(145deg, #fef3c7, #fde68a)',
      });
    });
  });

  describe('Filled State Rendering', () => {
    it('should render with word', () => {
      renderWithDnd(<WordDropZone index={0} word="中国" onDrop={mockOnDrop} />);
      expect(screen.getByText('中国')).toBeInTheDocument();
    });

    it('should render single character word', () => {
      renderWithDnd(<WordDropZone index={0} word="我" onDrop={mockOnDrop} />);
      expect(screen.getByText('我')).toBeInTheDocument();
    });

    it('should render multi-character word', () => {
      renderWithDnd(<WordDropZone index={0} word="学习者" onDrop={mockOnDrop} />);
      expect(screen.getByText('学习者')).toBeInTheDocument();
    });

    it('should have purple gradient when filled', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="测试" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({
        background: 'linear-gradient(145deg, #ddd6fe, #c4b5fd)',
      });
    });

    it('should not have dashed border when filled', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="已填" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone?.className).not.toContain('border-dashed');
    });
  });

  describe('Width Responsiveness', () => {
    it('should have 130px width when empty', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ width: '130px' });
    });

    it('should have 110px width for single character', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="我" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ width: '110px' });
    });

    it('should have 130px width for two characters', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="我们" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ width: '130px' });
    });

    it('should have 160px width for three characters', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="中国人" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ width: '160px' });
    });

    it('should have 200px width for four or more characters', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="国际化标准" onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ width: '200px' });
    });

    it('should have consistent height', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ height: '110px' });
    });
  });

  describe('Drop Functionality', () => {
    it('should call useDrop with WORD type', () => {
      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      expect(mockUseDrop).toHaveBeenCalledWith(
        expect.objectContaining({
          accept: 'WORD',
        })
      );
    });

    it('should allow drop when not disabled', () => {
      let canDropFn: (() => boolean) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        canDropFn = config.canDrop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} disabled={false} />);
      expect(canDropFn!()).toBe(true);
    });

    it('should prevent drop when disabled', () => {
      let canDropFn: (() => boolean) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        canDropFn = config.canDrop;
        return [{ isOver: false, canDrop: false }, jest.fn()] as any;
      });

      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} disabled={true} />);
      expect(canDropFn!()).toBe(false);
    });

    it('should call onDrop with correct parameters', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      renderWithDnd(<WordDropZone index={2} onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '测试', index: 0, isPlaced: false });
      });

      expect(mockOnDrop).toHaveBeenCalledWith('测试', 0, 2, false);
    });

    it('should handle drop from placed area', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      renderWithDnd(<WordDropZone index={1} onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '已放', index: 3, isPlaced: true });
      });

      expect(mockOnDrop).toHaveBeenCalledWith('已放', 3, 1, true);
    });
  });

  describe('Hover States', () => {
    it('should show active state when hovering with valid drop', () => {
      mockUseDrop.mockReturnValue([
        { isOver: true, canDrop: true },
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({
        background: 'linear-gradient(145deg, #a7f3d0, #6ee7b7)',
      });
    });

    it('should show border when can drop but not hovering', () => {
      mockUseDrop.mockReturnValue([
        { isOver: false, canDrop: true },
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('border-4');
      expect(zone).toHaveClass('border-dashed');
      expect(zone).toHaveClass('border-blue-400');
    });

    it('should have scale effect when active', () => {
      mockUseDrop.mockReturnValue([
        { isOver: true, canDrop: true },
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('scale-110');
      expect(zone).toHaveClass('animate-pulse');
    });

    it('should show green border when active', () => {
      mockUseDrop.mockReturnValue([
        { isOver: true, canDrop: true },
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('border-4');
      expect(zone).toHaveClass('border-green-400');
    });
  });

  describe('Disabled State', () => {
    it('should show disabled styles when disabled', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} disabled={true} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('opacity-50');
      expect(zone).toHaveClass('cursor-not-allowed');
    });

    it('should not show active styles when disabled and hovering', () => {
      mockUseDrop.mockReturnValue([
        { isOver: true, canDrop: false },
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} disabled={true} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).not.toHaveClass('scale-110');
    });
  });

  describe('Drop Animation', () => {
    it('should trigger animation on drop', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      const { container } = renderWithDnd(<WordDropZone index={0} word="词" onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '词', index: 0, isPlaced: false });
      });

      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({
        transform: 'scale(1.1) rotate(3deg)',
      });
    });

    it('should clear animation after timeout', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      const { container } = renderWithDnd(<WordDropZone index={0} word="测" onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '测', index: 0, isPlaced: false });
      });

      act(() => {
        jest.advanceTimersByTime(800);
      });

      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({
        transform: 'scale(1) rotate(0deg)',
      });
    });

    it('should show ripple effect on drop', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '波纹', index: 0, isPlaced: false });
      });

      const ripple = container.querySelector('.absolute.inset-0');
      expect(ripple).toBeInTheDocument();
    });

    it('should clear ripple after timeout', () => {
      let dropFn: ((item: any) => void) | undefined;
      mockUseDrop.mockImplementation((config: any) => {
        dropFn = config.drop;
        return [{ isOver: false, canDrop: true }, jest.fn()] as any;
      });

      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);

      act(() => {
        dropFn!({ word: '消失', index: 0, isPlaced: false });
        jest.advanceTimersByTime(1000);
      });

      const ripple = container.querySelector('.absolute.inset-0');
      expect(ripple).not.toBeInTheDocument();
    });
  });

  describe('Styling Elements', () => {
    it('should have gloss shine when filled', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="光泽" onDrop={mockOnDrop} />);
      const gloss = container.querySelector('.absolute.inset-2');
      expect(gloss).toBeInTheDocument();
    });

    it('should not have gloss when empty', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const gloss = container.querySelector('.absolute.inset-2');
      expect(gloss).not.toBeInTheDocument();
    });

    it('should have bottom shadow when filled', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} word="阴影" onDrop={mockOnDrop} />);
      const shadow = container.querySelector('.absolute.inset-x-2.bottom-1');
      expect(shadow).toBeInTheDocument();
    });

    it('should not have bottom shadow when empty', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const shadow = container.querySelector('.absolute.inset-x-2.bottom-1');
      expect(shadow).not.toBeInTheDocument();
    });

    it('should have border radius', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveStyle({ borderRadius: '24px' });
    });
  });

  describe('Text Styling', () => {
    it('should have gray text color when filled', () => {
      renderWithDnd(<WordDropZone index={0} word="文字" onDrop={mockOnDrop} />);
      const text = screen.getByText('文字');
      expect(text).toHaveClass('text-gray-800');
    });

    it('should have gray text color when empty', () => {
      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const text = screen.getByText('1');
      expect(text).toHaveClass('text-gray-500');
    });

    it('should have font-black when filled', () => {
      renderWithDnd(<WordDropZone index={0} word="粗体" onDrop={mockOnDrop} />);
      const text = screen.getByText('粗体');
      expect(text).toHaveClass('font-black');
    });

    it('should have whitespace-nowrap', () => {
      renderWithDnd(<WordDropZone index={0} word="不换行" onDrop={mockOnDrop} />);
      const text = screen.getByText('不换行');
      expect(text).toHaveClass('whitespace-nowrap');
    });

    it('should be non-selectable', () => {
      renderWithDnd(<WordDropZone index={0} word="不可选" onDrop={mockOnDrop} />);
      const text = screen.getByText('不可选');
      expect(text).toHaveClass('select-none');
    });
  });

  describe('Edge Cases', () => {
    it('should handle zero index', () => {
      renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should handle large index', () => {
      renderWithDnd(<WordDropZone index={99} onDrop={mockOnDrop} />);
      expect(screen.getByText('100')).toBeInTheDocument();
    });

    it('should handle empty string word', () => {
      renderWithDnd(<WordDropZone index={0} word="" onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should handle special characters in word', () => {
      renderWithDnd(<WordDropZone index={0} word="？！" onDrop={mockOnDrop} />);
      expect(screen.getByText('？！')).toBeInTheDocument();
    });

    it('should handle very long words', () => {
      const longWord = '这是一个非常长的词语';
      renderWithDnd(<WordDropZone index={0} word={longWord} onDrop={mockOnDrop} />);
      expect(screen.getByText(longWord)).toBeInTheDocument();
    });

    it('should handle rapid state changes', () => {
      const { rerender } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);

      rerender(
        <WordDropZone index={0} word="变化" onDrop={mockOnDrop} />
      );

      expect(screen.getByText('变化')).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    it('should have word-drop-zone class', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toBeInTheDocument();
    });

    it('should have relative positioning', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('relative');
    });

    it('should have inline-flex display', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('inline-flex');
    });

    it('should center content', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('items-center');
      expect(zone).toHaveClass('justify-center');
    });

    it('should have margin classes', () => {
      const { container } = renderWithDnd(<WordDropZone index={0} onDrop={mockOnDrop} />);
      const zone = container.querySelector('.word-drop-zone');
      expect(zone).toHaveClass('m-4');
    });
  });
});
