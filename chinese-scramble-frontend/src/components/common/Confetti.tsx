import React, { useEffect, useState } from 'react';

interface ConfettiProps {
  active: boolean;
  duration?: number;
}

const Confetti: React.FC<ConfettiProps> = ({ active, duration = 3000 }) => {
  const [confettiPieces, setConfettiPieces] = useState<React.ReactElement[]>([]);

  useEffect(() => {
    if (active) {
      const pieces: React.ReactElement[] = [];
      const colors = ['#fbbf24', '#f59e0b', '#d97706', '#ef4444', '#ec4899', '#8b5cf6', '#3b82f6', '#10b981'];
      const animations = ['confetti-fall-1', 'confetti-fall-2', 'confetti-fall-3'];

      for (let i = 0; i < 50; i++) {
        const left = Math.random() * 100;
        const color = colors[Math.floor(Math.random() * colors.length)];
        const animation = animations[Math.floor(Math.random() * animations.length)];
        const animationDuration = 2 + Math.random() * 2;
        const delay = Math.random() * 0.5;
        const size = 8 + Math.random() * 8;

        pieces.push(
          <div
            key={i}
            style={{
              position: 'fixed',
              left: `${left}%`,
              top: '-20px',
              width: `${size}px`,
              height: `${size}px`,
              backgroundColor: color,
              borderRadius: Math.random() > 0.5 ? '50%' : '0',
              animation: `${animation} ${animationDuration}s linear ${delay}s forwards`,
              zIndex: 9999,
              pointerEvents: 'none',
            }}
          />
        );
      }

      setConfettiPieces(pieces);

      setTimeout(() => {
        setConfettiPieces([]);
      }, duration);
    }
  }, [active, duration]);

  return <>{confettiPieces}</>;
};

export default Confetti;
