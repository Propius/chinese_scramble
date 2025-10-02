import React from 'react';
import { useDrag } from 'react-dnd';

interface WordTileProps {
  word: string;
  index: number;
  isPlaced?: boolean;
  disabled?: boolean;
}

export const WordTile: React.FC<WordTileProps> = ({
  word,
  index,
  isPlaced = false,
  disabled = false,
}) => {
  const [{ isDragging }, drag] = useDrag({
    type: 'WORD',
    item: { word, index, isPlaced },
    canDrag: !disabled,
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });

  // Determine word length for responsive sizing - MUCH BIGGER
  const wordLength = word.length;
  const width = wordLength === 1
    ? '110px'
    : wordLength === 2
    ? '130px'
    : wordLength === 3
    ? '160px'
    : '200px';

  return (
    <div
      ref={drag as unknown as React.Ref<HTMLDivElement>}
      style={{
        width: width,
        height: '100px',
        background: isDragging
          ? 'linear-gradient(145deg, #fbcfe8, #f9a8d4)'
          : isPlaced
          ? 'linear-gradient(145deg, #ddd6fe, #c4b5fd)'
          : 'linear-gradient(145deg, #fde68a, #fcd34d)',
        boxShadow: isDragging
          ? '0 4px 8px rgba(0,0,0,0.2), inset 0 -2px 4px rgba(0,0,0,0.2)'
          : '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)',
        transform: isDragging ? 'translateY(4px)' : 'translateY(0)',
        borderRadius: '24px',
      }}
      className={`
        word-tile
        relative
        inline-flex items-center justify-center
        m-4
        transition-all duration-200 ease-out
        px-4
        ${disabled ? 'cursor-not-allowed opacity-50' : 'cursor-grab active:cursor-grabbing'}
        ${!disabled && !isDragging && 'hover:scale-110 hover:-translate-y-2 hover:brightness-110'}
      `}
    >
      {/* Top gloss shine - makes it look like plastic keycap */}
      <div
        className="absolute inset-2 pointer-events-none"
        style={{
          background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
          borderRadius: '20px',
        }}
      ></div>

      {/* Word content - centered and readable */}
      <span
        className="relative z-10 whitespace-nowrap font-black select-none text-gray-800"
        style={{
          textShadow: '0 1px 2px rgba(0,0,0,0.1)',
          fontSize: '1.875rem',
          lineHeight: '1',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
          width: '100%',
        }}
      >{word}</span>

      {/* Bottom dark edge for 3D depth */}
      <div
        className="absolute inset-x-2 bottom-1 h-6 pointer-events-none"
        style={{
          background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
          borderRadius: '0 0 20px 20px',
        }}
      ></div>
    </div>
  );
};

export default WordTile;
