import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import Confetti from '../Confetti';

describe('Confetti', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('should render without crashing when active is false', () => {
    const { container } = render(<Confetti active={false} />);
    expect(container.firstChild).toBeNull();
  });

  it('should render confetti pieces when active is true', () => {
    const { container } = render(<Confetti active={true} />);

    // Should render 50 confetti pieces (as per component implementation)
    const confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);
  });

  it('should render confetti with correct styling', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');
    const firstPiece = confettiPieces[0];

    // Check fixed positioning
    expect(firstPiece).toHaveStyle({
      position: 'fixed',
      top: '-20px',
      zIndex: '9999',
      pointerEvents: 'none'
    });
  });

  it('should render confetti with random colors from predefined palette', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');

    // Check that all pieces have backgroundColor set (will be in RGB format)
    confettiPieces.forEach((piece) => {
      const bgColor = (piece as HTMLElement).style.backgroundColor;
      // Just check that backgroundColor is set (browsers convert hex to rgb)
      expect(bgColor).toBeTruthy();
      expect(bgColor).toMatch(/rgb/);
    });
  });

  it('should render confetti with random left positions', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');
    const leftPositions = new Set<string>();

    confettiPieces.forEach((piece) => {
      const left = (piece as HTMLElement).style.left;
      leftPositions.add(left);
    });

    // Should have multiple different positions (randomized)
    expect(leftPositions.size).toBeGreaterThan(1);
  });

  it('should render confetti with mixed shapes (circles and squares)', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');
    let circleCount = 0;
    let squareCount = 0;

    confettiPieces.forEach((piece) => {
      const borderRadius = (piece as HTMLElement).style.borderRadius;
      if (borderRadius === '50%') {
        circleCount++;
      } else if (borderRadius === '0' || borderRadius === '0px') {
        squareCount++;
      }
    });

    // Should have both circles and squares
    expect(circleCount).toBeGreaterThan(0);
    expect(squareCount).toBeGreaterThan(0);
  });

  it('should render confetti with animation styles', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');
    const animations = ['confetti-fall-1', 'confetti-fall-2', 'confetti-fall-3'];

    confettiPieces.forEach((piece) => {
      const animation = (piece as HTMLElement).style.animation;
      const hasValidAnimation = animations.some(anim => animation.includes(anim));
      expect(hasValidAnimation).toBeTruthy();
    });
  });

  it('should clear confetti after default duration (3000ms)', async () => {
    const { container } = render(<Confetti active={true} />);

    // Initially should have confetti
    let confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);

    // Fast-forward time by 3000ms (default duration)
    jest.advanceTimersByTime(3000);

    // Wait for state update
    await waitFor(() => {
      const pieces = container.querySelectorAll('div');
      expect(pieces.length).toBe(0);
    });
  });

  it('should clear confetti after custom duration', async () => {
    const customDuration = 5000;
    const { container } = render(<Confetti active={true} duration={customDuration} />);

    // Initially should have confetti
    let confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);

    // Fast-forward time by custom duration
    jest.advanceTimersByTime(customDuration);

    // Wait for state update
    await waitFor(() => {
      const pieces = container.querySelectorAll('div');
      expect(pieces.length).toBe(0);
    });
  });

  it('should not clear confetti before duration expires', () => {
    const { container } = render(<Confetti active={true} duration={5000} />);

    // Initially should have confetti
    let confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);

    // Fast-forward time by less than duration
    jest.advanceTimersByTime(2000);

    // Should still have confetti
    confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);
  });

  it('should generate new confetti when active changes from false to true', () => {
    const { container, rerender } = render(<Confetti active={false} />);

    // Initially no confetti
    let confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(0);

    // Activate confetti
    rerender(<Confetti active={true} />);

    // Should now have confetti
    confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);
  });

  it('should regenerate confetti when active prop toggles', async () => {
    const { container, rerender } = render(<Confetti active={true} duration={1000} />);

    // Initially should have confetti
    let confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);

    // Wait for duration to expire
    jest.advanceTimersByTime(1000);

    await waitFor(() => {
      const pieces = container.querySelectorAll('div');
      expect(pieces.length).toBe(0);
    });

    // Reactivate
    rerender(<Confetti active={false} />);
    rerender(<Confetti active={true} duration={1000} />);

    // Should have new confetti
    confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);
  });

  it('should render confetti with varying sizes', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');
    const sizes = new Set<string>();

    confettiPieces.forEach((piece) => {
      const width = (piece as HTMLElement).style.width;
      sizes.add(width);
    });

    // Should have multiple different sizes (randomized)
    expect(sizes.size).toBeGreaterThan(1);
  });

  it('should have consistent width and height for each piece', () => {
    const { container } = render(<Confetti active={true} />);

    const confettiPieces = container.querySelectorAll('div');

    confettiPieces.forEach((piece) => {
      const width = (piece as HTMLElement).style.width;
      const height = (piece as HTMLElement).style.height;
      // Width and height should be the same for each piece
      expect(width).toBe(height);
    });
  });

  it('should render empty fragment when not active', () => {
    const { container } = render(<Confetti active={false} />);
    expect(container.firstChild).toBeNull();
  });

  it('should handle rapid active state changes', async () => {
    const { container, rerender } = render(<Confetti active={false} />);

    // Toggle active state rapidly
    rerender(<Confetti active={true} />);
    rerender(<Confetti active={false} />);
    rerender(<Confetti active={true} />);

    // Should have confetti from the last active=true
    const confettiPieces = container.querySelectorAll('div');
    expect(confettiPieces.length).toBe(50);
  });
});
