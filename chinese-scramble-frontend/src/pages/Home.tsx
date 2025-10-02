import React from 'react';
import Header from '../components/layout/Header';
import GameModeSelector from '../components/layout/GameModeSelector';

export const Home: React.FC = () => {

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem 1rem',
        backgroundColor: '#f8f9fa',
      }}
    >
      <div className="w-full max-w-6xl mx-auto" style={{
        background: 'linear-gradient(135deg, #fffbeb, #fef9c3, #fef3c7)',
        borderRadius: '24px',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        overflow: 'hidden'
      }}>
        <Header
          title="欢迎来到汉字游戏"
          subtitle="选择一个游戏模式开始学习中文"
          variant="card"
        />
        <div style={{
          width: '100%',
          height: '2px',
          background: 'linear-gradient(to right, transparent 10%, rgba(209, 213, 219, 0.8) 50%, transparent 90%)'
        }}></div>
        <div style={{ padding: '2rem' }}>
        <div className="text-center mb-12">
          <h2
            className="text-5xl font-extrabold mb-3"
            style={{
              background: 'linear-gradient(135deg, #dc2626, #ea580c, #d97706)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
            }}
          >
            开始你的中文学习之旅
          </h2>
          <p className="text-xl text-gray-700 font-semibold mt-2">
            通过有趣的游戏学习成语和句子构造
          </p>
        </div>
        <GameModeSelector />

        <div className="mt-16 max-w-5xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div
              className="p-8 shadow-lg transform transition-all hover:scale-105"
              style={{
                borderRadius: '32px',
                background: 'linear-gradient(145deg, #e0f2fe, #bae6fd)',
                border: '2px solid rgba(56, 189, 248, 0.3)',
              }}
            >
              <div className="text-5xl mb-4">🎮</div>
              <h3 className="font-extrabold text-2xl mb-3 text-sky-800">多种难度</h3>
              <p className="text-gray-700 font-medium">从简单到专家级，适合所有水平</p>
            </div>
            <div
              className="p-8 shadow-lg transform transition-all hover:scale-105"
              style={{
                borderRadius: '32px',
                background: 'linear-gradient(145deg, #fef9c3, #fef08a)',
                border: '2px solid rgba(234, 179, 8, 0.3)',
              }}
            >
              <div className="text-5xl mb-4">🏆</div>
              <h3 className="font-extrabold text-2xl mb-3 text-amber-800">排行榜</h3>
              <p className="text-gray-700 font-medium">与其他玩家竞争，争夺榜首</p>
            </div>
            <div
              className="p-8 shadow-lg transform transition-all hover:scale-105"
              style={{
                borderRadius: '32px',
                background: 'linear-gradient(145deg, #d1fae5, #a7f3d0)',
                border: '2px solid rgba(52, 211, 153, 0.3)',
              }}
            >
              <div className="text-5xl mb-4">📊</div>
              <h3 className="font-extrabold text-2xl mb-3 text-emerald-800">统计追踪</h3>
              <p className="text-gray-700 font-medium">跟踪你的进步和成就</p>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
