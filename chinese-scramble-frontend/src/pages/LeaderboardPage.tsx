import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Header from '../components/layout/Header';
import { Difficulty, DIFFICULTY_LABELS } from '../constants/difficulties';
import { useLeaderboard, LeaderboardEntry } from '../hooks/useLeaderboard';

export const LeaderboardPage: React.FC = () => {
  const { t } = useTranslation();
  const { loading, error, getTopPlayers } = useLeaderboard();

  const [gameType, setGameType] = useState<'IDIOM' | 'SENTENCE'>('IDIOM');
  const [difficulty, setDifficulty] = useState<Difficulty>(Difficulty.EASY);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [limit, setLimit] = useState(10);

  useEffect(() => {
    loadLeaderboard();
  }, [gameType, difficulty, limit]);

  const loadLeaderboard = async () => {
    try {
      const data = await getTopPlayers(gameType, difficulty, limit);
      setLeaderboard(data);
    } catch (err) {
      console.error('Failed to load leaderboard:', err);
    }
  };

  const getRankBadgeColor = (rank: number) => {
    switch (rank) {
      case 1:
        return 'bg-gradient-to-r from-yellow-400 to-yellow-600 text-white';
      case 2:
        return 'bg-gradient-to-r from-gray-300 to-gray-400 text-gray-800';
      case 3:
        return 'bg-gradient-to-r from-orange-400 to-orange-600 text-white';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const getRankEmoji = (rank: number) => {
    switch (rank) {
      case 1:
        return '🥇';
      case 2:
        return '🥈';
      case 3:
        return '🥉';
      default:
        return '👤';
    }
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
        background: 'linear-gradient(to bottom right, #fae8ff, #f3e8ff, #e9d5ff, #d8b4fe)',
        borderRadius: '24px',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        overflow: 'hidden'
      }}>
        <Header title={t('leaderboard.title')} subtitle="查看顶尖玩家排名" variant="card" />
        <div style={{ padding: '2rem' }}>
          {/* Filters Section */}
          <div
            className="shadow-lg p-8 mb-8 border-3"
            style={{
              borderRadius: '24px',
              background: 'linear-gradient(145deg, #eff6ff, #dbeafe)',
              borderColor: 'rgba(59, 130, 246, 0.3)',
            }}
          >
            <h2 className="text-2xl font-bold mb-6 text-gray-800" style={{ marginTop: '0.5rem' }}>筛选条件</h2>

            {/* Game Type Selection */}
            <div style={{ marginBottom: '2rem', paddingTop: '0.5rem' }}>
              <label style={{ display: 'block', fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.75rem', color: '#374151' }}>游戏类型</label>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div
                  role="button"
                  tabIndex={0}
                  onClick={() => setGameType('IDIOM')}
                  onKeyPress={(e) => e.key === 'Enter' && setGameType('IDIOM')}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: gameType === 'IDIOM' ? '#16a34a' : 'transparent',
                    color: gameType === 'IDIOM' ? 'white' : '#16a34a',
                    border: '2px solid #16a34a',
                    borderRadius: '0.375rem',
                    fontWeight: '500',
                    fontSize: '1rem',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
                    outline: 'none',
                    userSelect: 'none',
                  }}
                >
                  🎯 成语游戏
                </div>
                <div
                  role="button"
                  tabIndex={0}
                  onClick={() => setGameType('SENTENCE')}
                  onKeyPress={(e) => e.key === 'Enter' && setGameType('SENTENCE')}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: gameType === 'SENTENCE' ? '#16a34a' : 'transparent',
                    color: gameType === 'SENTENCE' ? 'white' : '#16a34a',
                    border: '2px solid #16a34a',
                    borderRadius: '0.375rem',
                    fontWeight: '500',
                    fontSize: '1rem',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
                    outline: 'none',
                    userSelect: 'none',
                  }}
                >
                  📝 造句游戏
                </div>
              </div>
            </div>

            {/* Difficulty Selection */}
            <div style={{ marginBottom: '2rem' }}>
              <label style={{ display: 'block', fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.75rem', color: '#374151' }}>难度级别</label>
              <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                {Object.values(Difficulty).map((diff) => (
                  <div
                    key={diff}
                    role="button"
                    tabIndex={0}
                    onClick={() => setDifficulty(diff)}
                    onKeyPress={(e) => e.key === 'Enter' && setDifficulty(diff)}
                    style={{
                      flex: 1,
                      padding: '0.5rem 1rem',
                      backgroundColor: difficulty === diff ? '#16a34a' : 'transparent',
                      color: difficulty === diff ? 'white' : '#16a34a',
                      border: '2px solid #16a34a',
                      borderRadius: '0.375rem',
                      fontWeight: '500',
                      fontSize: '1rem',
                      cursor: 'pointer',
                      transition: 'all 0.2s',
                      boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
                      outline: 'none',
                      userSelect: 'none',
                      textAlign: 'center',
                    }}
                  >
                    {DIFFICULTY_LABELS[diff]}
                  </div>
                ))}
              </div>
            </div>

            {/* Limit Selection */}
            <div>
              <label className="block text-xl font-bold mb-3 text-gray-700">显示数量</label>
              <select
                value={limit}
                onChange={(e) => setLimit(Number(e.target.value))}
                className="form-select px-3 py-2 border-2 border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                style={{
                  background: '#ffffff',
                  width: 'auto',
                  minWidth: '150px',
                }}
              >
                <option value={10}>前 10 名</option>
                <option value={25}>前 25 名</option>
                <option value={50}>前 50 名</option>
                <option value={100}>前 100 名</option>
              </select>
            </div>
          </div>

          {/* Leaderboard Table */}
          <div
            className="shadow-lg overflow-hidden border-3"
            style={{
              borderRadius: '24px',
              background: '#ffffff',
              borderColor: 'rgba(168, 85, 247, 0.3)',
            }}
          >
            <div
              className="p-6"
              style={{
                background: 'linear-gradient(135deg, #3b82f6, #2563eb)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div style={{ flex: 1 }}>
                <h2 className="text-3xl font-bold text-white text-center">
                  🏆 排行榜
                </h2>
                <p className="text-center text-lg font-medium mt-2" style={{ color: '#dbeafe' }}>
                  {gameType === 'IDIOM' ? '成语游戏' : '造句游戏'} - {DIFFICULTY_LABELS[difficulty]}
                </p>
              </div>
              {!loading && (
                <button
                  onClick={loadLeaderboard}
                  className="btn btn-light px-3 py-2 shadow"
                  style={{
                    fontSize: '0.9rem',
                    whiteSpace: 'nowrap',
                  }}
                >
                  🔄 刷新
                </button>
              )}
            </div>

            {loading && (
              <div className="text-center py-12">
                <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-600 mx-auto"></div>
                <p className="mt-4 text-gray-600">加载排行榜中...</p>
              </div>
            )}

            {error && (
              <div className="bg-red-100 border-2 border-red-400 text-red-700 px-6 py-4 m-4 rounded-lg">
                <strong>错误：</strong> {error}
              </div>
            )}

            {!loading && !error && leaderboard.length === 0 && (
              <div className="text-center py-12">
                <div className="text-6xl mb-4">📊</div>
                <p className="text-gray-600 text-lg">暂无排行榜数据</p>
                <p className="text-gray-500 text-sm mt-2">开始游戏后，您的分数将出现在这里！</p>
              </div>
            )}

            {!loading && !error && leaderboard.length > 0 && (
              <div className="overflow-x-auto">
                <table className="w-full table-fixed">
                  <thead className="bg-gray-50 border-b-2 border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-bold text-gray-700 uppercase w-24">
                        排名
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-bold text-gray-700 uppercase">
                        玩家
                      </th>
                      <th className="px-4 py-3 text-center text-xs font-bold text-gray-700 uppercase w-28">
                        最高分
                      </th>
                      <th className="px-4 py-3 text-center text-xs font-bold text-gray-700 uppercase w-28">
                        平均分
                      </th>
                      <th className="px-4 py-3 text-center text-xs font-bold text-gray-700 uppercase w-24">
                        游戏数
                      </th>
                      <th className="px-4 py-3 text-center text-xs font-bold text-gray-700 uppercase w-32">
                        最后游戏
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {leaderboard.map((entry, index) => (
                      <tr
                        key={entry.playerId}
                        className={`hover:bg-gray-50 transition-colors ${
                          entry.rank <= 3 ? 'bg-yellow-50' : ''
                        }`}
                      >
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <span className="text-base">{getRankEmoji(entry.rank)}</span>
                            <span
                              className={`px-2 py-1 rounded-full font-bold text-xs ${getRankBadgeColor(
                                entry.rank
                              )}`}
                            >
                              #{entry.rank}
                            </span>
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="text-sm font-semibold text-gray-900 truncate">
                            {entry.username}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-center">
                          <span className="text-base font-bold text-purple-600">
                            {entry.bestScore}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-center">
                          <span className="text-sm font-semibold text-blue-600">
                            {Math.round(entry.averageScore)}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-center">
                          <span className="text-gray-700 text-sm">{entry.totalGames}</span>
                        </td>
                        <td className="px-4 py-3 text-center text-xs text-gray-500">
                          {new Date(entry.lastPlayed).toLocaleDateString('zh-CN')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default LeaderboardPage;
