import React from 'react';
import { render, screen } from '@testing-library/react';
import { useDrag } from 'react-dnd';
import CharacterTile from '../CharacterTile';

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

describe('CharacterTile', () => {
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
    it('should render with character prop', () => {
      renderWithDnd(
        <CharacterTile character="中" index={0} />
      );
      expect(screen.getByText('中')).toBeInTheDocument();
    });

    it('should render with different characters', () => {
      const { rerender } = renderWithDnd(
        <CharacterTile character="国" index={0} />
      );
      expect(screen.getByText('国')).toBeInTheDocument();

      rerender(
        <CharacterTile character="语" index={0} />
      );
      expect(screen.getByText('语')).toBeInTheDocument();
    });

    it('should render with correct index', () => {
      renderWithDnd(
        <CharacterTile character="文" index={5} />
      );
      expect(screen.getByText('文')).toBeInTheDocument();
    });

    it('should render with empty character', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toBeInTheDocument();
    });

    it('should render with special characters', () => {
      renderWithDnd(
        <CharacterTile character="？" index={0} />
      );
      expect(screen.getByText('？')).toBeInTheDocument();
    });
  });

  describe('Props - isPlaced', () => {
    it('should render with isPlaced false by default', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="测" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
      });
    });

    it('should render with isPlaced true', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="试" index={0} isPlaced={true} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #bfdbfe, #93c5fd)',
      });
    });

    it('should change style when isPlaced changes', () => {
      const { container, rerender } = renderWithDnd(
        <CharacterTile character="变" index={0} isPlaced={false} />
      );
      let tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
      });

      rerender(
        <CharacterTile character="变" index={0} isPlaced={true} />
      );
      tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #bfdbfe, #93c5fd)',
      });
    });
  });

  describe('Props - disabled', () => {
    it('should render with disabled false by default', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="用" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveClass('cursor-grab');
      expect(tile).not.toHaveClass('cursor-not-allowed');
    });

    it('should render with disabled true', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="禁" index={0} disabled={true} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveClass('cursor-not-allowed');
      expect(tile).toHaveClass('opacity-50');
    });

    it('should not show hover effects when disabled', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="不" index={0} disabled={true} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).not.toHaveClass('hover:scale-110');
    });

    it('should show hover effects when not disabled', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="可" index={0} disabled={false} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile?.className).toContain('hover:scale-110');
    });
  });

  describe('Drag functionality', () => {
    it('should call useDrag with correct type', () => {
      renderWithDnd(
        <CharacterTile character="拖" index={0} />
      );
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'CHARACTER',
        })
      );
    });

    it('should pass character in drag item', () => {
      renderWithDnd(
        <CharacterTile character="拽" index={2} />
      );
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          item: { character: '拽', index: 2, isPlaced: false },
        })
      );
    });

    it('should pass isPlaced in drag item', () => {
      renderWithDnd(
        <CharacterTile character="移" index={1} isPlaced={true} />
      );
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          item: { character: '移', index: 1, isPlaced: true },
        })
      );
    });

    it('should allow drag when not disabled', () => {
      renderWithDnd(
        <CharacterTile character="可" index={0} disabled={false} />
      );
      expect(mockUseDrag).toHaveBeenCalledWith(
        expect.objectContaining({
          canDrag: true,
        })
      );
    });

    it('should prevent drag when disabled', () => {
      renderWithDnd(
        <CharacterTile character="禁" index={0} disabled={true} />
      );
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

      const { container } = renderWithDnd(
        <CharacterTile character="拖" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        background: 'linear-gradient(145deg, #fde68a, #fcd34d)',
        transform: 'translateY(4px)',
      });
    });

    it('should apply dragging styles', () => {
      mockUseDrag.mockReturnValue([
        { isDragging: true },
        jest.fn(),
        jest.fn(),
      ] as any);

      const { container } = renderWithDnd(
        <CharacterTile character="动" index={0} isPlaced={false} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        transform: 'translateY(4px)',
      });
    });
  });

  describe('Styling', () => {
    it('should have correct dimensions', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="大" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        width: '110px',
        height: '110px',
      });
    });

    it('should have border radius', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="圆" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveStyle({
        borderRadius: '24px',
      });
    });

    it('should render gloss shine element', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="亮" index={0} />
      );
      const gloss = container.querySelector('.absolute.inset-2');
      expect(gloss).toBeInTheDocument();
    });

    it('should render bottom shadow element', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="影" index={0} />
      );
      const shadow = container.querySelector('.absolute.inset-x-2.bottom-1');
      expect(shadow).toBeInTheDocument();
    });

    it('should have correct font styling for character', () => {
      renderWithDnd(
        <CharacterTile character="字" index={0} />
      );
      const char = screen.getByText('字');
      expect(char).toHaveClass('font-black');
      expect(char).toHaveClass('select-none');
      expect(char).toHaveClass('text-gray-800');
    });
  });

  describe('Edge cases', () => {
    it('should handle negative index', () => {
      renderWithDnd(
        <CharacterTile character="负" index={-1} />
      );
      expect(screen.getByText('负')).toBeInTheDocument();
    });

    it('should handle very large index', () => {
      renderWithDnd(
        <CharacterTile character="大" index={999} />
      );
      expect(screen.getByText('大')).toBeInTheDocument();
    });

    it('should handle zero index', () => {
      renderWithDnd(
        <CharacterTile character="零" index={0} />
      );
      expect(screen.getByText('零')).toBeInTheDocument();
    });

    it('should handle multiple tiles with same character', () => {
      const { container } = render(
        <>
          <CharacterTile character="同" index={0} />
          <CharacterTile character="同" index={1} />
        </>
      );
      const tiles = screen.getAllByText('同');
      expect(tiles).toHaveLength(2);
    });

    it('should handle rapid prop changes', () => {
      const { rerender } = renderWithDnd(
        <CharacterTile character="快" index={0} disabled={false} isPlaced={false} />
      );

      rerender(
        <CharacterTile character="速" index={1} disabled={true} isPlaced={true} />
      );

      expect(screen.getByText('速')).toBeInTheDocument();
    });
  });

  describe('Component structure', () => {
    it('should have character-tile class', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="类" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toBeInTheDocument();
    });

    it('should have relative positioning', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="位" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveClass('relative');
    });

    it('should have inline-flex display', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="显" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveClass('inline-flex');
    });

    it('should center content', () => {
      const { container } = renderWithDnd(
        <CharacterTile character="中" index={0} />
      );
      const tile = container.querySelector('.character-tile');
      expect(tile).toHaveClass('items-center');
      expect(tile).toHaveClass('justify-center');
    });
  });
});
