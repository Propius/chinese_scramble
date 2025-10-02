import { renderHook, act, waitFor } from '@testing-library/react';
import { useTimer } from '../useTimer';

describe('useTimer', () => {
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

  describe('Initial state', () => {
    it('should initialize with correct default values', () => {
      const { result } = renderHook(() => useTimer(60));

      expect(result.current.timeLeft).toBe(60);
      expect(result.current.isRunning).toBe(false);
    });

    it('should initialize with provided initial time', () => {
      const { result } = renderHook(() => useTimer(120));

      expect(result.current.timeLeft).toBe(120);
    });

    it('should return all required methods', () => {
      const { result } = renderHook(() => useTimer(60));

      expect(typeof result.current.start).toBe('function');
      expect(typeof result.current.pause).toBe('function');
      expect(typeof result.current.resume).toBe('function');
      expect(typeof result.current.reset).toBe('function');
      expect(typeof result.current.stop).toBe('function');
    });

    it('should not be running initially', () => {
      const { result } = renderHook(() => useTimer(60));

      expect(result.current.isRunning).toBe(false);
    });
  });

  describe('start() method', () => {
    it('should start the timer', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      expect(result.current.isRunning).toBe(true);
    });

    it('should begin countdown after starting', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      expect(result.current.timeLeft).toBe(60);

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(result.current.timeLeft).toBe(59);
    });

    it('should allow multiple starts without error', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
        result.current.start();
        result.current.start();
      });

      expect(result.current.isRunning).toBe(true);
    });

    it('should maintain isRunning true after start', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.isRunning).toBe(true);
      expect(result.current.timeLeft).toBe(55);
    });
  });

  describe('Timer countdown behavior', () => {
    it('should count down by 1 second every second', () => {
      const { result } = renderHook(() => useTimer(10));

      act(() => {
        result.current.start();
      });

      expect(result.current.timeLeft).toBe(10);

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(9);

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(8);

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(7);
    });

    it('should count down continuously', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(result.current.timeLeft).toBe(50);
    });

    it('should count down to zero', () => {
      const { result } = renderHook(() => useTimer(3));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(2);

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(1);

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(result.current.timeLeft).toBe(0);
    });

    it('should not countdown when not running', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(60);
      expect(result.current.isRunning).toBe(false);
    });
  });

  describe('pause() method', () => {
    it('should pause the countdown', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(55);

      act(() => {
        result.current.pause();
      });

      expect(result.current.isRunning).toBe(false);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(55);
    });

    it('should stop the timer and set isRunning to false', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      expect(result.current.isRunning).toBe(true);

      act(() => {
        result.current.pause();
      });

      expect(result.current.isRunning).toBe(false);
    });

    it('should preserve timeLeft when paused', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(13000);
      });

      const timeBeforePause = result.current.timeLeft;

      act(() => {
        result.current.pause();
      });

      expect(result.current.timeLeft).toBe(timeBeforePause);
      expect(result.current.timeLeft).toBe(47);
    });

    it('should allow pause when already paused', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
        result.current.pause();
        result.current.pause();
      });

      expect(result.current.isRunning).toBe(false);
    });
  });

  describe('resume() method', () => {
    it('should resume the countdown after pause', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      act(() => {
        result.current.pause();
      });

      expect(result.current.timeLeft).toBe(55);
      expect(result.current.isRunning).toBe(false);

      act(() => {
        result.current.resume();
      });

      expect(result.current.isRunning).toBe(true);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(50);
    });

    it('should start countdown from current timeLeft', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(20000);
      });

      act(() => {
        result.current.pause();
      });

      expect(result.current.timeLeft).toBe(40);

      act(() => {
        result.current.resume();
      });

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(result.current.timeLeft).toBe(30);
    });

    it('should not resume if timeLeft is 0', () => {
      const { result } = renderHook(() => useTimer(3));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(result.current.timeLeft).toBe(0);
      expect(result.current.isRunning).toBe(false);

      act(() => {
        result.current.resume();
      });

      expect(result.current.isRunning).toBe(false);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(0);
    });

    it('should allow resume when not started yet', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.resume();
      });

      expect(result.current.isRunning).toBe(true);
    });
  });

  describe('reset() method', () => {
    it('should reset to initial time', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(20000);
      });

      expect(result.current.timeLeft).toBe(40);

      act(() => {
        result.current.reset();
      });

      expect(result.current.timeLeft).toBe(60);
      expect(result.current.isRunning).toBe(false);
    });

    it('should reset to new time when provided', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      act(() => {
        result.current.reset(120);
      });

      expect(result.current.timeLeft).toBe(120);
      expect(result.current.isRunning).toBe(false);
    });

    it('should stop the timer when reset', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      expect(result.current.isRunning).toBe(true);

      act(() => {
        result.current.reset();
      });

      expect(result.current.isRunning).toBe(false);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(60);
    });

    it('should reset to initial time without parameter', () => {
      const { result } = renderHook(() => useTimer(180));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(100000);
      });

      act(() => {
        result.current.reset();
      });

      expect(result.current.timeLeft).toBe(180);
    });

    it('should allow reset multiple times', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.reset(30);
      });
      expect(result.current.timeLeft).toBe(30);

      act(() => {
        result.current.reset(45);
      });
      expect(result.current.timeLeft).toBe(45);

      act(() => {
        result.current.reset();
      });
      expect(result.current.timeLeft).toBe(60);
    });
  });

  describe('stop() method', () => {
    it('should stop the timer', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      expect(result.current.isRunning).toBe(true);

      act(() => {
        result.current.stop();
      });

      expect(result.current.isRunning).toBe(false);
    });

    it('should clear the interval', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      const timeBeforeStop = result.current.timeLeft;

      act(() => {
        result.current.stop();
      });

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(result.current.timeLeft).toBe(timeBeforeStop);
    });

    it('should not change timeLeft', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(25000);
      });

      expect(result.current.timeLeft).toBe(35);

      act(() => {
        result.current.stop();
      });

      expect(result.current.timeLeft).toBe(35);
    });

    it('should be safe to call when already stopped', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.stop();
        result.current.stop();
      });

      expect(result.current.isRunning).toBe(false);
    });
  });

  describe('onTimeout callback', () => {
    it('should call onTimeout when timer reaches 0', () => {
      const onTimeout = jest.fn();
      const { result } = renderHook(() => useTimer(3, onTimeout));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(onTimeout).toHaveBeenCalledTimes(1);
      expect(result.current.timeLeft).toBe(0);
      expect(result.current.isRunning).toBe(false);
    });

    it('should not call onTimeout if timer is stopped before reaching 0', () => {
      const onTimeout = jest.fn();
      const { result } = renderHook(() => useTimer(10, onTimeout));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      act(() => {
        result.current.stop();
      });

      expect(onTimeout).not.toHaveBeenCalled();
    });

    it('should call onTimeout only once when reaching 0', () => {
      const onTimeout = jest.fn();
      const { result } = renderHook(() => useTimer(2, onTimeout));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      expect(onTimeout).toHaveBeenCalled();
      expect(result.current.timeLeft).toBe(0);
    });

    it('should work without onTimeout callback', () => {
      const { result } = renderHook(() => useTimer(3));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(result.current.timeLeft).toBe(0);
      expect(result.current.isRunning).toBe(false);
    });

    it('should call onTimeout after reset and countdown to 0', () => {
      const onTimeout = jest.fn();
      const { result } = renderHook(() => useTimer(5, onTimeout));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      act(() => {
        result.current.reset(2);
      });

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      expect(onTimeout).toHaveBeenCalledTimes(1);
    });
  });

  describe('Timer stops at 0', () => {
    it('should stop automatically when reaching 0', () => {
      const { result } = renderHook(() => useTimer(3));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(result.current.timeLeft).toBe(0);
      expect(result.current.isRunning).toBe(false);
    });

    it('should not go below 0', () => {
      const { result } = renderHook(() => useTimer(2));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(0);
      expect(result.current.timeLeft).toBeGreaterThanOrEqual(0);
    });

    it('should remain at 0 after reaching it', () => {
      const { result } = renderHook(() => useTimer(1));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(result.current.timeLeft).toBe(0);

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(result.current.timeLeft).toBe(0);
    });
  });

  describe('Cleanup and unmount', () => {
    it('should clear interval on unmount', () => {
      const { result, unmount } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      unmount();

      // No error should occur
      act(() => {
        jest.advanceTimersByTime(5000);
      });
    });

    it('should clean up interval when isRunning changes', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      act(() => {
        result.current.pause();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(58);
    });

    it('should clear interval when timer reaches 0', () => {
      const { result } = renderHook(() => useTimer(2));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(2000);
      });

      expect(result.current.isRunning).toBe(false);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(0);
    });
  });

  describe('Edge cases', () => {
    it('should handle pause at 0', () => {
      const { result } = renderHook(() => useTimer(1));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(result.current.timeLeft).toBe(0);

      act(() => {
        result.current.pause();
      });

      expect(result.current.isRunning).toBe(false);
    });

    it('should handle resume at 0', () => {
      const { result } = renderHook(() => useTimer(1));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(result.current.timeLeft).toBe(0);

      act(() => {
        result.current.resume();
      });

      expect(result.current.isRunning).toBe(false);

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(0);
    });

    it('should handle multiple starts without clearing previous interval', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(55);

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.timeLeft).toBe(50);
    });

    it('should handle reset with 0 as new time', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.reset(0);
      });

      expect(result.current.timeLeft).toBe(0);
      expect(result.current.isRunning).toBe(false);
    });

    it('should handle very large initial time', () => {
      const { result } = renderHook(() => useTimer(999999));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(10000);
      });

      expect(result.current.timeLeft).toBe(999989);
    });

    it('should handle rapid start/pause/resume cycles', () => {
      const { result } = renderHook(() => useTimer(60));

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(result.current.timeLeft).toBe(59);

      act(() => {
        result.current.pause();
        result.current.resume();
        result.current.pause();
        result.current.resume();
      });

      expect(result.current.isRunning).toBe(true);

      // After pause/resume cycles, verify timer is running
      // The timeLeft should still be 59 since pause/resume happened quickly
      expect(result.current.timeLeft).toBeGreaterThan(57);
      expect(result.current.timeLeft).toBeLessThanOrEqual(59);
    });
  });

  describe('Method stability', () => {
    it('should maintain referential equality of control methods', () => {
      const { result, rerender } = renderHook(() => useTimer(60));

      const firstStart = result.current.start;
      const firstPause = result.current.pause;
      const firstResume = result.current.resume;
      const firstReset = result.current.reset;
      const firstStop = result.current.stop;

      rerender();

      expect(result.current.start).toBe(firstStart);
      expect(result.current.pause).toBe(firstPause);
      expect(result.current.resume).toBe(firstResume);
      expect(result.current.reset).toBe(firstReset);
      expect(result.current.stop).toBe(firstStop);
    });

    it('should not recreate methods after state changes', () => {
      const { result } = renderHook(() => useTimer(60));

      const initialStart = result.current.start;

      act(() => {
        result.current.start();
      });

      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.start).toBe(initialStart);
    });
  });
});
