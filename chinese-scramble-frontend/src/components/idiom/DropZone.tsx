import React from 'react';
import { useDrop } from 'react-dnd';

interface DropZoneProps {
  index: number;
  character?: string;
  onDrop: (character: string, fromIndex: number, toIndex: number, fromPlaced: boolean) => void;
  disabled?: boolean;
}

export const DropZone: React.FC<DropZoneProps> = ({
  index,
  character,
  onDrop,
  disabled = false,
}) => {
  const [justDropped, setJustDropped] = React.useState(false);
  const [ripple, setRipple] = React.useState(false);

  const [{ isOver, canDrop }, drop] = useDrop({
    accept: 'CHARACTER',
    canDrop: () => !disabled,
    drop: (item: { character: string; index: number; isPlaced: boolean }) => {
      onDrop(item.character, item.index, index, item.isPlaced);
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

  return (
    <div className="relative inline-block m-4">
      {/* FUN KEYCAP DROP ZONE for KIDS */}
      <div
        ref={drop as unknown as React.Ref<HTMLDivElement>}
        style={{
          width: '110px',
          height: '110px',
          background: character
            ? 'linear-gradient(145deg, #bfdbfe, #93c5fd)'
            : isActive && !disabled
            ? 'linear-gradient(145deg, #a7f3d0, #6ee7b7)'
            : 'linear-gradient(145deg, #fef3c7, #fde68a)',
          boxShadow: character
            ? '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)'
            : isActive && !disabled
            ? '0 8px 0 rgba(16,185,129,0.4), 0 12px 28px rgba(16,185,129,0.5), 0 0 0 6px rgba(16,185,129,0.3)'
            : 'inset 0 2px 8px rgba(0,0,0,0.1), 0 4px 12px rgba(0,0,0,0.1)',
          animation: justDropped && character ? 'bounceIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55)' : undefined,
          borderRadius: '24px',
          transform: justDropped ? 'scale(1.1) rotate(5deg)' : 'scale(1) rotate(0deg)',
          transition: 'transform 0.3s ease-out',
        }}
        className={`
          drop-zone
          relative
          inline-flex items-center justify-center
          duration-200 ease-out
          ${character
            ? ''
            : isActive && !disabled
            ? 'scale-110 animate-pulse'
            : 'border-4 border-dashed border-amber-300'
          }
          ${canDrop && !disabled && !isActive && 'border-4 border-dashed border-blue-400'}
          ${!canDrop && !disabled && !character && 'hover:scale-105 hover:border-blue-400'}
          ${disabled && 'opacity-50 cursor-not-allowed'}
        `}
      >
        {/* Ripple effect on drop */}
        {ripple && (
          <div
            className="absolute inset-0 rounded-3xl pointer-events-none"
            style={{
              animation: 'ripple 1s ease-out',
              background: 'radial-gradient(circle, rgba(59, 130, 246, 0.6) 0%, transparent 70%)',
            }}
          ></div>
        )}

        {/* Gloss shine for plastic keycap look */}
        {character && (
          <div
            className="absolute inset-2 rounded-2xl pointer-events-none"
            style={{
              background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
            }}
          ></div>
        )}

        {/* Content */}
        {character ? (
          <span
            className="relative z-10 font-black select-none text-gray-800"
            style={{
              textShadow: '0 1px 2px rgba(0,0,0,0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '3.75rem',
              lineHeight: '1',
              height: '100%',
              width: '100%',
            }}
          >{character}</span>
        ) : (
          <span
            className="relative z-10 font-bold text-gray-500"
            style={{
              textShadow: '0 1px 2px rgba(0,0,0,0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '3rem',
              lineHeight: '1',
              height: '100%',
              width: '100%',
            }}
          >{index + 1}</span>
        )}

        {/* Bottom dark edge for 3D depth */}
        {character && (
          <div
            className="absolute inset-x-2 bottom-1 h-6 rounded-b-2xl pointer-events-none"
            style={{
              background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
            }}
          ></div>
        )}
      </div>
    </div>
  );
};

export default DropZone;
