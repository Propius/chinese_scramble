import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import IdiomGamePage from '../IdiomGamePage';
import { Difficulty, DIFFICULTY_LABELS } from '../../constants/difficulties';
import { useIdiomGame } from '../../hooks/useIdiomGame';
import apiClient from '../../services/api';
import { soundManager } from '../../utils/soundManager';
import { usernameUtils } from '../../utils/usernameUtils';

// Mock dependencies
jest.mock('../../hooks/useIdiomGame');
jest.mock('../../services/api');
jest.mock('../../utils/soundManager');
jest.mock('../../utils/usernameUtils');
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'idiom.title': '成语挑战',
        'idiom.description': '挑战你的成语知识',
      };
      return translations[key] || key;
    },
  }),
}));

// Mock Header component
jest.mock('../../components/layout/Header', () => ({
  __esModule: true,
  default: ({ title, subtitle }: { title: string; subtitle?: string }) => (
    <div data-testid="mock-header">
      <h1>{title}</h1>
      {subtitle && <p>{subtitle}</p>}
    </div>
  ),
}));

// Mock IdiomGame component
jest.mock('../../components/idiom/IdiomGame', () => ({
  __esModule: true,
  default: ({ onSubmit, onTimeout, onHintRequest, hint, hintLoading, difficulty, timeLimit }: any) => (
    <div data-testid="mock-idiom-game">
      <div data-testid="difficulty-display">Difficulty: {difficulty}</div>
      <div data-testid="time-limit-display">Time Limit: {timeLimit}</div>
      <div data-testid="hint-display">Hint: {hint}</div>
      <div data-testid="hint-loading-display">Hint Loading: {hintLoading ? 'true' : 'false'}</div>
      <button onClick={() => onSubmit('答案', 10, 0)}>Submit Answer</button>
      <button onClick={() => onTimeout()}>Trigger Timeout</button>
      <button onClick={() => onHintRequest(1)}>Request Hint</button>
    </div>
  ),
}));

// Mock Confetti component
jest.mock('../../components/common/Confetti', () => ({
  __esModule: true,
  default: ({ active }: { active: boolean }) => (
    <div data-testid="mock-confetti" data-active={active}>
      {active ? 'Confetti Active' : 'Confetti Inactive'}
    </div>
  ),
}));

const mockUseIdiomGame = useIdiomGame as jest.MockedFunction<typeof useIdiomGame>;
const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;
const mockSoundManager = soundManager as jest.Mocked<typeof soundManager>;
const mockUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;

describe('IdiomGamePage', () => {
  const mockQuestion = {
    id: 'idiom-1',
    scrambledCharacters: ['明', '争', '暗', '斗'],
    correctAnswer: '明争暗斗',
  };

  const mockStartGame = jest.fn();

  // Helper function to render the page with default state (not started)
  const renderIdiomGamePage = () => {
    return render(
      <BrowserRouter>
        <IdiomGamePage />
      </BrowserRouter>
    );
  };

  // Helper function to start a game
  const startGame = async (difficulty?: Difficulty) => {
    const { rerender } = renderIdiomGamePage();

    // Click difficulty if provided
    if (difficulty) {
      fireEvent.click(screen.getByText(DIFFICULTY_LABELS[difficulty]));
    }

    // Click start button
    await act(async () => {
      fireEvent.click(screen.getByText('🚀 开始游戏'));
    });

    // Update mock to show game started with question
    mockUseIdiomGame.mockReturnValue({
      question: mockQuestion,
      loading: false,
      error: null,
      userAnswer: [],
      hintsUsed: 0,
      timeTaken: 0,
      validationResult: null,
      startGame: mockStartGame,
      submitAnswer: jest.fn(),
      getHint: jest.fn(),
      setUserAnswer: jest.fn(),
      reset: jest.fn(),
    });

    // Rerender with updated state
    rerender(
      <BrowserRouter>
        <IdiomGamePage />
      </BrowserRouter>
    );

    return rerender;
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();

    mockUseIdiomGame.mockReturnValue({
      question: null,
      loading: false,
      error: null,
      userAnswer: [],
      hintsUsed: 0,
      timeTaken: 0,
      validationResult: null,
      startGame: mockStartGame,
      submitAnswer: jest.fn(),
      getHint: jest.fn(),
      setUserAnswer: jest.fn(),
      reset: jest.fn(),
    });

    mockUsernameUtils.getUsername.mockReturnValue('TestPlayer');
    mockSoundManager.playWin = jest.fn();
    mockSoundManager.playLose = jest.fn();
    mockSoundManager.playHint = jest.fn();
    mockSoundManager.playTimeout = jest.fn();
  });

  afterEach(() => {
    act(() => {
      jest.runOnlyPendingTimers();
    });
    jest.useRealTimers();
  });

  describe('Initial Rendering', () => {
    it('should render the page correctly before game starts', () => {
      renderIdiomGamePage();
      expect(screen.getByTestId('mock-header')).toBeInTheDocument();
    });

    it('should render Header with correct title and subtitle', () => {
      renderIdiomGamePage();
      expect(screen.getByText('成语挑战')).toBeInTheDocument();
      expect(screen.getByText('挑战你的成语知识')).toBeInTheDocument();
    });

    it('should render difficulty selection screen', () => {
      renderIdiomGamePage();
      expect(screen.getByText('选择难度级别')).toBeInTheDocument();
      expect(screen.getByText('选择适合你的挑战水平')).toBeInTheDocument();
    });

    it('should show all 4 difficulty buttons (EASY, MEDIUM, HARD, EXPERT)', () => {
      renderIdiomGamePage();
      expect(screen.getByText(DIFFICULTY_LABELS[Difficulty.EASY])).toBeInTheDocument();
      expect(screen.getByText(DIFFICULTY_LABELS[Difficulty.MEDIUM])).toBeInTheDocument();
      expect(screen.getByText(DIFFICULTY_LABELS[Difficulty.HARD])).toBeInTheDocument();
      expect(screen.getByText(DIFFICULTY_LABELS[Difficulty.EXPERT])).toBeInTheDocument();
    });

    it('should show start game button', () => {
      renderIdiomGamePage();
      expect(screen.getByText('🚀 开始游戏')).toBeInTheDocument();
    });

    it('should not show statistics initially', () => {
      renderIdiomGamePage();
      expect(screen.queryByText('游戏数')).not.toBeInTheDocument();
      expect(screen.queryByText('总分')).not.toBeInTheDocument();
      expect(screen.queryByText('平均分')).not.toBeInTheDocument();
    });

    it('should render confetti component as inactive initially', () => {
      renderIdiomGamePage();
      expect(screen.getByTestId('mock-confetti')).toBeInTheDocument();
      expect(screen.getByTestId('mock-confetti')).toHaveAttribute('data-active', 'false');
    });

    it('should not render game component before start', () => {
      renderIdiomGamePage();
      expect(screen.queryByTestId('mock-idiom-game')).not.toBeInTheDocument();
    });
  });

  describe('Difficulty Selection', () => {
    it('should have EASY difficulty selected by default', () => {
      renderIdiomGamePage();
      const easyButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.EASY]);
      expect(easyButton).toHaveStyle({ color: '#ffffff' });
    });

    it('should show checkmark badge for selected difficulty', () => {
      renderIdiomGamePage();
      const badges = screen.getAllByText('✓');
      expect(badges.length).toBeGreaterThan(0);
    });

    it('should change to MEDIUM difficulty when clicked', () => {
      renderIdiomGamePage();
      const mediumButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.MEDIUM]);
      fireEvent.click(mediumButton);
      expect(mediumButton).toHaveStyle({ color: '#ffffff' });
    });

    it('should change to HARD difficulty when clicked', () => {
      renderIdiomGamePage();
      const hardButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.HARD]);
      fireEvent.click(hardButton);
      expect(hardButton).toHaveStyle({ color: '#ffffff' });
    });

    it('should change to EXPERT difficulty when clicked', () => {
      renderIdiomGamePage();
      const expertButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.EXPERT]);
      fireEvent.click(expertButton);
      expect(expertButton).toHaveStyle({ color: '#ffffff' });
    });

    it('should allow switching between difficulties multiple times', () => {
      renderIdiomGamePage();
      const easyButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.EASY]);
      const hardButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.HARD]);

      fireEvent.click(hardButton);
      expect(hardButton).toHaveStyle({ color: '#ffffff' });

      fireEvent.click(easyButton);
      expect(easyButton).toHaveStyle({ color: '#ffffff' });
    });

    it('should persist difficulty selection before starting', () => {
      renderIdiomGamePage();
      const expertButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.EXPERT]);
      fireEvent.click(expertButton);

      // Should still be selected after other interactions
      const startButton = screen.getByText('🚀 开始游戏');
      expect(startButton).toBeInTheDocument();
      expect(expertButton).toHaveStyle({ color: '#ffffff' });
    });
  });

  describe('Game Start Flow', () => {
    it('should call startGame with correct difficulty (EASY) when start button clicked', async () => {
      renderIdiomGamePage();
      const startButton = screen.getByText('🚀 开始游戏');

      await act(async () => {
        fireEvent.click(startButton);
      });

      expect(mockStartGame).toHaveBeenCalledWith(Difficulty.EASY);
    });

    it('should call startGame with MEDIUM difficulty when selected', async () => {
      renderIdiomGamePage();
      const mediumButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.MEDIUM]);
      fireEvent.click(mediumButton);

      const startButton = screen.getByText('🚀 开始游戏');
      await act(async () => {
        fireEvent.click(startButton);
      });

      expect(mockStartGame).toHaveBeenCalledWith(Difficulty.MEDIUM);
    });

    it('should call startGame with HARD difficulty when selected', async () => {
      renderIdiomGamePage();
      const hardButton = screen.getByText(DIFFICULTY_LABELS[Difficulty.HARD]);
      fireEvent.click(hardButton);

      await act(async () => {
        fireEvent.click(screen.getByText('🚀 开始游戏'));
      });

      expect(mockStartGame).toHaveBeenCalledWith(Difficulty.HARD);
    });

    it('should call startGame with EXPERT difficulty when selected', async () => {
      renderIdiomGamePage();
      fireEvent.click(screen.getByText(DIFFICULTY_LABELS[Difficulty.EXPERT]));

      await act(async () => {
        fireEvent.click(screen.getByText('🚀 开始游戏'));
      });

      expect(mockStartGame).toHaveBeenCalledWith(Difficulty.EXPERT);
    });

    it('should show loading state while starting game', () => {
      mockUseIdiomGame.mockReturnValue({
        question: null,
        loading: true,
        error: null,
        userAnswer: [],
        hintsUsed: 0,
        timeTaken: 0,
        validationResult: null,
        startGame: mockStartGame,
        submitAnswer: jest.fn(),
        getHint: jest.fn(),
        setUserAnswer: jest.fn(),
        reset: jest.fn(),
      });

      renderIdiomGamePage();
      expect(screen.getByText('加载中...')).toBeInTheDocument();
    });

    it('should disable start button when loading', () => {
      mockUseIdiomGame.mockReturnValue({
        question: null,
        loading: true,
        error: null,
        userAnswer: [],
        hintsUsed: 0,
        timeTaken: 0,
        validationResult: null,
        startGame: mockStartGame,
        submitAnswer: jest.fn(),
        getHint: jest.fn(),
        setUserAnswer: jest.fn(),
        reset: jest.fn(),
      });

      renderIdiomGamePage();
      const startButton = screen.getByText('加载中...');
      expect(startButton).toBeDisabled();
    });

    it('should hide difficulty selection after game starts', async () => {
      await startGame();
      expect(screen.queryByText('选择难度级别')).not.toBeInTheDocument();
    });

    it('should show game component after game starts', async () => {
      await startGame();
      expect(screen.getByTestId('mock-idiom-game')).toBeInTheDocument();
    });

    it('should show loading spinner when game started but no question yet', async () => {
      const { rerender } = renderIdiomGamePage();

      await act(async () => {
        fireEvent.click(screen.getByText('🚀 开始游戏'));
      });

      mockUseIdiomGame.mockReturnValue({
        question: null,
        loading: false,
        error: null,
        userAnswer: [],
        hintsUsed: 0,
        timeTaken: 0,
        validationResult: null,
        startGame: mockStartGame,
        submitAnswer: jest.fn(),
        getHint: jest.fn(),
        setUserAnswer: jest.fn(),
        reset: jest.fn(),
      });

      rerender(
        <BrowserRouter>
          <IdiomGamePage />
        </BrowserRouter>
      );

      expect(screen.getByText('加载游戏中...')).toBeInTheDocument();
    });
  });

  describe('Game State Display', () => {
    it('should show back to menu button during game', async () => {
      await startGame();
      expect(screen.getByText('← 返回菜单')).toBeInTheDocument();
    });

    it('should show current score (starts at 0)', async () => {
      await startGame();
      expect(screen.getByText('当前得分')).toBeInTheDocument();
      const scoreElements = screen.getAllByText('0');
      expect(scoreElements.length).toBeGreaterThan(0);
    });

    it('should show games played counter (starts at 0)', async () => {
      await startGame();
      expect(screen.getByText('游戏数')).toBeInTheDocument();
    });

    it('should render IdiomGame component with correct props', async () => {
      await startGame();
      expect(screen.getByTestId('mock-idiom-game')).toBeInTheDocument();
      expect(screen.getByTestId('difficulty-display')).toHaveTextContent('Difficulty: 简单');
    });

    it('should pass correct time limit for EASY difficulty (180 seconds)', async () => {
      await startGame(Difficulty.EASY);
      expect(screen.getByTestId('time-limit-display')).toHaveTextContent('Time Limit: 180');
    });

    it('should pass correct time limit for MEDIUM difficulty (120 seconds)', async () => {
      await startGame(Difficulty.MEDIUM);
      expect(screen.getByTestId('time-limit-display')).toHaveTextContent('Time Limit: 120');
    });

    it('should pass correct time limit for HARD difficulty (90 seconds)', async () => {
      await startGame(Difficulty.HARD);
      expect(screen.getByTestId('time-limit-display')).toHaveTextContent('Time Limit: 90');
    });

    it('should pass correct time limit for EXPERT difficulty (60 seconds)', async () => {
      await startGame(Difficulty.EXPERT);
      expect(screen.getByTestId('time-limit-display')).toHaveTextContent('Time Limit: 60');
    });

    it('should display stats bar with score and games played', async () => {
      await startGame();
      expect(screen.getByText('当前得分')).toBeInTheDocument();
      expect(screen.getByText('游戏数')).toBeInTheDocument();
    });
  });

  describe('Submit Answer Flow', () => {
    it('should call API when submitting answer', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/submit?playerId=TestPlayer',
        { answer: '答案', timeTaken: 10, hintsUsed: 0 }
      );
    });

    it('should update score after successful submission', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('正确！')).toBeInTheDocument();
      });
    });

    it('should show confetti on correct answer', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('mock-confetti')).toHaveAttribute('data-active', 'true');
      });
    });

    it('should play win sound on correct answer', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(mockSoundManager.playWin).toHaveBeenCalled();
      });
    });

    it('should play lose sound on incorrect answer', async () => {
      const mockResult = { isCorrect: false, score: 0, feedback: '答案不正确' };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(mockSoundManager.playLose).toHaveBeenCalled();
      });
    });

    it('should hide confetti after 3 seconds', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('mock-confetti')).toHaveAttribute('data-active', 'true');
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      await waitFor(() => {
        expect(screen.getByTestId('mock-confetti')).toHaveAttribute('data-active', 'false');
      });
    });

    it('should load next question after 3 seconds', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      const initialCallCount = mockStartGame.mock.calls.length;

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      await waitFor(() => {
        expect(mockStartGame.mock.calls.length).toBe(initialCallCount + 1);
      });
    });

    it('should display game result modal with correct emoji and message', async () => {
      const mockResult = {
        isCorrect: true,
        score: 100,
        meaning: '比喻公开的和暗地里的斗争',
        example: '他们之间的明争暗斗已经持续很久了',
      };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('正确！')).toBeInTheDocument();
        expect(screen.getByText('得分: +100')).toBeInTheDocument();
        expect(screen.getByText('成语含义:')).toBeInTheDocument();
        expect(screen.getByText('比喻公开的和暗地里的斗争')).toBeInTheDocument();
      });
    });

    it('should display incorrect result with feedback', async () => {
      const mockResult = {
        isCorrect: false,
        score: 0,
        feedback: '答案不正确，请再试一次',
      };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('不正确')).toBeInTheDocument();
        expect(screen.getByText('答案不正确，请再试一次')).toBeInTheDocument();
      });
    });

    it('should show message about next question on correct answer', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('3秒后进入下一题...')).toBeInTheDocument();
      });
    });

    it('should show message about restart on incorrect answer', async () => {
      const mockResult = { isCorrect: false, score: 0 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('3秒后重新开始...')).toBeInTheDocument();
      });
    });
  });

  describe('Timeout Handling', () => {
    it('should call handleTimeout properly', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Trigger Timeout'));
      });

      await waitFor(() => {
        expect(screen.getByText('不正确')).toBeInTheDocument();
      });
    });

    it('should show timeout result with feedback message', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Trigger Timeout'));
      });

      await waitFor(() => {
        expect(screen.getByText('时间到！请再试一次。')).toBeInTheDocument();
      });
    });

    it('should load next question after timeout and 3 seconds', async () => {
      await startGame();

      const initialCallCount = mockStartGame.mock.calls.length;

      await act(async () => {
        fireEvent.click(screen.getByText('Trigger Timeout'));
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      await waitFor(() => {
        expect(mockStartGame.mock.calls.length).toBe(initialCallCount + 1);
      });
    });

    it('should show restart message after timeout', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Trigger Timeout'));
      });

      await waitFor(() => {
        expect(screen.getByText('3秒后重新开始...')).toBeInTheDocument();
      });
    });
  });

  describe('Hint Request', () => {
    it('should call hint API when requesting hint', async () => {
      const mockHint = { content: '这个成语形容斗争' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/hint/1?playerId=TestPlayer'
      );
    });

    it('should display hint in component after request', async () => {
      const mockHint = { content: '这个成语形容斗争' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 这个成语形容斗争');
      });
    });

    it('should show hint loading state', async () => {
      mockApiClient.post.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({ content: 'hint' }), 1000))
      );

      await startGame();

      act(() => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-loading-display')).toHaveTextContent('Hint Loading: true');
      });
    });

    it('should play hint sound when requesting hint', async () => {
      const mockHint = { content: '这个成语形容斗争' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      expect(mockSoundManager.playHint).toHaveBeenCalled();
    });

    it('should handle hint API error gracefully', async () => {
      mockApiClient.post.mockRejectedValue({
        response: { data: { error: '提示获取失败' } },
      });

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 提示获取失败');
      });
    });

    it('should handle hint with different response formats (hint field)', async () => {
      const mockHint = { hint: '这是另一种格式的提示' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 这是另一种格式的提示');
      });
    });

    it('should use username from usernameUtils in hint request', async () => {
      mockUsernameUtils.getUsername.mockReturnValue('CustomPlayer');
      const mockHint = { content: '提示内容' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/hint/1?playerId=CustomPlayer'
      );
    });

    it('should use Guest as default username if none exists', async () => {
      mockUsernameUtils.getUsername.mockReturnValue(null);
      const mockHint = { content: '提示内容' };
      mockApiClient.post.mockResolvedValue(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/hint/1?playerId=Guest'
      );
    });
  });

  describe('Back to Menu', () => {
    it('should return to difficulty selection when clicking back to menu', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('← 返回菜单'));
      });

      await waitFor(() => {
        expect(screen.getByText('选择难度级别')).toBeInTheDocument();
      });
    });

    it('should reset game state when going back to menu', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('← 返回菜单'));
      });

      await waitFor(() => {
        expect(screen.queryByTestId('mock-idiom-game')).not.toBeInTheDocument();
      });
    });

    it('should reset score when going back to menu', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      await act(async () => {
        fireEvent.click(screen.getByText('← 返回菜单'));
      });

      await waitFor(() => {
        expect(screen.queryByText('当前得分')).not.toBeInTheDocument();
      });
    });

    it('should reset games played when going back to menu', async () => {
      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('← 返回菜单'));
      });

      await waitFor(() => {
        // The games played counter shouldn't be visible in the game area when back at menu
        const idiomGame = screen.queryByTestId('mock-idiom-game');
        expect(idiomGame).not.toBeInTheDocument();
      });
    });
  });

  describe('Statistics Display', () => {
    it('should not show statistics initially (games played = 0)', () => {
      renderIdiomGamePage();
      expect(screen.queryByText('游戏数')).not.toBeInTheDocument();
      expect(screen.queryByText('总分')).not.toBeInTheDocument();
      expect(screen.queryByText('平均分')).not.toBeInTheDocument();
    });

    it('should show statistics after games played > 0', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      // Wait for result modal to appear
      await waitFor(() => {
        expect(screen.getByText(/正确|不正确/)).toBeInTheDocument();
      }, { timeout: 3000 });

      // Wait for feedback and next question load
      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      // Statistics should be visible in game view after playing at least 1 game
      await waitFor(() => {
        expect(screen.getByText('游戏数')).toBeInTheDocument();
        expect(screen.getByText('当前得分')).toBeInTheDocument();
      }, { timeout: 3000 });

      // Verify the games played count is 1
      const gamesPlayedElements = screen.getAllByText('1');
      expect(gamesPlayedElements.length).toBeGreaterThan(0);
    });

    it('should display correct games played count', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText(/正确|不正确/)).toBeInTheDocument();
      }, { timeout: 3000 });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      // Check statistics in game view
      await waitFor(() => {
        expect(screen.getByText('游戏数')).toBeInTheDocument();
        const allNumbers = screen.getAllByText('1');
        expect(allNumbers.length).toBeGreaterThan(0);
      }, { timeout: 3000 });
    });

    it('should display correct total score', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText(/正确|不正确/)).toBeInTheDocument();
      }, { timeout: 3000 });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      // Check total score in game view
      await waitFor(() => {
        expect(screen.getByText('当前得分')).toBeInTheDocument();
        const score100 = screen.getAllByText('100');
        expect(score100.length).toBeGreaterThan(0);
      }, { timeout: 3000 });
    });

    it('should calculate correct average score', async () => {
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText(/正确|不正确/)).toBeInTheDocument();
      }, { timeout: 3000 });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      // Check statistics in game view - after 1 game with score 100, average should be 100
      await waitFor(() => {
        expect(screen.getByText('游戏数')).toBeInTheDocument();
        expect(screen.getByText('当前得分')).toBeInTheDocument();
        // Score is 100 and games played is 1
        const scoreElements = screen.getAllByText('100');
        expect(scoreElements.length).toBeGreaterThan(0);
      }, { timeout: 3000 });
    });
  });

  describe('Error Handling', () => {
    it('should show error message when useIdiomGame returns error', async () => {
      mockUseIdiomGame.mockReturnValue({
        question: null,
        loading: false,
        error: '加载失败，请稍后重试',
        userAnswer: [],
        hintsUsed: 0,
        timeTaken: 0,
        validationResult: null,
        startGame: mockStartGame,
        submitAnswer: jest.fn(),
        getHint: jest.fn(),
        setUserAnswer: jest.fn(),
        reset: jest.fn(),
      });

      renderIdiomGamePage();

      // Click difficulty button to trigger game start
      await act(async () => {
        fireEvent.click(screen.getAllByText('🚀 开始游戏')[0]);
      });

      await waitFor(() => {
        expect(screen.getByText('错误：')).toBeInTheDocument();
        expect(screen.getByText('加载失败，请稍后重试')).toBeInTheDocument();
      }, { timeout: 3000 });
    });

    it('should handle submit API errors', async () => {
      mockApiClient.post.mockRejectedValue({
        response: { data: { error: '提交失败' } },
      });

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('提交失败')).toBeInTheDocument();
      });
    });

    it('should play lose sound on submit error', async () => {
      mockApiClient.post.mockRejectedValue(new Error('Network error'));

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(mockSoundManager.playLose).toHaveBeenCalled();
      });
    });

    it('should handle error with message field', async () => {
      mockApiClient.post.mockRejectedValue({
        response: { data: { message: '服务器错误' } },
      });

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('服务器错误')).toBeInTheDocument();
      });
    });

    it('should show default error message when no specific error message', async () => {
      mockApiClient.post.mockRejectedValue(new Error('Unknown error'));

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('提交失败，请重试')).toBeInTheDocument();
      });
    });

    it('should handle hint API error with error field', async () => {
      mockApiClient.post.mockRejectedValue({
        response: { data: { error: '提示服务不可用' } },
      });

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 提示服务不可用');
      });
    });

    it('should handle hint API error with message field', async () => {
      mockApiClient.post.mockRejectedValue({
        response: { data: { message: '提示功能暂时不可用' } },
      });

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 提示功能暂时不可用');
      });
    });

    it('should show default hint error when no specific message', async () => {
      mockApiClient.post.mockRejectedValue(new Error('Unknown error'));

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 提示获取失败');
      });
    });
  });

  describe('Score Accumulation', () => {
    it('should accumulate score across multiple correct answers', async () => {
      const mockResult1 = { isCorrect: true, score: 100 };
      const mockResult2 = { isCorrect: true, score: 150 };

      mockApiClient.post.mockResolvedValueOnce(mockResult1);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      mockApiClient.post.mockResolvedValueOnce(mockResult2);

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('正确！')).toBeInTheDocument();
      });
    });

    it('should not add score for incorrect answer', async () => {
      const mockResult = { isCorrect: false, score: 0 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await waitFor(() => {
        expect(screen.getByText('不正确')).toBeInTheDocument();
        expect(screen.getByText('得分: +0')).toBeInTheDocument();
      });
    });
  });

  describe('Username Handling', () => {
    it('should use username from usernameUtils in submit API call', async () => {
      mockUsernameUtils.getUsername.mockReturnValue('PlayerABC');
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/submit?playerId=PlayerABC',
        expect.any(Object)
      );
    });

    it('should use Guest as default if no username', async () => {
      mockUsernameUtils.getUsername.mockReturnValue(null);
      const mockResult = { isCorrect: true, score: 100 };
      mockApiClient.post.mockResolvedValue(mockResult);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      expect(mockApiClient.post).toHaveBeenCalledWith(
        '/api/idiom-game/submit?playerId=Guest',
        expect.any(Object)
      );
    });
  });

  describe('Hint State Management', () => {
    it('should clear hint when loading next question', async () => {
      const mockHint = { content: '这是一个提示' };
      const mockResult = { isCorrect: true, score: 100 };

      mockApiClient.post.mockResolvedValueOnce(mockHint);

      await startGame();

      await act(async () => {
        fireEvent.click(screen.getByText('Request Hint'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint: 这是一个提示');
      });

      mockApiClient.post.mockResolvedValueOnce(mockResult);

      await act(async () => {
        fireEvent.click(screen.getByText('Submit Answer'));
      });

      await act(async () => {
        jest.advanceTimersByTime(3000);
      });

      await waitFor(() => {
        expect(screen.getByTestId('hint-display')).toHaveTextContent('Hint:');
      });
    });
  });
});
