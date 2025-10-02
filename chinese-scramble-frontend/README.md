# Chinese Word Scramble Frontend (汉字游戏前端)

A comprehensive React 18.2.0 frontend application for the Chinese Word Scramble Game, featuring idiom scrambling and sentence crafting games with multiple difficulty levels.

## Features (功能特点)

- **成语拼字游戏 (Idiom Scramble Game)**: Rearrange scrambled Chinese characters to form correct idioms
- **造句游戏 (Sentence Crafting Game)**: Construct grammatically correct sentences from scrambled words
- **多难度级别 (Multiple Difficulty Levels)**: Easy (简单), Medium (中等), Hard (困难), Expert (专家)
- **排行榜系统 (Leaderboard System)**: Compete with other players
- **统计追踪 (Statistics Tracking)**: Track your progress and achievements
- **全中文界面 (Chinese UI)**: All interface elements in Simplified Chinese
- **响应式设计 (Responsive Design)**: Works on desktop and mobile devices

## Technology Stack (技术栈)

- **Frontend Framework**: React 18.2.0 with TypeScript
- **Routing**: React Router DOM 6.30+
- **Styling**: Tailwind CSS with Chinese font support
- **Drag & Drop**: @dnd-kit for character/word tiles
- **Charts**: Recharts for statistics visualization
- **Internationalization**: i18next with Chinese translations
- **HTTP Client**: Axios
- **State Management**: React Hooks

## Prerequisites (前置要求)

- Node.js 16+ and npm
- Backend API running on port 8090 (see `/chinese-scramble-backend`)

## Quick Start (快速开始)

### Local Development (本地开发)

```bash
# Install dependencies
npm install

# Start development server (connects to http://localhost:8090)
npm start

# Application will be available at http://localhost:3000
```

### With LocalTunnel (使用隧道)

```bash
# Start with tunnel environment (connects to https://chinese-scramble.loca.lt)
npm run start:tunnel

# Note: Requires backend to be running with tunnel as well
```

## Environment Variables (环境变量)

### `.env` (Local Development)
```
REACT_APP_API_URL=http://localhost:8090
REACT_APP_ENVIRONMENT=development
```

### `.env.tunnel` (LocalTunnel)
```
REACT_APP_API_URL=https://chinese-scramble.loca.lt
REACT_APP_ENVIRONMENT=tunnel
```

## Available Scripts (可用脚本)

```bash
# Start development server (local)
npm start

# Start with tunnel configuration
npm run start:tunnel

# Build for production
npm run build

# Run tests
npm test

# Run tests with coverage
npm test -- --coverage --watchAll=false
```

## API Integration (API 集成)

The frontend connects to the backend API at the configured `REACT_APP_API_URL`:

### Idiom Game Endpoints
- `GET /api/idiom-game/start?difficulty={difficulty}` - Get random idiom
- `POST /api/idiom-game/submit` - Validate answer
- `POST /api/idiom-game/hint` - Get hint

### Sentence Game Endpoints
- `GET /api/sentence-game/start?difficulty={difficulty}` - Get random sentence
- `POST /api/sentence-game/submit` - Validate answer
- `POST /api/sentence-game/hint` - Get hint

### Leaderboard Endpoints
- `GET /api/leaderboard/combined` - Get combined leaderboard
- `GET /api/leaderboard/idiom?difficulty={difficulty}` - Idiom leaderboard
- `GET /api/leaderboard/sentence?difficulty={difficulty}` - Sentence leaderboard

## Game Features (游戏功能)

### Difficulty Levels (难度级别)

| Level | Chinese | Time Limit | Base Points |
|-------|---------|------------|-------------|
| EASY | 简单 | 180s | 100 |
| MEDIUM | 中等 | 120s | 200 |
| HARD | 困难 | 90s | 300 |
| EXPERT | 专家 | 60s | 500 |

### Scoring System (计分系统)

- **Base Points**: Based on difficulty level
- **Time Bonus**: Extra points for fast completion
- **Difficulty Multiplier**: 1.0x (Easy) to 3.0x (Expert)
- **Hint Penalty**: -10 to -30 points per hint used

## Chinese Font Support (中文字体支持)

The application uses Chinese-optimized fonts:
- Noto Sans SC (primary)
- SimSun (fallback)
- Microsoft YaHei (fallback)
- PingFang SC (macOS)
- Hiragino Sans GB (macOS)

## Development Status (开发状态)

### ✅ Completed
- Project structure and configuration
- Routing and navigation with all pages
- i18n with comprehensive Chinese translations
- Service layer with full API integration
- Custom hooks for game state management
- Layout components (Navigation, Header, Footer, GameModeSelector)
- Page components (Home, IdiomGamePage, SentenceGamePage, LeaderboardPage, StatisticsPage)
- Environment configuration (.env files)
- Tailwind CSS with Chinese font support
- TypeScript types and interfaces
- Utility functions (scoring, Chinese text, shuffling, storage)

### 🚧 In Progress
- Full drag-and-drop implementation for game tiles
- Detailed game components (IdiomTileGrid, WordTileBank, etc.)
- Leaderboard table with filters
- Statistics charts and visualizations

### 📋 Planned
- Common components (DifficultySelector, ScoreDisplay, etc.)
- Hint system UI
- Audio pronunciation support
- Achievement system UI
- Practice mode
- Mobile optimization

## Troubleshooting (故障排除)

### Port Already in Use
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9
```

### Dependency Issues
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install --legacy-peer-deps
```

### API Connection Issues
- Verify backend is running on port 8090
- Check REACT_APP_API_URL in `.env`
- Check browser console for CORS errors

## Contributing (贡献)

1. Follow React best practices
2. Use TypeScript for type safety
3. Keep components focused and reusable
4. Write tests for new features
5. Maintain Chinese language support
6. Follow existing code style

---

**版权所有 © 2025 汉字游戏**
