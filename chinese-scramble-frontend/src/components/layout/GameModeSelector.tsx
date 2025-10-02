import React from 'react';
import { useNavigate } from 'react-router-dom';
import { GameType, GAME_TYPE_LABELS } from '../../constants/gameTypes';
import { ROUTES } from '../../constants/routes';

export const GameModeSelector: React.FC = () => {
  const navigate = useNavigate();

  const handleGameSelect = (gameType: GameType) => {
    if (gameType === GameType.IDIOM) {
      navigate(ROUTES.IDIOM_GAME);
    } else {
      navigate(ROUTES.SENTENCE_GAME);
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto mb-16">
      <button
        onClick={() => handleGameSelect(GameType.IDIOM)}
        className="p-8 transition-all transform hover:scale-105 shadow-lg border-2"
        style={{
          borderRadius: '32px',
          background: 'linear-gradient(145deg, #fef08a, #fde047)',
          borderColor: 'rgba(234, 179, 8, 0.4)',
          color: '#78350f',
          minHeight: '200px',
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.boxShadow = '0 20px 40px rgba(234, 179, 8, 0.3)';
          e.currentTarget.style.transform = 'scale(1.05)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.1)';
          e.currentTarget.style.transform = 'scale(1)';
        }}
      >
        <div className="text-6xl mb-4">📝</div>
        <h3 className="text-3xl font-extrabold mb-3">{GAME_TYPE_LABELS[GameType.IDIOM]}</h3>
        <p className="text-lg font-medium" style={{ color: '#a16207' }}>
          将打乱的汉字重新排列成正确的成语
        </p>
      </button>

      <button
        onClick={() => handleGameSelect(GameType.SENTENCE)}
        className="p-8 transition-all transform hover:scale-105 shadow-lg border-2"
        style={{
          borderRadius: '32px',
          background: 'linear-gradient(145deg, #a7f3d0, #6ee7b7)',
          borderColor: 'rgba(52, 211, 153, 0.4)',
          color: '#064e3b',
          minHeight: '200px',
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.boxShadow = '0 20px 40px rgba(52, 211, 153, 0.3)';
          e.currentTarget.style.transform = 'scale(1.05)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.1)';
          e.currentTarget.style.transform = 'scale(1)';
        }}
      >
        <div className="text-6xl mb-4">✍️</div>
        <h3 className="text-3xl font-extrabold mb-3">{GAME_TYPE_LABELS[GameType.SENTENCE]}</h3>
        <p className="text-lg font-medium" style={{ color: '#065f46' }}>
          将打乱的词语重新排列成正确的句子
        </p>
      </button>
    </div>
  );
};

export default GameModeSelector;
