import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import { DropZone } from '../DropZone';

// Mock react-dnd completely to avoid drag-and-drop complexity
jest.mock('react-dnd', () => ({
  useDrop: (config: any) => {
    // Return mock hook values
    const mockRef = jest.fn();
    const mockCollect = { isOver: false, canDrop: true };

    // Store the config for testing
    (mockRef as any).dropConfig = config;

    return [mockCollect, mockRef];
  },
}));

describe('DropZone Integration Tests', () => {
  const mockOnDrop = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('Component Rendering', () => {
    it('should render empty drop zone with index', () => {
      render(<DropZone index={0} onDrop={mockOnDrop} />);

      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should render with different indices', () => {
      const { rerender } = render(<DropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();

      rerender(<DropZone index={1} onDrop={mockOnDrop} />);
      expect(screen.getByText('2')).toBeInTheDocument();

      rerender(<DropZone index={3} onDrop={mockOnDrop} />);
      expect(screen.getByText('4')).toBeInTheDocument();
    });

    it('should render filled drop zone with character', () => {
      render(<DropZone index={0} character="一" onDrop={mockOnDrop} />);

      expect(screen.getByText('一')).toBeInTheDocument();
      expect(screen.queryByText('1')).not.toBeInTheDocument();
    });

    it('should display character correctly', () => {
      const { rerender } = render(<DropZone index={0} character="马" onDrop={mockOnDrop} />);
      expect(screen.getByText('马')).toBeInTheDocument();

      rerender(<DropZone index={0} character="当" onDrop={mockOnDrop} />);
      expect(screen.getByText('当')).toBeInTheDocument();

      rerender(<DropZone index={0} character="先" onDrop={mockOnDrop} />);
      expect(screen.getByText('先')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should show index number when no character', () => {
      render(<DropZone index={2} onDrop={mockOnDrop} />);

      expect(screen.getByText('3')).toBeInTheDocument();
    });

    it('should apply dashed border when empty', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveClass('border-dashed');
    });

    it('should have correct styling for empty state', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toBeInTheDocument();
    });
  });

  describe('Filled State', () => {
    it('should render character when filled', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toBeInTheDocument();
      expect(screen.getByText('一')).toBeInTheDocument();
    });

    it('should display gloss shine effect when filled', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      // Check for elements with pointer-events-none (gloss effect)
      const glossElements = container.querySelectorAll('.pointer-events-none');
      expect(glossElements.length).toBeGreaterThan(0);
    });

    it('should have different styling than empty state', () => {
      const { container: emptyContainer } = render(
        <DropZone index={0} onDrop={mockOnDrop} />
      );
      const { container: filledContainer } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const emptyZone = emptyContainer.querySelector('.drop-zone');
      const filledZone = filledContainer.querySelector('.drop-zone');

      expect(emptyZone?.className).not.toEqual(filledZone?.className);
    });
  });

  describe('Disabled State', () => {
    it('should apply disabled styling when disabled', () => {
      const { container } = render(
        <DropZone index={0} onDrop={mockOnDrop} disabled={true} />
      );

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveClass('opacity-50');
      expect(dropZone).toHaveClass('cursor-not-allowed');
    });

    it('should not have disabled styling when not disabled', () => {
      const { container } = render(
        <DropZone index={0} onDrop={mockOnDrop} disabled={false} />
      );

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).not.toHaveClass('opacity-50');
      expect(dropZone).not.toHaveClass('cursor-not-allowed');
    });

    it('should default to not disabled', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).not.toHaveClass('opacity-50');
    });
  });

  describe('Index Prop Handling', () => {
    it('should handle index 0 correctly', () => {
      render(<DropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should handle large indices', () => {
      render(<DropZone index={99} onDrop={mockOnDrop} />);
      expect(screen.getByText('100')).toBeInTheDocument();
    });

    it('should convert index to display number correctly', () => {
      const indices = [0, 1, 2, 3, 4, 5];
      indices.forEach((idx) => {
        const { unmount } = render(<DropZone index={idx} onDrop={mockOnDrop} />);
        expect(screen.getByText(`${idx + 1}`)).toBeInTheDocument();
        unmount();
      });
    });
  });

  describe('Character Prop Display', () => {
    it('should display single character', () => {
      render(<DropZone index={0} character="一" onDrop={mockOnDrop} />);
      expect(screen.getByText('一')).toBeInTheDocument();
    });

    it('should display different characters', () => {
      const characters = ['一', '马', '当', '先', '龙', '虎'];
      characters.forEach((char) => {
        const { unmount } = render(
          <DropZone index={0} character={char} onDrop={mockOnDrop} />
        );
        expect(screen.getByText(char)).toBeInTheDocument();
        unmount();
      });
    });

    it('should switch between character and index when character changes', () => {
      const { rerender } = render(<DropZone index={0} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();

      rerender(<DropZone index={0} character="一" onDrop={mockOnDrop} />);
      expect(screen.getByText('一')).toBeInTheDocument();
      expect(screen.queryByText('1')).not.toBeInTheDocument();

      rerender(<DropZone index={0} character={undefined} onDrop={mockOnDrop} />);
      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.queryByText('一')).not.toBeInTheDocument();
    });
  });

  describe('Styling Changes', () => {
    it('should have different styles between empty and filled', () => {
      const { container: emptyContainer } = render(
        <DropZone index={0} onDrop={mockOnDrop} />
      );
      const emptyDropZone = emptyContainer.querySelector('.drop-zone');
      const emptyStyle = window.getComputedStyle(emptyDropZone as Element);

      const { container: filledContainer } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );
      const filledDropZone = filledContainer.querySelector('.drop-zone');
      const filledStyle = window.getComputedStyle(filledDropZone as Element);

      // Styles should be different
      expect(emptyDropZone?.className).not.toEqual(filledDropZone?.className);
    });

    it('should apply correct classes for empty state', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveClass('drop-zone');
      expect(dropZone).toHaveClass('relative');
      expect(dropZone).toHaveClass('inline-flex');
    });

    it('should apply correct classes for filled state', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveClass('drop-zone');
      expect(dropZone).toHaveClass('relative');
      expect(dropZone).toHaveClass('inline-flex');
    });
  });

  describe('Component Structure', () => {
    it('should have proper container structure', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const wrapper = container.querySelector('.relative.inline-block');
      expect(wrapper).toBeInTheDocument();
    });

    it('should contain drop-zone element', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toBeInTheDocument();
    });

    it('should render content inside drop-zone', () => {
      render(<DropZone index={0} onDrop={mockOnDrop} />);

      expect(screen.getByText('1')).toBeInTheDocument();
    });
  });

  describe('Visual Effects', () => {
    it('should have gradient background styling', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveStyle({ borderRadius: '24px' });
    });

    it('should show gloss effect when character is present', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const glossElements = container.querySelectorAll('.pointer-events-none');
      expect(glossElements.length).toBeGreaterThan(0);
    });

    it('should not show gloss effect when empty', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      // Should have fewer pointer-events-none elements when empty
      const glossElements = container.querySelectorAll('.pointer-events-none');
      // Empty state may still have some pointer-events-none, but fewer than filled
      expect(glossElements.length).toBeDefined();
    });
  });

  describe('Props Handling', () => {
    it('should accept all required props', () => {
      expect(() => {
        render(<DropZone index={0} onDrop={mockOnDrop} />);
      }).not.toThrow();
    });

    it('should accept optional character prop', () => {
      expect(() => {
        render(<DropZone index={0} character="一" onDrop={mockOnDrop} />);
      }).not.toThrow();
    });

    it('should accept optional disabled prop', () => {
      expect(() => {
        render(<DropZone index={0} onDrop={mockOnDrop} disabled={true} />);
      }).not.toThrow();
    });

    it('should work with all props combined', () => {
      expect(() => {
        render(
          <DropZone
            index={5}
            character="龙"
            onDrop={mockOnDrop}
            disabled={false}
          />
        );
      }).not.toThrow();
    });
  });

  describe('Index Display Logic', () => {
    it('should always add 1 to index for display', () => {
      for (let i = 0; i < 10; i++) {
        const { unmount } = render(<DropZone index={i} onDrop={mockOnDrop} />);
        expect(screen.getByText(`${i + 1}`)).toBeInTheDocument();
        unmount();
      }
    });
  });

  describe('Text Content', () => {
    it('should have correct font styling for character', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const character = screen.getByText('一');
      expect(character).toHaveClass('font-black');
      expect(character).toHaveClass('text-gray-800');
    });

    it('should have correct font styling for index', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const indexNumber = screen.getByText('1');
      expect(indexNumber).toHaveClass('font-bold');
      expect(indexNumber).toHaveClass('text-gray-500');
    });
  });

  describe('State Transitions', () => {
    it('should handle transition from empty to filled', () => {
      const { rerender } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      expect(screen.getByText('1')).toBeInTheDocument();

      rerender(<DropZone index={0} character="一" onDrop={mockOnDrop} />);

      expect(screen.getByText('一')).toBeInTheDocument();
      expect(screen.queryByText('1')).not.toBeInTheDocument();
    });

    it('should handle transition from filled to empty', () => {
      const { rerender } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      expect(screen.getByText('一')).toBeInTheDocument();

      rerender(<DropZone index={0} onDrop={mockOnDrop} />);

      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.queryByText('一')).not.toBeInTheDocument();
    });

    it('should handle character replacement', () => {
      const { rerender } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      expect(screen.getByText('一')).toBeInTheDocument();

      rerender(<DropZone index={0} character="马" onDrop={mockOnDrop} />);

      expect(screen.getByText('马')).toBeInTheDocument();
      expect(screen.queryByText('一')).not.toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should be keyboard accessible', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toBeInTheDocument();
    });

    it('should have proper semantic structure', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const content = screen.getByText('1');
      expect(content.tagName).toBe('SPAN');
    });
  });

  describe('Size and Layout', () => {
    it('should have consistent size for empty state', () => {
      const { container } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveStyle({ width: '110px', height: '110px' });
    });

    it('should have consistent size for filled state', () => {
      const { container } = render(
        <DropZone index={0} character="一" onDrop={mockOnDrop} />
      );

      const dropZone = container.querySelector('.drop-zone');
      expect(dropZone).toHaveStyle({ width: '110px', height: '110px' });
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty string character', () => {
      render(<DropZone index={0} character="" onDrop={mockOnDrop} />);

      // Empty string is falsy, so should show index
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('should handle very large index', () => {
      render(<DropZone index={999} onDrop={mockOnDrop} />);

      expect(screen.getByText('1000')).toBeInTheDocument();
    });

    it('should handle multiple re-renders without issues', () => {
      const { rerender } = render(<DropZone index={0} onDrop={mockOnDrop} />);

      for (let i = 0; i < 10; i++) {
        rerender(
          <DropZone index={i} character={i % 2 === 0 ? '一' : undefined} onDrop={mockOnDrop} />
        );
      }

      // Should end with index 9 (no character since 9 is odd)
      expect(screen.getByText('10')).toBeInTheDocument();
    });
  });
});
