import React, { useState, useEffect } from 'react';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import WordTile from './WordTile';
import WordDropZone from './WordDropZone';
import { soundManager } from '../../utils/soundManager';

interface SentenceGameProps {
  scrambledWords: string[];
  correctAnswer?: string[];
  timeLimit: number;
  onSubmit: (answer: string[], timeTaken: number, hintsUsed: number) => void;
  onTimeout: () => void;
  onHintRequest: (level: number) => void;
  hint?: string;
  hintLoading?: boolean;
  difficulty: string;
  grammarPattern?: string;
  translation?: string;
}

export const SentenceGame: React.FC<SentenceGameProps> = ({
  scrambledWords,
  correctAnswer,
  timeLimit,
  onSubmit,
  onTimeout,
  onHintRequest,
  hint,
  hintLoading = false,
  difficulty,
  grammarPattern,
  translation,
}) => {
  const [availableWords, setAvailableWords] = useState<(string | null)[]>(scrambledWords);
  const [placedWords, setPlacedWords] = useState<(string | null)[]>(
    Array(scrambledWords.length).fill(null)
  );
  const [timeLeft, setTimeLeft] = useState(timeLimit);
  const [hintsUsed, setHintsUsed] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [gameStartTime] = useState(Date.now());
  const [showTranslation, setShowTranslation] = useState(false);
  const [selectedTileIndex, setSelectedTileIndex] = useState<number | null>(null);
  const [selectedTileType, setSelectedTileType] = useState<'placed' | null>(null);

  // Reset all state when new question arrives
  useEffect(() => {
    setAvailableWords(scrambledWords);
    setPlacedWords(Array(scrambledWords.length).fill(null));
    setSelectedTileIndex(null);
    setSelectedTileType(null);
    setTimeLeft(timeLimit);
    setHintsUsed(0);
    setIsSubmitting(false);
    setShowTranslation(false);
  }, [scrambledWords, timeLimit]);

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

  const handleDrop = (word: string, fromIndex: number, toIndex: number, fromPlaced: boolean) => {
    if (fromPlaced) {
      // Moving from placed to placed (reordering)
      const newPlaced = [...placedWords];
      const draggedWord = newPlaced[fromIndex];
      const targetWord = newPlaced[toIndex];

      newPlaced[fromIndex] = targetWord;
      newPlaced[toIndex] = draggedWord;
      setPlacedWords(newPlaced);
    } else {
      // Moving from available to placed
      const newAvailable = [...availableWords];
      const newPlaced = [...placedWords];

      // If target position has word, swap it back to available
      if (newPlaced[toIndex]) {
        newAvailable[fromIndex] = newPlaced[toIndex];
      } else {
        newAvailable[fromIndex] = null;
      }

      newPlaced[toIndex] = word;
      setAvailableWords(newAvailable);
      setPlacedWords(newPlaced);
    }
    soundManager.playDrop();
  };

  const handleTileClick = (index: number) => {
    const word = placedWords[index];
    if (!word) return;

    // If no tile selected, select this one
    if (selectedTileIndex === null) {
      setSelectedTileIndex(index);
      setSelectedTileType('placed');
      soundManager.playClick();
    } else {
      // Swap with previously selected tile
      const newPlaced = [...placedWords];
      const temp = newPlaced[selectedTileIndex];
      newPlaced[selectedTileIndex] = newPlaced[index];
      newPlaced[index] = temp;
      setPlacedWords(newPlaced);

      // Clear selection
      setSelectedTileIndex(null);
      setSelectedTileType(null);
      soundManager.playDrop();
    }
  };

  const handleRemove = (index: number) => {
    const word = placedWords[index];
    if (!word) return;

    const newPlaced = [...placedWords];
    newPlaced[index] = null;
    setPlacedWords(newPlaced);

    // Return word to available pool
    const firstEmptyIndex = availableWords.findIndex((w) => w === null);
    const newAvailable = [...availableWords];
    if (firstEmptyIndex !== -1) {
      newAvailable[firstEmptyIndex] = word;
    } else {
      newAvailable.push(word);
    }
    setAvailableWords(newAvailable);

    // Clear selection if the removed tile was selected
    if (selectedTileIndex === index) {
      setSelectedTileIndex(null);
      setSelectedTileType(null);
    }
    soundManager.playRemove();
  };

  const handleReset = () => {
    setAvailableWords(scrambledWords);
    setPlacedWords(Array(scrambledWords.length).fill(null));
    setShowTranslation(false);
    setSelectedTileIndex(null);
    setSelectedTileType(null);
    soundManager.playReset();
  };

  const handleSubmit = async () => {
    const answer = placedWords.filter(Boolean) as string[];
    if (correctAnswer && answer.length !== correctAnswer.length) {
      alert('请完成所有词语的排列！(Please place all words!)');
      return;
    }

    setIsSubmitting(true);
    const timeTaken = Math.floor((Date.now() - gameStartTime) / 1000);
    onSubmit(answer, timeTaken, hintsUsed);
  };

  const handleHint = () => {
    const nextLevel = hintsUsed + 1; // Level 1, 2, or 3
    setHintsUsed(nextLevel);
    onHintRequest(nextLevel);
  };

  const canSubmit = placedWords.every((word) => word !== null) && !isSubmitting;

  // Format the sentence for display
  const currentSentence = placedWords.filter(Boolean).join('');

  return (
    <DndProvider backend={HTML5Backend}>
      <div
        className="sentence-game max-w-6xl mx-auto p-12 shadow-2xl border-4"
        style={{
          borderRadius: '48px',
          background: 'linear-gradient(145deg, #ffffff, #f0fdf4, #dcfce7)',
          borderColor: 'rgba(16, 185, 129, 0.3)',
        }}
      >
        {/* Game Header */}
        <div className="flex justify-between items-center mb-8 p-6 shadow-lg" style={{
          borderRadius: '32px',
          background: 'linear-gradient(135deg, #ddd6fe, #fbcfe8, #fed7aa)',
          color: '#1f2937',
        }}>
          <div className="text-lg font-bold">
            难度: {difficulty}
          </div>
          <div className="text-2xl font-bold">
            ⏱️ {Math.floor(timeLeft / 60)}:{(timeLeft % 60).toString().padStart(2, '0')}
          </div>
          <div className="text-lg font-bold">
            💡 提示: {hintsUsed}
          </div>
        </div>

        {/* Instructions */}
        <div className="mb-8 p-6 bg-gradient-to-r from-purple-50 to-pink-50 rounded-2xl border-2 border-purple-300 shadow-md">
          <h3 className="text-xl font-bold mb-2 text-purple-800">📝 游戏说明</h3>
          <p className="text-purple-700 font-medium">
            🎯 拖动下方的词语到句子区域排列<br/>
            🔄 点击答案区的词语选中，再点击另一个交换位置<br/>
            ✕ 点击红色按钮移除词语
          </p>
          {grammarPattern && (
            <div className="mt-3 p-2 bg-white rounded-lg border border-purple-200">
              <span className="font-bold text-purple-800">语法模式：</span>
              <span className="text-purple-700 ml-2">{grammarPattern}</span>
            </div>
          )}
        </div>

        {/* Sentence Building Area */}
        <div className="mb-8">
          <h3 className="text-xl font-bold mb-4 text-center text-gray-800">句子区域</h3>

          {/* Drop Zones */}
          <div className="flex justify-center flex-wrap items-center p-8 rounded-xl shadow-inner border-3 border-purple-300 min-h-[140px]" style={{
            background: 'linear-gradient(to bottom right, #f3e8ff, #fce7f3)'
          }}>
            {placedWords.map((word, index) => (
              <div key={index} className="relative m-2">
                {word ? (
                  <div className="relative inline-block">
                    {/* Blue pin on top */}
                    <div className="absolute -top-3 left-1/2 transform -translate-x-1/2 z-20">
                      <div className="w-3 h-3 bg-purple-500 rounded-full border-2 border-purple-600 shadow-lg"></div>
                      <div className="w-0.5 h-4 bg-purple-500 mx-auto"></div>
                    </div>

                    {/* Tile - Click to swap - 3D keycap style */}
                    <div
                      className="word-tile relative cursor-pointer transition-all duration-300"
                      style={{
                        position: 'relative',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        minWidth: '130px',
                        height: '110px',
                        borderRadius: '24px',
                        background: selectedTileIndex === index && selectedTileType === 'placed'
                          ? 'linear-gradient(145deg, #fde68a, #fcd34d)'
                          : 'linear-gradient(145deg, #ddd6fe, #c4b5fd)',
                        border: 'none',
                        boxShadow: selectedTileIndex === index && selectedTileType === 'placed'
                          ? '0 0 20px 5px rgba(250, 204, 21, 0.8), 0 0 40px 10px rgba(250, 204, 21, 0.4), 0 8px 0 rgba(0,0,0,0.15)'
                          : '0 8px 0 rgba(0,0,0,0.15), 0 10px 20px rgba(0,0,0,0.2), inset 0 -4px 6px rgba(0,0,0,0.1), inset 0 2px 4px rgba(255,255,255,0.8)',
                        transform: selectedTileIndex === index && selectedTileType === 'placed' ? 'scale(1.1)' : 'scale(1)',
                      }}
                      onClick={() => handleTileClick(index)}
                      title="点击选择，再点击另一个词语交换位置"
                    >
                      {/* Remove button - AT THE TOP RIGHT CORNER */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleRemove(index);
                        }}
                        className="text-white rounded-full w-6 h-6 flex items-center justify-center font-bold hover:scale-110 transition-all duration-200"
                        style={{
                          position: 'absolute',
                          top: '4px',
                          right: '4px',
                          zIndex: 30,
                          background: 'linear-gradient(145deg, #ef4444, #dc2626)',
                          boxShadow: '0 2px 6px rgba(239, 68, 68, 0.5)',
                          fontSize: '0.875rem',
                        }}
                        title="点击移除"
                      >
                        ✕
                      </button>

                      {/* Gloss shine */}
                      <div
                        className="absolute inset-2 pointer-events-none"
                        style={{
                          background: 'linear-gradient(145deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.1) 50%, transparent 100%)',
                          borderRadius: '20px',
                        }}
                      ></div>

                      {/* Word content */}
                      <span
                        className="relative z-10 whitespace-nowrap font-black text-gray-800"
                        style={{
                          textShadow: '0 1px 2px rgba(0,0,0,0.1)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          height: '100%',
                          width: '100%',
                          fontSize: '1.875rem',
                          lineHeight: '1',
                        }}
                      >
                        {word}
                      </span>

                      {/* Bottom dark edge */}
                      <div
                        className="absolute inset-x-2 bottom-1 h-6 pointer-events-none"
                        style={{
                          background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.25))',
                          borderRadius: '0 0 20px 20px',
                        }}
                      ></div>
                    </div>
                  </div>
                ) : (
                  <WordDropZone
                    index={index}
                    word={word || undefined}
                    onDrop={handleDrop}
                    disabled={isSubmitting}
                  />
                )}
              </div>
            ))}
          </div>
          <p className="text-center text-sm text-gray-500 mt-3">
            💡 提示: 拖动下方词语到答案区，或点击已放置的词语交换位置
          </p>

          {/* Current Sentence Preview */}
          {currentSentence && (
            <div className="mt-4 p-4 bg-blue-50 rounded-lg border-2 border-blue-200">
              <p className="text-center text-xl font-bold text-blue-800">
                {currentSentence}
              </p>
              {showTranslation && translation && (
                <p className="text-center text-gray-600 mt-2 italic">
                  {translation}
                </p>
              )}
            </div>
          )}
        </div>

        {/* Available Words */}
        <div className="mb-8">
          <h3 className="text-xl font-bold mb-4 text-center text-gray-800">可用词语</h3>
          <div className="flex justify-center flex-wrap bg-green-50 p-6 rounded-lg shadow-inner border-2 border-green-200 min-h-[80px]">
            {availableWords.map((word, index) => (
              word && (
                <WordTile
                  key={index}
                  word={word}
                  index={index}
                  isPlaced={false}
                  disabled={isSubmitting}
                />
              )
            ))}
          </div>
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

          {translation && (
            <button
              onClick={() => setShowTranslation(!showTranslation)}
              disabled={isSubmitting}
              className="btn btn-info px-3 py-2 shadow"
            >
              {showTranslation ? '🙈 隐藏翻译' : '👁️ 显示翻译'}
            </button>
          )}

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
              className="bg-gradient-to-r from-purple-500 to-pink-600 h-3 rounded-full transition-all duration-300"
              style={{
                width: `${(placedWords.filter(Boolean).length / placedWords.length) * 100}%`,
              }}
            />
          </div>
          <p className="text-center text-sm text-gray-600 mt-2">
            已完成: {placedWords.filter(Boolean).length} / {placedWords.length} 词语
          </p>
        </div>
      </div>
    </DndProvider>
  );
};

export default SentenceGame;
