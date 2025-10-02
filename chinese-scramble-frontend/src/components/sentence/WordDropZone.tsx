import React from 'react';
import { useDrop } from 'react-dnd';

interface WordDropZoneProps {
  index: number;
  word?: string;
  onDrop: (word: string, fromIndex: number, toIndex: number, fromPlaced: boolean) => void;
  disabled?: boolean;
}

export const WordDropZone: React.FC<WordDropZoneProps> = ({
  index,
  word,
  onDrop,
  disabled = false,
}) => {
  const [justDropped, setJustDropped] = React.useState(false);
  const [ripple, setRipple] = React.useState(false);

  const [{ isOver, canDrop }, drop] = useDrop({
    accept: 'WORD',
    canDrop: () => !disabled,
    drop: (item: { word: string; index: number; isPlaced: boolean }) => {
      onDrop(item.word, item.index, index, item.isPlaced);
      setJustDropped(true);
      setRipple(true);
      setTimeout(() => setJustDropped(false), 800);
      setTimeout(() => setRipple(false), 1000);
    },
    collect: (monitor) => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
  });

  const isActive = isOver && canDrop;

  // Dynamic width based on word length - MUCH BIGGER
  const wordLength = word?.length || 0;
  const width = wordLength === 0
    ? '130px'
    : wordLength === 1
    ? '110px'
    : wordLength === 2
    ? '130px'
    : wordLength === 3
    ? '160px'
    : '200px';

  return (
    <div
      ref={drop as unknown as React.Ref<HTMLDivElement>}
      style={{
        width: width,
        height: '110px',
        background: word
          ? 'linear-gradient(145deg, #ddd6fe, #c4b5fd)'
          : isActive && !disabled
          ? 'linear-gradient(145deg, #a7f3d0, #6ee7b7)'
          : 'linear-gradient(145deg, #fef3c7, #fde68a)',
        boxShadow: word
          ? '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)'
          : isActive && !disabled
          ? '0 8px 0 rgba(16,185,129,0.4), 0 12px 28px rgba(16,185,129,0.5), 0 0 0 6px rgba(16,185,129,0.3)'
          : 'inset 0 2px 8px rgba(0,0,0,0.1), 0 4px 12px rgba(0,0,0,0.1)',
        animation: justDropped && word ? 'bounceIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55)' : undefined,
        borderRadius: '24px',
        transform: justDropped ? 'scale(1.1) rotate(3deg)' : 'scale(1) rotate(0deg)',
        transition: 'transform 0.3s ease-out',
      }}
      className={`
        word-drop-zone
        relative
        inline-flex items-center justify-center
        m-4
        duration-200 ease-out
        px-4
        ${word
          ? ''
          : isActive && !disabled
          ? 'scale-110 animate-pulse border-4 border-green-400'
          : 'border-4 border-dashed border-amber-300'
        }
        ${canDrop && !disabled && !isActive && 'border-4 border-dashed border-blue-400'}
        ${!canDrop && !disabled && !word && 'hover:scale-105 hover:border-blue-400'}
        ${disabled && 'opacity-50 cursor-not-allowed'}
      `}
    >
      {/* Ripple effect on drop */}
      {ripple && (
        <div
          className="absolute inset-0 rounded-3xl pointer-events-none"
          style={{
            animation: 'ripple 1s ease-out',
            background: 'radial-gradient(circle, rgba(147, 51, 234, 0.6) 0%, transparent 70%)',
          }}
        ></div>
      )}

      {/* Gloss shine for plastic keycap look */}
      {word && (
        <div
          className="absolute inset-2 pointer-events-none"
          style={{
            background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
            borderRadius: '20px',
          }}
        ></div>
      )}

      {/* Content - centered and readable */}
      <span
        className={`relative z-10 whitespace-nowrap font-black select-none ${word ? 'text-gray-800' : 'text-gray-500'}`}
        style={{
          textShadow: '0 1px 2px rgba(0,0,0,0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: word ? '1.875rem' : '2.25rem',
          lineHeight: '1',
          height: '100%',
          width: '100%',
        }}
      >
        {word || (index + 1)}
      </span>

      {/* Bottom dark edge for 3D depth */}
      {word && (
        <div
          className="absolute inset-x-2 bottom-1 h-6 pointer-events-none"
          style={{
            background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
            borderRadius: '0 0 20px 20px',
          }}
        ></div>
      )}
    </div>
  );
};

export default WordDropZone;
