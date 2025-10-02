import React from 'react';
import { useDrag } from 'react-dnd';

interface CharacterTileProps {
  character: string;
  index: number;
  onMove?: (fromIndex: number, toIndex: number) => void;
  isPlaced?: boolean;
  disabled?: boolean;
}

export const CharacterTile: React.FC<CharacterTileProps> = ({
  character,
  index,
  onMove,
  isPlaced = false,
  disabled = false,
}) => {
  const [{ isDragging }, drag] = useDrag({
    type: 'CHARACTER',
    item: { character, index, isPlaced },
    canDrag: !disabled,
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });

  return (
    <div className="relative inline-block m-4">
      {/* FUN ROUNDED KEYCAP for KIDS - Bright and Colorful */}
      <div
        ref={drag as unknown as React.Ref<HTMLDivElement>}
        style={{
          width: '110px',
          height: '110px',
          background: isDragging
            ? 'linear-gradient(145deg, #fde68a, #fcd34d)'
            : isPlaced
            ? 'linear-gradient(145deg, #bfdbfe, #93c5fd)'
            : 'linear-gradient(145deg, #fde68a, #fcd34d)',
          boxShadow: isDragging
            ? '0 4px 8px rgba(0,0,0,0.2), inset 0 -2px 4px rgba(0,0,0,0.2)'
            : '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)',
          transform: isDragging ? 'translateY(4px)' : 'translateY(0)',
          borderRadius: '24px',
        }}
        className={`
          character-tile
          relative
          inline-flex items-center justify-center
          transition-all duration-200 ease-out
          ${disabled ? 'cursor-not-allowed opacity-50' : 'cursor-grab active:cursor-grabbing'}
          ${!disabled && !isDragging && 'hover:scale-110 hover:-translate-y-2 hover:brightness-110'}
        `}
      >
        {/* Top gloss shine - makes it look like plastic keycap */}
        <div
          className="absolute inset-2 rounded-2xl pointer-events-none"
          style={{
            background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
          }}
        ></div>

        {/* Character - centered and readable */}
        <span
          className="relative z-10 font-black select-none text-gray-800"
          style={{
            textShadow: '0 1px 2px rgba(0,0,0,0.1)',
            fontSize: '3.75rem',
            lineHeight: '1',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100%',
            width: '100%',
          }}
        >{character}</span>

        {/* Bottom dark edge for 3D depth */}
        <div
          className="absolute inset-x-2 bottom-1 h-6 rounded-b-2xl pointer-events-none"
          style={{
            background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
          }}
        ></div>
      </div>
    </div>
  );
};

export default CharacterTile;
