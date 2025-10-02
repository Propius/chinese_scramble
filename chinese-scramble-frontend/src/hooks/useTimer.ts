import { useState, useEffect, useCallback, useRef } from 'react';

export interface UseTimerReturn {
  timeLeft: number;
  isRunning: boolean;
  start: () => void;
  pause: () => void;
  resume: () => void;
  reset: (newTime?: number) => void;
  stop: () => void;
}

export const useTimer = (initialTime: number, onTimeout?: () => void): UseTimerReturn => {
  const [timeLeft, setTimeLeft] = useState(initialTime);
  const [isRunning, setIsRunning] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const stop = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    setIsRunning(false);
  }, []);

  useEffect(() => {
    if (isRunning && timeLeft > 0) {
      intervalRef.current = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            stop();
            if (onTimeout) {
              onTimeout();
            }
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [isRunning, timeLeft, stop, onTimeout]);

  const start = useCallback(() => {
    setIsRunning(true);
  }, []);

  const pause = useCallback(() => {
    stop();
  }, [stop]);

  const resume = useCallback(() => {
    if (timeLeft > 0) {
      setIsRunning(true);
    }
  }, [timeLeft]);

  const reset = useCallback((newTime?: number) => {
    stop();
    setTimeLeft(newTime ?? initialTime);
  }, [initialTime, stop]);

  return {
    timeLeft,
    isRunning,
    start,
    pause,
    resume,
    reset,
    stop
  };
};

export default useTimer;
