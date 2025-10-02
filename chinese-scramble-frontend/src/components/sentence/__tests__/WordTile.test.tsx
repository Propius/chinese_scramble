import React from 'react';
import { render, screen } from '@testing-library/react';
import { useDrag } from 'react-dnd';
import WordTile from '../WordTile';

jest.mock('react-dnd', () => ({
  DndProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  useDrag: jest.fn(),
}));

jest.mock('react-dnd-html5-backend', () => ({
  HTML5Backend: {},
}));

const mockUseDrag = useDrag as jest.MockedFunction<typeof useDrag>;

const renderWithDnd = (component: React.ReactElement) => {
  return render(component);
};

describe('WordTile', () => {
  beforeEach(() => {
    mockUseDrag.mockReturnValue([
      { isDragging: false },
      jest.fn(),
      jest.fn(),
    ] as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render with word prop', () => {
      renderWithDnd(<WordTile word="中国" index={0} />);
      expect(screen.getByText('中国')).toBeInTheDocument();
    });

    it('should render single character word', () => {
      renderWithDnd(<WordTile word="的" index={0} />);
      expect(screen.getByText('的')).toBeInTheDocument();
    });

    it('should render two character word', () => {
      renderWithDnd(<WordTile word="学习" index={0} />);
      expect(screen.getByText('学习')).toBeInTheDocument();
    });

    it('should render three character word', () => {
      renderWithDnd(<WordTile word="中国人" index={0} />);
      expect(screen.getByText('中国人')).toBeInTheDocument();
    });

    it('should render long word', () => {
      renderWithDnd(<WordTile word="国际化" index={0} />);
      expect(screen.getByText('国际化')).toBeInTheDocument();
    });

    it('should render with empty word', () => {
      const { container } = renderWithDnd(<WordTile word="" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toBeInTheDocument();
    });

    it('should render with different indices', () => {
      renderWithDnd(<WordTile word="测试" index={5} />);
      expect(screen.getByText('测试')).toBeInTheDocument();
    });
  });

  describe('Width Responsiveness', () => {
    it('should have 110px width for single character', () => {
      const { container } = renderWithDnd(<WordTile word="我" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ width: '110px' });
    });

    it('should have 130px width for two characters', () => {
      const { container } = renderWithDnd(<WordTile word="我们" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ width: '130px' });
    });

    it('should have 160px width for three characters', () => {
      const { container } = renderWithDnd(<WordTile word="学习者" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ width: '160px' });
    });

    it('should have 200px width for four or more characters', () => {
      const { container } = renderWithDnd(<WordTile word="国际化标准" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ width: '200px' });
    });

    it('should have consistent height regardless of word length', () => {
      const { container, rerender } = renderWithDnd(<WordTile word="我" index={0} />);
      let tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ height: '100px' });

      rerender(
        <WordTile word="我们都是" index={0} />
      );
      tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ height: '100px' });
    });
  });

  describe('Props - isPlaced', () => {
    it('should have yellow gradient when not placed', () => {
      const { container } = renderWithDnd(<WordTile word="未放" index={0} isPlaced={false} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
      });
    });

    it('should have purple gradient when placed', () => {
      const { container } = renderWithDnd(<WordTile word="已放" index={0} isPlaced={true} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #ddd6fe, #c4b5fd)',
      });
    });

    it('should default to not placed', () => {
      const { container } = renderWithDnd(<WordTile word="默认" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
      });
    });

    it('should change style when isPlaced changes', () => {
      const { container, rerender } = renderWithDnd(
        <WordTile word="变化" index={0} isPlaced={false} />
      );
      let tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
      });

      rerender(
        <WordTile word="变化" index={0} isPlaced={true} />
      );
      tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #ddd6fe, #c4b5fd)',
      });
    });
  });

  describe('Props - disabled', () => {
    it('should not be disabled by default', () => {
      const { container } = renderWithDnd(<WordTile word="可用" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('cursor-grab');
      expect(tile).not.toHaveClass('cursor-not-allowed');
    });

    it('should show disabled styles when disabled', () => {
      const { container } = renderWithDnd(<WordTile word="禁用" index={0} disabled={true} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('cursor-not-allowed');
      expect(tile).toHaveClass('opacity-50');
    });

    it('should not show hover effects when disabled', () => {
      const { container } = renderWithDnd(<WordTile word="禁止" index={0} disabled={true} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).not.toHaveClass('hover:scale-110');
    });

    it('should show hover effects when enabled', () => {
      const { container } = renderWithDnd(<WordTile word="可以" index={0} disabled={false} />);
      const tile = container.querySelector('.word-tile');
      expect(tile?.className).toContain('hover:scale-110');
    });
  });

  describe('Drag Functionality', () => {
    it('should call useDrag with WORD type', () => {
      renderWithDnd(<WordTile word="拖动" index={0} />);
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'WORD',
        })
      );
    });

    it('should pass word and index in drag item', () => {
      renderWithDnd(<WordTile word="词语" index={3} />);
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          item: { word: '词语', index: 3, isPlaced: false },
        })
      );
    });

    it('should pass isPlaced status in drag item', () => {
      renderWithDnd(<WordTile word="已放" index={1} isPlaced={true} />);
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          item: { word: '已放', index: 1, isPlaced: true },
        })
      );
    });

    it('should allow drag when not disabled', () => {
      renderWithDnd(<WordTile word="可拖" index={0} disabled={false} />);
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          canDrag: true,
        })
      );
    });

    it('should prevent drag when disabled', () => {
      renderWithDnd(<WordTile word="禁拖" index={0} disabled={true} />);
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          canDrag: false,
        })
      );
    });

    it('should handle isDragging state', () => {
      mockUseDrag.mockReturnValue([
        { isDragging: true },
        jest.fn(),
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordTile word="拖中" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fbcfe8, #f9a8d4)',
        transform: 'translateY(4px)',
      });
    });

    it('should apply dragging shadow', () => {
      mockUseDrag.mockReturnValue([
        { isDragging: true },
        jest.fn(),
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(<WordTile word="阴影" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({
        boxShadow: '0 4px 8px rgba(0,0,0,0.2), inset 0 -2px 4px rgba(0,0,0,0.2)',
      });
    });
  });

  describe('Styling', () => {
    it('should have border radius', () => {
      const { container } = renderWithDnd(<WordTile word="圆角" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveStyle({ borderRadius: '24px' });
    });

    it('should have gloss shine element', () => {
      const { container } = renderWithDnd(<WordTile word="光泽" index={0} />);
      const gloss = container.querySelector('.absolute.inset-2');
      expect(gloss).toBeInTheDocument();
    });

    it('should have bottom shadow element', () => {
      const { container } = renderWithDnd(<WordTile word="底影" index={0} />);
      const shadow = container.querySelector('.absolute.inset-x-2.bottom-1');
      expect(shadow).toBeInTheDocument();
    });

    it('should have proper font styling', () => {
      renderWithDnd(<WordTile word="字体" index={0} />);
      const word = screen.getByText('字体');
      expect(word).toHaveClass('font-black');
      expect(word).toHaveClass('select-none');
      expect(word).toHaveClass('text-gray-800');
    });

    it('should have whitespace-nowrap for text', () => {
      renderWithDnd(<WordTile word="不换行" index={0} />);
      const word = screen.getByText('不换行');
      expect(word).toHaveClass('whitespace-nowrap');
    });

    it('should center content', () => {
      const { container } = renderWithDnd(<WordTile word="居中" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('items-center');
      expect(tile).toHaveClass('justify-center');
    });
  });

  describe('Edge Cases', () => {
    it('should handle negative index', () => {
      renderWithDnd(<WordTile word="负数" index={-1} />);
      expect(screen.getByText('负数')).toBeInTheDocument();
    });

    it('should handle zero index', () => {
      renderWithDnd(<WordTile word="零" index={0} />);
      expect(screen.getByText('零')).toBeInTheDocument();
    });

    it('should handle very large index', () => {
      renderWithDnd(<WordTile word="大数" index={9999} />);
      expect(screen.getByText('大数')).toBeInTheDocument();
    });

    it('should handle very long words', () => {
      const longWord = '这是一个非常长的词语';
      renderWithDnd(<WordTile word={longWord} index={0} />);
      expect(screen.getByText(longWord)).toBeInTheDocument();
    });

    it('should handle special characters', () => {
      renderWithDnd(<WordTile word="？！" index={0} />);
      expect(screen.getByText('？！')).toBeInTheDocument();
    });

    it('should handle numbers', () => {
      renderWithDnd(<WordTile word="123" index={0} />);
      expect(screen.getByText('123')).toBeInTheDocument();
    });

    it('should handle mixed content', () => {
      renderWithDnd(<WordTile word="ABC中文123" index={0} />);
      expect(screen.getByText('ABC中文123')).toBeInTheDocument();
    });

    it('should handle rapid prop changes', () => {
      const { rerender } = renderWithDnd(
        <WordTile word="第一" index={0} disabled={false} isPlaced={false} />
      );

      rerender(
        <WordTile word="第二" index={1} disabled={true} isPlaced={true} />
      );

      expect(screen.getByText('第二')).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    it('should have word-tile class', () => {
      const { container } = renderWithDnd(<WordTile word="类名" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toBeInTheDocument();
    });

    it('should have relative positioning', () => {
      const { container } = renderWithDnd(<WordTile word="相对" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('relative');
    });

    it('should have inline-flex display', () => {
      const { container } = renderWithDnd(<WordTile word="显示" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('inline-flex');
    });

    it('should have transition classes', () => {
      const { container } = renderWithDnd(<WordTile word="过渡" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('transition-all');
      expect(tile).toHaveClass('duration-200');
    });

    it('should have margin classes', () => {
      const { container } = renderWithDnd(<WordTile word="边距" index={0} />);
      const tile = container.querySelector('.word-tile');
      expect(tile).toHaveClass('m-4');
    });
  });

  describe('Multiple Tiles', () => {
    it('should render multiple tiles independently', () => {
      const { container } = render(
        <>
          <WordTile word="第一" index={0} />
          <WordTile word="第二" index={1} />
          <WordTile word="第三" index={2} />
        </>
      );

      expect(screen.getByText('第一')).toBeInTheDocument();
      expect(screen.getByText('第二')).toBeInTheDocument();
      expect(screen.getByText('第三')).toBeInTheDocument();
    });

    it('should handle tiles with same word', () => {
      render(
        <>
          <WordTile word="相同" index={0} />
          <WordTile word="相同" index={1} />
        </>
      );

      const tiles = screen.getAllByText('相同');
      expect(tiles).toHaveLength(2);
    });

    it('should maintain independent states', () => {
      const { container } = render(
        <>
          <WordTile word="独立" index={0} isPlaced={false} />
          <WordTile word="独立" index={1} isPlaced={true} />
        </>
      );

      const tiles = container.querySelectorAll('.word-tile');
      expect(tiles).toHaveLength(2);
    });
  });
});
