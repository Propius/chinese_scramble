import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Header from '../components/layout/Header';
import { useLeaderboard, PlayerRanking } from '../hooks/useLeaderboard';
import { DIFFICULTY_LABELS } from '../constants/difficulties';
import { usernameUtils } from '../utils/usernameUtils';

export const StatisticsPage: React.FC = () => {
  const { t } = useTranslation();
  const { loading, error, getPlayerRankings } = useLeaderboard();
  const [rankings, setRankings] = useState<PlayerRanking[]>([]);
  const [playerId] = useState(usernameUtils.getUsername() || 'Guest');

  useEffect(() => {
    loadPlayerStats();
  }, []);

  const loadPlayerStats = async () => {
    try {
      const data = await getPlayerRankings(playerId);
      setRankings(data);
    } catch (err) {
      console.error('Failed to load player statistics:', err);
    }
  };

  const getOverallStats = () => {
    if (rankings.length === 0) {
      return {
        totalGames: 0,
        totalScore: 0,
        averageScore: 0,
        bestRank: null,
      };
    }

    const totalGames = rankings.reduce((sum, r) => sum + r.totalGames, 0);
    const totalScore = rankings.reduce((sum, r) => sum + r.bestScore, 0);
    const bestRank = Math.min(...rankings.map((r) => r.rank));

    return {
      totalGames,
      totalScore,
      averageScore: totalGames > 0 ? Math.round(totalScore / rankings.length) : 0,
      bestRank: bestRank !== Infinity ? bestRank : null,
    };
  };

  const getGameTypeStats = (gameType: 'IDIOM' | 'SENTENCE') => {
    const filtered = rankings.filter((r) => r.gameType === gameType);
    if (filtered.length === 0) return null;

    const totalGames = filtered.reduce((sum, r) => sum + r.totalGames, 0);
    const totalScore = filtered.reduce((sum, r) => sum + r.bestScore, 0);
    const bestRank = Math.min(...filtered.map((r) => r.rank));
    const avgScore = Math.round(
      filtered.reduce((sum, r) => sum + r.averageScore, 0) / filtered.length
    );

    return {
      totalGames,
      totalScore,
      bestRank,
      avgScore,
      rankings: filtered,
    };
  };

  const stats = getOverallStats();
  const idiomStats = getGameTypeStats('IDIOM');
  const sentenceStats = getGameTypeStats('SENTENCE');

  const getRankBadge = (rank: number) => {
    if (rank === 1) return { emoji: '🥇', color: 'text-yellow-500', text: '金牌' };
    if (rank === 2) return { emoji: '🥈', color: 'text-gray-400', text: '银牌' };
    if (rank === 3) return { emoji: '🥉', color: 'text-orange-500', text: '铜牌' };
    if (rank <= 10) return { emoji: '🏅', color: 'text-blue-500', text: '前十名' };
    if (rank <= 50) return { emoji: '⭐', color: 'text-purple-500', text: '前五十名' };
    return { emoji: '👤', color: 'text-gray-500', text: `第 ${rank} 名` };
  };

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
        background: 'linear-gradient(to bottom right, #dbeafe, #e0e7ff, #ede9fe, #fae8ff)',
        borderRadius: '24px',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        overflow: 'hidden'
      }}>
        <Header title={t('statistics.title')} subtitle="查看你的游戏统计数据" variant="card" />
        <div style={{ padding: '2rem' }}>
        <div className="w-100 mx-auto" style={{ maxWidth: '1200px' }}>
          {loading && (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-600 mx-auto"></div>
              <p className="mt-4 text-gray-600">加载统计数据中...</p>
            </div>
          )}

          {error && (
            <div className="bg-red-100 border-2 border-red-400 text-red-700 px-6 py-4 rounded-lg mb-6">
              <strong>错误：</strong> {error}
            </div>
          )}

          {!loading && !error && rankings.length === 0 && (
            <div className="d-flex justify-content-center">
              <div
                className="shadow-lg p-5 text-center border-0"
                style={{
                  borderRadius: '24px',
                  background: 'linear-gradient(145deg, #ffffff, #fef3c7)',
                  maxWidth: '600px',
                  width: '100%',
                }}
              >
                <div className="mb-4" style={{ fontSize: '4rem' }}>🎮</div>
                <h3 className="mb-3" style={{ fontSize: '1.75rem', fontWeight: 'bold' }}>还没有统计数据</h3>
                <p className="mb-4" style={{ fontSize: '1.1rem', color: '#4b5563' }}>开始玩游戏来积累你的统计数据吧！</p>
                <a
                  href="/"
                  className="btn btn-primary px-4 py-2 shadow text-decoration-none"
                >
                  开始游戏
                </a>
              </div>
            </div>
          )}

          {!loading && !error && rankings.length > 0 && (
            <>
              {/* Overall Summary Cards */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
                <div className="rounded-xl shadow-md p-5 border-2" style={{
                  background: 'linear-gradient(145deg, #faf5ff, #f3e8ff)',
                  borderColor: '#c084fc',
                }}>
                  <div className="text-3xl mb-2">🎮</div>
                  <div className="text-2xl font-bold text-purple-600">{stats.totalGames}</div>
                  <div className="text-gray-600 text-sm font-medium">总游戏数</div>
                </div>

                <div className="rounded-xl shadow-md p-5 border-2" style={{
                  background: 'linear-gradient(145deg, #eff6ff, #dbeafe)',
                  borderColor: '#60a5fa',
                }}>
                  <div className="text-3xl mb-2">⭐</div>
                  <div className="text-2xl font-bold text-blue-600">{stats.totalScore}</div>
                  <div className="text-gray-600 text-sm font-medium">总分</div>
                </div>

                <div className="rounded-xl shadow-md p-5 border-2" style={{
                  background: 'linear-gradient(145deg, #f0fdf4, #dcfce7)',
                  borderColor: '#4ade80',
                }}>
                  <div className="text-3xl mb-2">📊</div>
                  <div className="text-2xl font-bold text-green-600">{stats.averageScore}</div>
                  <div className="text-gray-600 text-sm font-medium">平均最高分</div>
                </div>

                <div className="rounded-xl shadow-md p-5 border-2" style={{
                  background: 'linear-gradient(145deg, #fefce8, #fef9c3)',
                  borderColor: '#facc15',
                }}>
                  <div className="text-3xl mb-2">🏆</div>
                  <div className="text-2xl font-bold text-yellow-600">
                    {stats.bestRank ? `#${stats.bestRank}` : 'N/A'}
                  </div>
                  <div className="text-gray-600 text-sm font-medium">最佳排名</div>
                </div>
              </div>

              {/* Idiom Game Stats */}
              {idiomStats && (
                <div
                  className="shadow-md p-6 mb-6 border-2 rounded-xl"
                  style={{
                    background: 'linear-gradient(145deg, #ffffff, #dbeafe)',
                    borderColor: '#60a5fa',
                  }}
                >
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-2xl font-bold text-gray-900">🎯 成语游戏统计</h3>
                    <div className="text-sm font-medium text-gray-600">
                      {idiomStats.totalGames} 场游戏 | 平均分: {idiomStats.avgScore}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                    {idiomStats.rankings.map((ranking) => {
                      const badge = getRankBadge(ranking.rank);
                      return (
                        <div
                          key={`${ranking.gameType}-${ranking.difficulty}`}
                          className="border-2 border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-bold text-gray-700">
                              {DIFFICULTY_LABELS[ranking.difficulty.toUpperCase() as keyof typeof DIFFICULTY_LABELS]}
                            </span>
                            <span className={`text-2xl ${badge.color}`}>{badge.emoji}</span>
                          </div>
                          <div className="text-center py-2">
                            <div className="text-2xl font-bold text-purple-600">
                              #{ranking.rank}
                            </div>
                            <div className="text-xs text-gray-500">{badge.text}</div>
                          </div>
                          <div className="border-t border-gray-200 pt-2 mt-2">
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">最高分:</span>
                              <span className="font-bold text-gray-800">{ranking.bestScore}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">平均分:</span>
                              <span className="font-bold text-gray-800">
                                {Math.round(ranking.averageScore)}
                              </span>
                            </div>
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">游戏数:</span>
                              <span className="font-bold text-gray-800">{ranking.totalGames}</span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Sentence Game Stats */}
              {sentenceStats && (
                <div
                  className="shadow-md p-6 mb-6 border-2 rounded-xl"
                  style={{
                    background: 'linear-gradient(145deg, #ffffff, #d1fae5)',
                    borderColor: '#4ade80',
                  }}
                >
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-2xl font-bold text-gray-900">📝 造句游戏统计</h3>
                    <div className="text-sm font-medium text-gray-600">
                      {sentenceStats.totalGames} 场游戏 | 平均分: {sentenceStats.avgScore}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                    {sentenceStats.rankings.map((ranking) => {
                      const badge = getRankBadge(ranking.rank);
                      return (
                        <div
                          key={`${ranking.gameType}-${ranking.difficulty}`}
                          className="border-2 border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-bold text-gray-700">
                              {DIFFICULTY_LABELS[ranking.difficulty.toUpperCase() as keyof typeof DIFFICULTY_LABELS]}
                            </span>
                            <span className={`text-2xl ${badge.color}`}>{badge.emoji}</span>
                          </div>
                          <div className="text-center py-2">
                            <div className="text-2xl font-bold text-purple-600">
                              #{ranking.rank}
                            </div>
                            <div className="text-xs text-gray-500">{badge.text}</div>
                          </div>
                          <div className="border-t border-gray-200 pt-2 mt-2">
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">最高分:</span>
                              <span className="font-bold text-gray-800">{ranking.bestScore}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">平均分:</span>
                              <span className="font-bold text-gray-800">
                                {Math.round(ranking.averageScore)}
                              </span>
                            </div>
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-600">游戏数:</span>
                              <span className="font-bold text-gray-800">{ranking.totalGames}</span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Performance Tips */}
              <div
                className="shadow-md p-6 border-2 rounded-xl"
                style={{
                  background: 'linear-gradient(135deg, #fae8ff, #fbcfe8)',
                  borderColor: '#f0abfc',
                }}
              >
                <h3 className="text-2xl font-bold text-gray-900 mb-4">💡 提升技巧</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl mb-2">🎯</div>
                    <h4 className="font-bold text-gray-800 mb-2">专注练习</h4>
                    <p className="text-sm text-gray-600">
                      选择一个难度等级，持续练习直到精通，再挑战更高难度
                    </p>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl mb-2">💡</div>
                    <h4 className="font-bold text-gray-800 mb-2">善用提示</h4>
                    <p className="text-sm text-gray-600">
                      一级提示最划算，能帮助理解而不直接给答案
                    </p>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl mb-2">⏱️</div>
                    <h4 className="font-bold text-gray-800 mb-2">时间管理</h4>
                    <p className="text-sm text-gray-600">
                      保持冷静，合理分配时间，不要急于求成
                    </p>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl mb-2">📚</div>
                    <h4 className="font-bold text-gray-800 mb-2">学习积累</h4>
                    <p className="text-sm text-gray-600">
                      每次游戏后复习成语和句型，加深记忆
                    </p>
                  </div>
                </div>
              </div>

              {/* Refresh Button */}
              <div className="text-center" style={{ marginTop: '3rem' }}>
                <button
                  onClick={loadPlayerStats}
                  disabled={loading}
                  className="btn btn-primary px-3 py-2 shadow"
                >
                  🔄 刷新统计数据
                </button>
              </div>
            </>
          )}
        </div>
        </div>
      </div>
    </div>
  );
};

export default StatisticsPage;
