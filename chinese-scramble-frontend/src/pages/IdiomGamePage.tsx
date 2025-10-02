import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Header from '../components/layout/Header';
import { Difficulty, DIFFICULTY_LABELS } from '../constants/difficulties';
import { useIdiomGame } from '../hooks/useIdiomGame';
import IdiomGame from '../components/idiom/IdiomGame';
import apiClient from '../services/api';
import Confetti from '../components/common/Confetti';
import { usernameUtils } from '../utils/usernameUtils';
import { soundManager } from '../utils/soundManager';
import { DEFAULT_FEATURE_FLAGS } from '../constants/game.constants';

interface GameResult {
  isCorrect: boolean;  // Backend returns isCorrect for idioms
  score: number;
  meaning?: string;
  example?: string;
  feedback?: string;
  accuracy?: number;
  allQuestionsCompleted?: boolean;
}

export const IdiomGamePage: React.FC = () => {
  const { t } = useTranslation();
  const [difficulty, setDifficulty] = useState<Difficulty>(Difficulty.EASY);
  const [gameStarted, setGameStarted] = useState(false);
  const { question, loading, error, startGame } = useIdiomGame();
  const [gameResult, setGameResult] = useState<GameResult | null>(null);
  const [hint, setHint] = useState<string>('');
  const [hintLoading, setHintLoading] = useState(false);
  const [totalScore, setTotalScore] = useState(0);
  const [gamesPlayed, setGamesPlayed] = useState(0);
  const [showConfetti, setShowConfetti] = useState(false);
  const [quizCompleted, setQuizCompleted] = useState(false);

  // Reset completion state when component mounts
  useEffect(() => {
    setQuizCompleted(false);
    setGameResult(null);
  }, []);

  const handleStart = async () => {
    try {
      await startGame(difficulty);
      setGameStarted(true);
      setGameResult(null);
      setHint('');
      setQuizCompleted(false);
    } catch (err: any) {
      // Check if quiz is completed
      const errorData = err.response?.data;
      if (errorData?.details?.allQuestionsCompleted || errorData?.error?.includes('已完成所有')) {
        setQuizCompleted(true);
        setGameResult({
          isCorrect: false,
          score: 0,
          feedback: errorData?.message || errorData?.error || '恭喜！您已完成所有题目！',
          allQuestionsCompleted: true,
        });
      }
    }
  };

  const handleSubmit = async (answer: string, timeTaken: number, hintsUsed: number) => {
    try {
      const username = usernameUtils.getUsername() || 'Guest';
      console.log('Submitting idiom answer:', { answer, timeTaken, hintsUsed, username });
      const result = await apiClient.post<GameResult>(`/api/idiom-game/submit?playerId=${encodeURIComponent(username)}`, {
        answer,
        timeTaken,
        hintsUsed,
      });
      console.log('Submit result:', result);

      setTotalScore((prev) => prev + (result.score || 0));
      setGamesPlayed((prev) => prev + 1);

      // Play sound and trigger confetti based on result
      if (result.isCorrect) {
        soundManager.playWin();
        setShowConfetti(true);
        setTimeout(() => setShowConfetti(false), 3000);
      } else {
        soundManager.playLose();
      }

      // Check if no-repeat feature is enabled
      if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS) {
        // NO-REPEAT ENABLED: Check for completion
        // IMMEDIATELY check if there are more questions (to decide flow)
        (async () => {
          try {
            // Attempt to preload next question (in background)
            await startGame(difficulty);

            // Success! More questions exist.
            // Show current result, wait 3 seconds, then clear to reveal loaded question
            setGameResult(result);
            setTimeout(() => {
              setGameResult(null);
              setHint('');
            }, 3000);

          } catch (startErr: any) {
            // Failed to load next question - check if it's completion
            const errorData = startErr.response?.data;
            if (errorData?.details?.allQuestionsCompleted || errorData?.error?.includes('已完成所有')) {
              // IT'S THE LAST QUESTION! Skip normal feedback, go straight to completion
              setQuizCompleted(true);
              setGameResult({
                ...result,
                allQuestionsCompleted: true,
                feedback: errorData?.message || errorData?.error || '恭喜！您已完成所有题目！',
              });
            } else {
              // Other error - show normal feedback flow
              setGameResult(result);
              setTimeout(() => {
                setGameResult(null);
                setHint('');
              }, 3000);
            }
          }
        })();
      } else {
        // NO-REPEAT DISABLED: Simple flow - always load next question after 3 seconds
        setGameResult(result);
        setTimeout(async () => {
          setGameResult(null);
          setHint('');
          await startGame(difficulty);
        }, 3000);
      }
    } catch (err: any) {
      soundManager.playLose();
      console.error('Submit error:', err);
      console.error('Error response:', err.response?.data);
      setGameResult({
        isCorrect: false,
        score: 0,
        feedback: err.response?.data?.error || err.response?.data?.message || '提交失败，请重试',
      });
    }
  };

  const handleTimeout = () => {
    setGameResult({
      isCorrect: false,
      score: 0,
      feedback: '时间到！请再试一次。',
    });

    setTimeout(async () => {
      setGameResult(null);
      setHint('');
      await startGame(difficulty);
    }, 3000);
  };

  const handleHintRequest = async (level: number) => {
    soundManager.playHint();
    setHintLoading(true);
    try {
      const username = usernameUtils.getUsername() || 'Guest';
      console.log('Requesting hint level:', level, 'for user:', username);
      const data = await apiClient.post<{ content?: string; hint?: string; message?: string }>(`/api/idiom-game/hint/${level}?playerId=${encodeURIComponent(username)}`);
      console.log('Hint response:', data);
      setHint(data.content || data.hint || data.message || '');
    } catch (err: any) {
      console.error('Hint error:', err);
      console.error('Error response:', err.response?.data);
      setHint(err.response?.data?.error || err.response?.data?.message || '提示获取失败');
    } finally {
      setHintLoading(false);
    }
  };

  const handleBackToMenu = () => {
    setGameStarted(false);
    setGameResult(null);
    setHint('');
    setTotalScore(0);
    setGamesPlayed(0);
    setQuizCompleted(false);
  };

  const handleRestartQuiz = async () => {
    try {
      // Only call backend restart if no-repeat feature is enabled
      if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS) {
        const username = usernameUtils.getUsername() || 'Guest';
        await apiClient.post(`/api/idiom-game/restart?playerId=${encodeURIComponent(username)}`);
      }

      // Reset state
      setQuizCompleted(false);
      setGameResult(null);
      setTotalScore(0);
      setGamesPlayed(0);

      // Start new game
      await handleStart();
    } catch (err: any) {
      console.error('Restart error:', err);
      setGameResult({
        isCorrect: false,
        score: 0,
        feedback: '重新开始失败，请重试',
      });
    }
  };

  const timeLimit = {
    [Difficulty.EASY]: 180,
    [Difficulty.MEDIUM]: 120,
    [Difficulty.HARD]: 90,
    [Difficulty.EXPERT]: 60,
  }[difficulty];

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem 1rem',
      backgroundColor: '#f8f9fa',
    }}>
      <Confetti active={showConfetti} />
      <div className="w-full max-w-6xl mx-auto" style={{
        background: 'linear-gradient(to bottom right, #d1fae5, #a7f3d0, #86efac)',
        borderRadius: '24px',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        overflow: 'hidden'
      }}>
        <Header title={t('idiom.title')} subtitle={t('idiom.description')} variant="card" />
        <div style={{ padding: '2rem' }}>
        {!gameStarted ? (
          <div className="w-100 d-flex justify-content-center">
            <div
              className="shadow-lg p-5 border-0"
              style={{
                borderRadius: '24px',
                background: 'linear-gradient(145deg, #eff6ff, #dbeafe)',
                maxWidth: '850px',
                width: '100%',
              }}
            >
              <div className="text-center mb-4">
                <h3
                  className="mb-3"
                  style={{
                    fontSize: '2rem',
                    fontWeight: '700',
                    background: 'linear-gradient(135deg, #a855f7, #ec4899)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text',
                  }}
                >
                  选择难度级别
                </h3>
                <p className="text-secondary" style={{ fontSize: '1rem' }}>选择适合你的挑战水平</p>
              </div>

              {/* Statistics Display */}
              {gamesPlayed > 0 && (
                <div
                  className="mb-12 p-10 rounded-3xl shadow-2xl"
                  style={{
                    background: 'linear-gradient(135deg, #d1fae5, #a7f3d0)',
                    border: '4px solid #34d399',
                  }}
                >
                  <div className="flex justify-around text-center">
                    <div>
                      <div className="text-5xl font-extrabold text-green-700">{gamesPlayed}</div>
                      <div className="text-xl font-semibold text-green-600 mt-2">游戏数</div>
                    </div>
                    <div>
                      <div className="text-5xl font-extrabold text-blue-700">{totalScore}</div>
                      <div className="text-xl font-semibold text-blue-600 mt-2">总分</div>
                    </div>
                    <div>
                      <div className="text-5xl font-extrabold text-purple-700">
                        {gamesPlayed > 0 ? Math.round(totalScore / gamesPlayed) : 0}
                      </div>
                      <div className="text-xl font-semibold text-purple-600 mt-2">平均分</div>
                    </div>
                  </div>
                </div>
              )}

              <div className="row g-3 mb-4">
                {Object.values(Difficulty).map((diff) => (
                  <div key={diff} className="col-6 col-md-3">
                    <button
                      onClick={() => setDifficulty(diff)}
                      className={`btn w-100 py-2 fw-bold position-relative`}
                      style={{
                        fontSize: '1.1rem',
                        borderColor: difficulty === diff ? '#6c757d' : '#6c757d',
                        backgroundColor: difficulty === diff ? '#6c757d' : 'transparent',
                        color: difficulty === diff ? '#ffffff' : '#6c757d',
                        borderWidth: '2px',
                        borderStyle: 'solid',
                      }}
                    >
                      {difficulty === diff && (
                        <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-success">
                          ✓
                        </span>
                      )}
                      {DIFFICULTY_LABELS[diff]}
                    </button>
                  </div>
                ))}
              </div>

              <button
                onClick={handleStart}
                disabled={loading}
                className="btn btn-success w-100 py-3 fw-bold"
                style={{
                  fontSize: '1.1rem',
                }}
              >
                {loading ? '加载中...' : '🚀 开始游戏'}
              </button>
            </div>
          </div>
        ) : (
          <div className="max-w-5xl mx-auto">
            {error && (
              <div className="bg-red-100 border-2 border-red-400 text-red-700 px-6 py-4 rounded-lg mb-6 shadow-lg">
                <strong>错误：</strong> {error}
              </div>
            )}

            {/* Game Result Modal */}
            {gameResult && (
              <div className="fixed inset-0 flex items-center justify-center z-50 p-4">
                <div
                  className="max-w-4xl w-full mx-4 border-4"
                  style={{
                    borderRadius: '32px',
                    background: gameResult.allQuestionsCompleted
                      ? 'linear-gradient(145deg, #ffffff, #fef3c7, #fde68a)'
                      : gameResult.isCorrect
                      ? 'linear-gradient(145deg, #ffffff, #d1fae5, #a7f3d0)'
                      : 'linear-gradient(145deg, #ffffff, #fecaca, #fca5a5)',
                    borderColor: gameResult.allQuestionsCompleted
                      ? 'rgba(245, 158, 11, 0.5)'
                      : gameResult.isCorrect ? 'rgba(16, 185, 129, 0.5)' : 'rgba(239, 68, 68, 0.5)',
                    padding: '2rem 1.5rem',
                    boxShadow: 'none',
                  }}
                >
                  <div className="text-center">
                    <div className="mb-6" style={{ fontSize: '7.2rem', lineHeight: '1' }}>
                      {gameResult.allQuestionsCompleted ? '🎊' : gameResult.isCorrect ? '🎉' : '😔'}
                    </div>
                    <h3
                      className={`font-extrabold mb-6 ${
                        gameResult.allQuestionsCompleted ? 'text-yellow-600' : gameResult.isCorrect ? 'text-green-600' : 'text-red-600'
                      }`}
                      style={{ fontSize: '4.8rem', lineHeight: '1.1' }}
                    >
                      {gameResult.allQuestionsCompleted ? '恭喜完成！' : gameResult.isCorrect ? '正确！' : '不正确'}
                    </h3>
                    {gameResult.score !== undefined && (
                      <div className="font-extrabold text-blue-600 mb-6" style={{ fontSize: '3.6rem', lineHeight: '1.2' }}>
                        得分: +{gameResult.score}
                      </div>
                    )}
                    {gameResult.meaning && (
                      <div className="mt-6 p-6 bg-blue-50 rounded-lg text-left">
                        <p className="font-extrabold text-blue-800 mb-3" style={{ fontSize: '1.8rem', lineHeight: '1.3' }}>成语含义:</p>
                        <p className="text-gray-700 leading-relaxed" style={{ fontSize: '1.5rem', lineHeight: '1.5' }}>{gameResult.meaning}</p>
                      </div>
                    )}
                    {gameResult.example && (
                      <div className="mt-6 p-6 bg-green-50 rounded-lg text-left">
                        <p className="font-extrabold text-green-800 mb-3" style={{ fontSize: '1.8rem', lineHeight: '1.3' }}>用法示例:</p>
                        <p className="text-gray-700 leading-relaxed" style={{ fontSize: '1.5rem', lineHeight: '1.5' }}>{gameResult.example}</p>
                      </div>
                    )}
                    {gameResult.feedback && (
                      <p className="mt-6 text-gray-700 leading-relaxed" style={{ fontSize: '1.5rem', lineHeight: '1.5' }}>{gameResult.feedback}</p>
                    )}

                    {/* Quiz Completion Actions */}
                    {gameResult.allQuestionsCompleted ? (
                      <div className="mt-8 space-y-4">
                        <div className="p-6 bg-yellow-50 rounded-lg">
                          <p className="text-xl font-bold text-yellow-800 mb-2">🏆 您已完成所有 {DIFFICULTY_LABELS[difficulty]} 难度的题目！</p>
                          <p className="text-lg text-gray-700">总得分: {totalScore} | 完成题数: {gamesPlayed}</p>
                        </div>
                        <div className="flex gap-4 justify-center flex-wrap">
                          <button
                            onClick={handleRestartQuiz}
                            className="btn btn-warning btn-lg px-6 py-3 fw-bold"
                            style={{ fontSize: '1.2rem' }}
                          >
                            🔄 重新开始挑战
                          </button>
                          <button
                            onClick={handleBackToMenu}
                            className="btn btn-secondary btn-lg px-6 py-3 fw-bold"
                            style={{ fontSize: '1.2rem' }}
                          >
                            📋 返回主菜单
                          </button>
                        </div>
                      </div>
                    ) : (
                      <p className="text-gray-500 mt-6" style={{ fontSize: '1.2rem', lineHeight: '1.4' }}>
                        {gameResult.isCorrect ? '3秒后进入下一题...' : '3秒后重新开始...'}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {question && !gameResult ? (
              <div>
                {/* Top Stats Bar */}
                <div className="mb-6 shadow-xl p-6 flex justify-between items-center border-4" style={{
                  borderRadius: '32px',
                  background: 'linear-gradient(145deg, #bfdbfe, #ddd6fe, #e9d5ff)',
                  borderColor: 'rgba(139, 92, 246, 0.3)',
                }}>
                  <button
                    onClick={handleBackToMenu}
                    className="btn btn-primary px-3 py-2 shadow"
                  >
                    ← 返回菜单
                  </button>
                  <div className="text-center px-6">
                    <div className="text-lg font-semibold text-gray-700">当前得分</div>
                    <div className="text-3xl font-extrabold text-blue-600">{totalScore}</div>
                  </div>
                  <div className="text-center px-6">
                    <div className="text-lg font-semibold text-gray-700">游戏数</div>
                    <div className="text-3xl font-extrabold text-purple-600">{gamesPlayed}</div>
                  </div>
                </div>

                <IdiomGame
                  scrambledCharacters={question.scrambledCharacters}
                  correctAnswer={question.correctAnswer}
                  timeLimit={timeLimit}
                  onSubmit={handleSubmit}
                  onTimeout={handleTimeout}
                  onHintRequest={handleHintRequest}
                  hint={hint}
                  hintLoading={hintLoading}
                  difficulty={DIFFICULTY_LABELS[difficulty]}
                />
              </div>
            ) : !gameResult && (
              <div className="text-center py-12 bg-gradient-to-br from-blue-50 to-cyan-50 rounded-lg shadow-lg border-2 border-blue-100">
                <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600 mx-auto"></div>
                <p className="mt-6 text-gray-600 text-lg">加载游戏中...</p>
              </div>
            )}
          </div>
        )}
        </div>
      </div>
    </div>
  );
};

export default IdiomGamePage;
