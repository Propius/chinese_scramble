import React, { useState, useEffect } from 'react';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import CharacterTile from './CharacterTile';
import DropZone from './DropZone';
import { soundManager } from '../../utils/soundManager';

interface IdiomGameProps {
  scrambledCharacters: string[];
  correctAnswer?: string;
  timeLimit: number;
  onSubmit: (answer: string, timeTaken: number, hintsUsed: number) => void;
  onTimeout: () => void;
  onHintRequest: (level: number) => void;
  hint?: string;
  hintLoading?: boolean;
  difficulty: string;
}

export const IdiomGame: React.FC<IdiomGameProps> = ({
  scrambledCharacters,
  correctAnswer,
  timeLimit,
  onSubmit,
  onTimeout,
  onHintRequest,
  hint,
  hintLoading = false,
  difficulty,
}) => {
  const [availableCharacters, setAvailableCharacters] = useState<(string | null)[]>(scrambledCharacters);
  const [placedCharacters, setPlacedCharacters] = useState<(string | null)[]>(
    Array(scrambledCharacters.length).fill(null)
  );
  const [timeLeft, setTimeLeft] = useState(timeLimit);
  const [hintsUsed, setHintsUsed] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [gameStartTime] = useState(Date.now());
  const [selectedTileIndex, setSelectedTileIndex] = useState<number | null>(null);
  const [selectedTileType, setSelectedTileType] = useState<'placed' | null>(null);

  // Reset all state when new question arrives
  useEffect(() => {
    setAvailableCharacters(scrambledCharacters);
    setPlacedCharacters(Array(scrambledCharacters.length).fill(null));
    setSelectedTileIndex(null);
    setSelectedTileType(null);
    setTimeLeft(timeLimit);
    setHintsUsed(0);
    setIsSubmitting(false);
  }, [scrambledCharacters, timeLimit]);

  // Timer countdown
  useEffect(() => {
    if (timeLeft <= 0) {
      soundManager.playTimeout();
      onTimeout();
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          soundManager.playTimeout();
          onTimeout();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft, onTimeout]);

  const handleDrop = (character: string, fromIndex: number, toIndex: number, fromPlaced: boolean) => {
    if (fromPlaced) {
      // Moving from placed to placed
      const newPlaced = [...placedCharacters];
      const draggedChar = newPlaced[fromIndex];
      const targetChar = newPlaced[toIndex];

      newPlaced[fromIndex] = targetChar;
      newPlaced[toIndex] = draggedChar;
      setPlacedCharacters(newPlaced);
    } else {
      // Moving from available to placed
      const newAvailable = [...availableCharacters];
      const newPlaced = [...placedCharacters];

      // If target position has character, swap it back to available
      if (newPlaced[toIndex]) {
        newAvailable[fromIndex] = newPlaced[toIndex];
      } else {
        newAvailable[fromIndex] = null;
      }

      newPlaced[toIndex] = character;
      setAvailableCharacters(newAvailable);
      setPlacedCharacters(newPlaced);
    }
    soundManager.playDrop();
  };

  const handleTileClick = (index: number) => {
    const character = placedCharacters[index];
    if (!character) return;

    // If no tile selected, select this one
    if (selectedTileIndex === null) {
      setSelectedTileIndex(index);
      setSelectedTileType('placed');
      soundManager.playClick();
    } else {
      // Swap with previously selected tile
      const newPlaced = [...placedCharacters];
      const temp = newPlaced[selectedTileIndex];
      newPlaced[selectedTileIndex] = newPlaced[index];
      newPlaced[index] = temp;
      setPlacedCharacters(newPlaced);

      // Clear selection
      setSelectedTileIndex(null);
      setSelectedTileType(null);
      soundManager.playDrop();
    }
  };

  const handleRemove = (index: number) => {
    const character = placedCharacters[index];
    if (!character) return;

    const newPlaced = [...placedCharacters];
    newPlaced[index] = null;
    setPlacedCharacters(newPlaced);

    // Return character to available pool
    const firstEmptyIndex = availableCharacters.findIndex((char) => char === null);
    const newAvailable = [...availableCharacters];
    if (firstEmptyIndex !== -1) {
      newAvailable[firstEmptyIndex] = character;
    } else {
      newAvailable.push(character);
    }
    setAvailableCharacters(newAvailable);
    soundManager.playRemove();
  };

  const handleReset = () => {
    setAvailableCharacters(scrambledCharacters);
    setPlacedCharacters(Array(scrambledCharacters.length).fill(null));
    soundManager.playReset();
  };

  const handleSubmit = async () => {
    const answer = placedCharacters.filter(Boolean).join('');

    // Simple validation - just check if all slots are filled
    if (answer.length !== scrambledCharacters.length) {
      alert('请完成所有字符的排列！(Please place all characters!)');
      return;
    }

    setIsSubmitting(true);
    const timeTaken = Math.floor((Date.now() - gameStartTime) / 1000);
    onSubmit(answer, timeTaken, hintsUsed);
  };

  const handleHint = () => {
    if (hintsUsed >= 3) return;
    const nextLevel = hintsUsed + 1; // Level 1, 2, or 3
    setHintsUsed(nextLevel);
    onHintRequest(nextLevel);
  };

  const canSubmit = placedCharacters.every((char) => char !== null) && !isSubmitting;

  return (
    <DndProvider backend={HTML5Backend}>
      <div
        className="idiom-game max-w-5xl mx-auto p-12 shadow-2xl border-4"
        style={{
          borderRadius: '48px',
          background: 'linear-gradient(145deg, #ffffff, #f0f9ff, #e0f2fe)',
          borderColor: 'rgba(59, 130, 246, 0.3)',
        }}
      >
        {/* Game Header */}
        <div className="flex justify-between items-center mb-8 p-6 shadow-lg" style={{
          borderRadius: '32px',
          background: 'linear-gradient(135deg, #bfdbfe, #ddd6fe, #fbcfe8)',
          color: '#1f2937',
        }}>
          <div className="text-lg font-bold">
            难度: {difficulty}
          </div>
          <div className="text-2xl font-bold">
            ⏱️ {Math.floor(timeLeft / 60)}:{(timeLeft % 60).toString().padStart(2, '0')}
          </div>
          <div className="text-lg font-bold">
            💡 提示: {hintsUsed}/3
          </div>
        </div>

        {/* Instructions */}
        <div className="mb-8 p-6 bg-gradient-to-r from-blue-50 to-purple-50 rounded-2xl border-2 border-blue-300 shadow-md">
          <h3 className="text-xl font-bold mb-2 text-blue-800">📝 游戏说明</h3>
          <p className="text-blue-700 font-medium">
            🎯 拖动下方字符到答案区域排列成语<br/>
            🔄 点击答案区的字符选中，再点击另一个交换位置<br/>
            ✕ 点击红色按钮移除字符
          </p>
        </div>

        {/* Answer Area */}
        <div className="mb-8">
          <h3 className="text-2xl font-bold mb-4 text-center text-gray-800 flex items-center justify-center gap-2">
            <span>📝</span> 答案区域
          </h3>
          <div className="flex justify-center flex-wrap items-center p-8 rounded-xl shadow-inner border-3 border-blue-300" style={{
            background: 'linear-gradient(to bottom right, #dbeafe, #e9d5ff)'
          }}>
            {placedCharacters.map((char, index) => (
              <div key={index} className="relative">
                {char ? (
                  <div className="relative inline-block m-2">
                    {/* Blue pin on top */}
                    <div className="absolute -top-3 left-1/2 transform -translate-x-1/2 z-20">
                      <div className="w-3 h-3 bg-blue-500 rounded-full border-2 border-blue-600 shadow-lg"></div>
                      <div className="w-0.5 h-4 bg-blue-500 mx-auto"></div>
                    </div>

                    {/* Tile - Click to swap - 3D keycap style */}
                    <div
                      className="character-tile relative cursor-pointer transition-all duration-300"
                      style={{
                        position: 'relative',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: '110px',
                        height: '110px',
                        borderRadius: '24px',
                        background: selectedTileIndex === index && selectedTileType === 'placed'
                          ? 'linear-gradient(145deg, #fde68a, #fcd34d)'
                          : 'linear-gradient(145deg, #bfdbfe, #93c5fd)',
                        border: 'none',
                        boxShadow: selectedTileIndex === index && selectedTileType === 'placed'
                          ? '0 0 20px 5px rgba(250, 204, 21, 0.8), 0 0 40px 10px rgba(250, 204, 21, 0.4), 0 8px 0 rgba(0,0,0,0.15)'
                          : '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)',
                        transform: selectedTileIndex === index && selectedTileType === 'placed' ? 'scale(1.1)' : 'scale(1)',
                      }}
                      onClick={() => handleTileClick(index)}
                      title="点击选择，再点击另一个瓦片交换位置"
                    >
                      {/* Remove button - Badge style like difficulty checkmark */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleRemove(index);
                        }}
                        className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-flex align-items-center justify-center"
                        style={{
                          zIndex: 30,
                          width: '24px',
                          height: '24px',
                          padding: '0',
                          fontSize: '0.875rem',
                          fontWeight: 'bold',
                          cursor: 'pointer',
                          border: 'none',
                          boxShadow: '0 2px 6px rgba(239, 68, 68, 0.5)',
                        }}
                        title="点击移除"
                      >
                        ✕
                      </button>

                      {/* Top gloss shine */}
                      <div
                        className="absolute inset-2 rounded-2xl pointer-events-none"
                        style={{
                          background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
                        }}
                      ></div>

                      {/* Character */}
                      <span
                        className="relative z-10 font-black text-gray-800"
                        style={{
                          fontSize: '1.875rem',
                          lineHeight: '1',
                          textShadow: '0 1px 2px rgba(0,0,0,0.1)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          height: '100%',
                          width: '100%',
                        }}
                      >
                        {char}
                      </span>

                      {/* Bottom dark edge for 3D depth */}
                      <div
                        className="absolute inset-x-2 bottom-1 h-6 rounded-b-2xl pointer-events-none"
                        style={{
                          background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
                        }}
                      ></div>
                    </div>
                  </div>
                ) : (
                  <DropZone
                    index={index}
                    character={char || undefined}
                    onDrop={handleDrop}
                    disabled={isSubmitting}
                  />
                )}
              </div>
            ))}
          </div>
          <p className="text-center text-sm text-gray-500 mt-3">
            💡 提示: 拖动下方字符到答案区，或点击已放置的字符移除
          </p>
        </div>

        {/* Available Characters */}
        <div className="mb-8">
          <h3 className="text-2xl font-bold mb-4 text-center text-gray-800 flex items-center justify-center gap-2">
            <span>🎯</span> 可用字符
          </h3>
          <div className="flex justify-center flex-wrap bg-gradient-to-br from-green-50 to-emerald-50 p-8 rounded-xl shadow-inner border-3 border-green-200 min-h-[140px]">
            {availableCharacters.map((char, index) => (
              char && (
                <CharacterTile
                  key={index}
                  character={char}
                  index={index}
                  isPlaced={false}
                  disabled={isSubmitting}
                />
              )
            ))}
          </div>
          <p className="text-center text-sm text-gray-500 mt-3">
            👆 拖动字符到上方答案区域
          </p>
        </div>

        {/* Hint Display */}
        {(hint || hintLoading) && (
          <div className="mb-6 p-5 rounded-xl border-3 shadow-md" style={{
            background: 'linear-gradient(145deg, #fef9c3, #fef3c7)',
            borderColor: '#fbbf24',
            borderWidth: '3px',
          }}>
            <h3 className="text-lg font-bold mb-3 text-yellow-800 flex items-center gap-2">
              💡 提示信息
            </h3>
            <div className="text-yellow-800 whitespace-pre-line leading-relaxed text-base p-3 bg-white rounded-lg border border-yellow-200">
              {hintLoading ? (
                <div className="flex items-center justify-center gap-2">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-yellow-600"></div>
                  <span>获取提示中...</span>
                </div>
              ) : (
                hint
              )}
            </div>
          </div>
        )}

        {/* Action Buttons - All in same row */}
        <div className="flex justify-center gap-3 flex-wrap">
          <button
            onClick={handleHint}
            disabled={isSubmitting || hintsUsed >= 3 || hintLoading}
            className="btn btn-warning px-3 py-2 shadow"
          >
            {hintLoading ? '⏳ 获取中...' : `💡 获取提示 ${hintsUsed > 0 ? `(${hintsUsed}/3)` : `(${3 - hintsUsed} 次可用)`}`}
          </button>

          <button
            onClick={handleReset}
            disabled={isSubmitting}
            className="btn btn-danger px-3 py-2 shadow"
          >
            🔄 重置
          </button>

          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="btn btn-success px-4 py-2 shadow"
          >
            {isSubmitting ? '⏳ 提交中...' : '✓ 提交答案'}
          </button>
        </div>

        {/* Progress Indicator */}
        <div className="mt-6">
          <div className="w-full bg-gray-200 rounded-full h-3">
            <div
              className="bg-gradient-to-r from-blue-500 to-purple-600 h-3 rounded-full transition-all duration-300"
              style={{
                width: `${(placedCharacters.filter(Boolean).length / placedCharacters.length) * 100}%`,
              }}
            />
          </div>
          <p className="text-center text-sm text-gray-600 mt-2">
            已完成: {placedCharacters.filter(Boolean).length} / {placedCharacters.length}
          </p>
        </div>
      </div>
    </DndProvider>
  );
};

export default IdiomGame;
