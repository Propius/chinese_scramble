# 🚀 Quick Start Guide - Chinese Word Scramble Game

Welcome! This guide will help you start the Chinese Word Scramble Game in just a few minutes.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Option 1: Docker Compose (Recommended)](#option-1-docker-compose-recommended---full-stack)
- [Option 2: Local Development](#option-2-local-development-backend-only)
- [Option 3: Backend + Manual Testing](#option-3-backend--manual-testing-no-frontend)
- [Option 4: Public Access with LocalTunnel](#option-4-public-access-with-localtunnel-new)
- [Verify Everything is Working](#-verify-everything-is-working)
- [Troubleshooting](#-troubleshooting)
- [Next Steps](#-next-steps-after-starting)
- [Recommended Start Flow](#-recommended-start-flow)

---

## Prerequisites

### For Docker (Option 1)
- Docker Desktop installed
- Docker Compose available

### For Local Development (Option 2 & 3)
- Java 21 or higher
- Maven 3.9+ (or use included `./mvnw` wrapper)
- Node.js 16+ and npm (for frontend)

---

## Option 1: Docker Compose (Recommended - Full Stack)

This starts **everything** (PostgreSQL database + Backend API + Frontend):

```bash
# Navigate to project root
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble

# Start all services
docker-compose up --build

# Or run in background (detached mode)
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 🌐 Access the Application

Once started, open your browser:

| Service | URL | Description |
|---------|-----|-------------|
| 🎮 **Frontend** | http://localhost:3000 | React web application |
| 🔧 **Backend API** | http://localhost:8080 | REST API endpoints |
| 📚 **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| 🗄️ **Database** | localhost:5432 | PostgreSQL database |

### ⏱️ Startup Time
- First build: ~3-5 minutes (downloads dependencies)
- Subsequent starts: ~30-60 seconds

---

## Option 2: Local Development (Backend Only)

### Step 1: Start Backend

```bash
# Navigate to backend directory
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble/chinese-scramble-backend

# Build and run (uses H2 in-memory database)
./mvnw clean spring-boot:run
```

**Wait for this message:**
```
Started ChineseScrambleBackendApplication in X.XXX seconds
```

### 🌐 Access Backend Services

| Service | URL | Credentials |
|---------|-----|-------------|
| 🔧 **Backend API** | http://localhost:8080 | - |
| 📚 **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| 🗄️ **H2 Console** | http://localhost:8080/h2-console | JDBC URL: `jdbc:h2:mem:chinesescramble`<br>Username: `sa`<br>Password: (empty) |

### Step 2: Start Frontend (Optional)

```bash
# In a NEW terminal window
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble/chinese-scramble-frontend

# Install dependencies (first time only)
npm install

# Start React development server
npm start
```

**Access:**
- 🎮 **Frontend**: http://localhost:3000

The frontend will automatically open in your browser!

---

## Option 3: Backend + Manual Testing (No Frontend)

Perfect for API development and testing!

### Start Backend Only

```bash
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble/chinese-scramble-backend
./mvnw spring-boot:run
```

### Test with Swagger UI

👉 **Open:** http://localhost:8080/swagger-ui.html

You'll see all API endpoints organized by category:
- 👤 Player Management
- 🎯 Idiom Game
- 📝 Sentence Game
- 🏆 Leaderboards
- 🎖️ Achievements
- 🚩 Feature Flags

### Test with cURL

```bash
# 1. Register a player
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testplayer",
    "email": "test@example.com",
    "password": "password123"
  }'

# Response will include player ID (use it in next requests)

# 2. Start an idiom game (replace playerId=1 with your actual ID)
curl -X POST "http://localhost:8080/api/games/idiom/start?playerId=1" \
  -H "Content-Type: application/json" \
  -d '{"difficulty": "EASY"}'

# You'll receive scrambled characters to solve!

# 3. Submit your answer
curl -X POST "http://localhost:8080/api/games/idiom/submit?playerId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "answer": "一帆风顺",
    "timeTaken": 45,
    "hintsUsed": 0
  }'

# 4. Check leaderboard
curl "http://localhost:8080/api/leaderboards/top?gameType=IDIOM&difficulty=EASY&limit=10"
```

### Test with Postman

1. Import the API documentation from: http://localhost:8080/api-docs
2. All endpoints are pre-configured with examples

---

## Option 4: Public Access with LocalTunnel 🆕

### 🌐 Share Your Game Online!

Host your game publicly with a custom URL like `https://chinese-scramble.loca.lt`

This is perfect for:
- 🎮 Sharing with friends/testers
- 📱 Testing on mobile devices
- 🌍 Remote access without port forwarding
- 🎯 Demo presentations

### Prerequisites
- Backend running locally
- Node.js/npm installed
- Internet connection

### Quick Start

**Mac/Linux:**
```bash
# Navigate to project root
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble

# Run the tunnel script (automatically installs localtunnel if needed)
./start-with-tunnel.sh
```

**Windows:**
```bash
# Navigate to project root
cd C:\Users\...\word_scramble

# Run the tunnel script
start-with-tunnel.bat
```

The script will:
1. ✅ Install `localtunnel` if not present
2. ✅ Start the backend automatically
3. ✅ Create a public tunnel
4. ✅ Display your public URL

### 🎯 Your Game is Live!

After running the script, you'll see:

```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║    ✅ Chinese Word Scramble Game is now LIVE! ✅          ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝

📍 Access your game at:
   🌐 Public URL:  https://chinese-scramble.loca.lt
   🏠 Local URL:   http://localhost:8080
   📚 Swagger UI:  https://chinese-scramble.loca.lt/swagger-ui.html
```

### 🔧 Customize Your Tunnel URL

**Easy Method - Edit Configuration File:**

```bash
# Edit .env.tunnel file
nano .env.tunnel

# Change the subdomain:
TUNNEL_SUBDOMAIN=my-custom-name
TUNNEL_PORT=8080
```

**Available customization:**
```bash
# .env.tunnel file
TUNNEL_SUBDOMAIN=chinese-scramble   # ← Change this anytime!
TUNNEL_PORT=8080                     # Backend port

# Examples:
# TUNNEL_SUBDOMAIN=my-game        → https://my-game.loca.lt
# TUNNEL_SUBDOMAIN=demo-2024      → https://demo-2024.loca.lt
# TUNNEL_SUBDOMAIN=team-alpha     → https://team-alpha.loca.lt
```

After editing, restart the script:
```bash
./start-with-tunnel.sh
```

### 📱 Access from Anywhere

**Share these URLs with anyone:**
- 🎮 **Game API**: `https://chinese-scramble.loca.lt`
- 📚 **Swagger UI**: `https://chinese-scramble.loca.lt/swagger-ui.html`
- 🏥 **Health Check**: `https://chinese-scramble.loca.lt/actuator/health`

**Test on mobile:**
1. Open browser on your phone
2. Navigate to your tunnel URL
3. Play the game!

### ⚠️ Important Notes

**First-Time Access:**
- Visitors will see a LocalTunnel security page
- Click "Continue" or "Click to Continue"
- This is normal and expected

**Tunnel Limitations:**
- Free tier: May disconnect after inactivity
- Requires backend to stay running
- URL changes if you restart (unless you use paid plan)

**Security Considerations:**
- Anyone with the URL can access your game
- Don't expose production databases
- Use for testing/demo purposes only

### 🛑 Stop the Tunnel

Press `Ctrl+C` in the terminal where the script is running.

The script will automatically:
- Stop the tunnel
- Stop the backend
- Clean up all processes

### 🔄 Restart with Same URL

```bash
# Your subdomain is saved in .env.tunnel
./start-with-tunnel.sh

# It will use the same URL: https://chinese-scramble.loca.lt
```

### 💡 Advanced Usage

**Manual tunnel setup:**
```bash
# Start backend first
cd chinese-scramble-backend
./mvnw spring-boot:run

# In another terminal, start tunnel
npx localtunnel --port 8080 --subdomain chinese-scramble
```

**Use different port:**
```bash
# Edit .env.tunnel
TUNNEL_PORT=8081

# Backend will need to run on that port too
```

**Check tunnel status:**
```bash
# View backend logs
tail -f logs/backend.log

# Test tunnel connectivity
curl https://chinese-scramble.loca.lt/actuator/health
```

### 🎓 Alternative Tunneling Services

If LocalTunnel doesn't work, try these alternatives:

**ngrok** (Popular, more reliable):
```bash
# Install ngrok
brew install ngrok  # Mac
# or download from https://ngrok.com

# Start tunnel
ngrok http 8080

# You'll get a URL like: https://abc123.ngrok.io
```

**Cloudflare Tunnel** (Free, enterprise-grade):
```bash
# Install cloudflared
brew install cloudflared  # Mac

# Start tunnel
cloudflared tunnel --url http://localhost:8080
```

**Tailscale Funnel** (Secure, peer-to-peer):
```bash
# Install Tailscale
# Visit: https://tailscale.com

# Share your app
tailscale funnel 8080
```

---

## 🔍 Verify Everything is Working

### 1. Check Backend Health

```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### 2. Check Database Connection

**For Docker (PostgreSQL):**
```bash
docker exec -it chinese-scramble-db psql -U postgres -d chinesescramble -c "SELECT COUNT(*) FROM players;"
```

**For Local (H2):**
- Visit: http://localhost:8080/h2-console
- Connect and run: `SELECT COUNT(*) FROM PLAYERS;`

### 3. Verify API Endpoints

Visit Swagger UI: http://localhost:8080/swagger-ui.html

You should see **25+ endpoints** across 6 controllers.

---

## 🐛 Troubleshooting

### ❌ Port 8080 Already in Use

**Find and kill the process:**
```bash
# Mac/Linux
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Or change the port:**
```bash
# Edit: chinese-scramble-backend/src/main/resources/application.properties
server.port=8081
```

---

### ❌ Docker Compose Issues

**Clean restart:**
```bash
# Stop all containers
docker-compose down

# Remove volumes (fresh database)
docker-compose down -v

# Rebuild everything from scratch
docker-compose up --build --force-recreate
```

**Check container status:**
```bash
docker-compose ps
```

**View logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

---

### ❌ Maven Build Issues

**Clear Maven cache:**
```bash
./mvnw clean
```

**Force dependency update:**
```bash
./mvnw clean install -U
```

**Skip tests for faster build:**
```bash
./mvnw clean install -DskipTests
```

**Java version mismatch:**
```bash
# Check Java version (must be 21+)
java -version

# Set JAVA_HOME if needed (Mac)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

---

### ❌ Database Connection Issues

**PostgreSQL not starting (Docker):**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

**H2 Database issues (Local):**
- H2 is in-memory, so restart the backend to reset
- Check console at: http://localhost:8080/h2-console

---

### ❌ Frontend Not Loading

**Check backend is running:**
```bash
curl http://localhost:8080/actuator/health
```

**CORS issues:**
- Backend must be running on port 8080
- Frontend must be running on port 3000
- Check browser console for errors

**Rebuild frontend:**
```bash
cd chinese-scramble-frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

---

### ❌ Tests Failing

**Run tests with verbose output:**
```bash
./mvnw test -X
```

**Run specific test:**
```bash
./mvnw test -Dtest=PlayerServiceTest
```

**Check test database:**
- Tests use H2 in-memory database
- Test profile: `src/test/resources/application-test.properties`

---

## 📖 Next Steps After Starting

### 1️⃣ Register a Player

**Via Swagger UI:**
1. Go to http://localhost:8080/swagger-ui.html
2. Navigate to **Player Controller**
3. Try **POST /api/players/register**
4. Click "Try it out"
5. Enter:
   ```json
   {
     "username": "chineselearner",
     "email": "learner@example.com",
     "password": "MyPassword123"
   }
   ```
6. Click "Execute"
7. **Save the player ID** from the response!

**Via Frontend:**
- Go to http://localhost:3000
- Click "Register"
- Fill in the form

---

### 2️⃣ Start Your First Game

**Idiom Scramble (成语):**

Via Swagger UI:
1. Navigate to **Idiom Game Controller**
2. Try **POST /api/games/idiom/start**
3. Enter your player ID
4. Choose difficulty:
   - `EASY` - 4 characters, 60s time limit
   - `MEDIUM` - 4 characters, 120s time limit
   - `HARD` - 4 characters, 180s time limit
   - `EXPERT` - 4 characters, 240s time limit

You'll receive scrambled Chinese characters!

Example response:
```json
{
  "sessionId": 123,
  "scrambledCharacters": ["语", "成", "全", "大"],
  "difficulty": "EASY",
  "timeLimit": 60,
  "maxHints": 3
}
```

**Sentence Crafting:**
- Similar process, use **Sentence Game Controller**

---

### 3️⃣ Submit Your Answer

Via Swagger UI:
1. **POST /api/games/idiom/submit**
2. Enter:
   ```json
   {
     "answer": "成语大全",
     "timeTaken": 45,
     "hintsUsed": 0
   }
   ```

You'll get detailed scoring:
```json
{
  "correct": true,
  "score": 250,
  "timeBonus": 50,
  "accuracyBonus": 100,
  "newAchievements": [...]
}
```

---

### 4️⃣ Check Leaderboards

**GET /api/leaderboards/top**

Parameters:
- `gameType`: IDIOM or SENTENCE
- `difficulty`: EASY, MEDIUM, HARD, EXPERT
- `limit`: Number of players (default 10)

See where you rank!

---

### 5️⃣ Unlock Achievements

**GET /api/achievements/player/{playerId}**

Track your progress across 10 achievement types:
- 🏆 **首次胜利** (FIRST_WIN) - Complete first game
- ⚡ **速度恶魔** (SPEED_DEMON) - Complete in <30s
- 💯 **完美得分** (PERFECT_SCORE) - 100% accuracy, no hints
- 🎯 **百场达人** (HUNDRED_GAMES) - Play 100 games
- 👑 **成语大师** (IDIOM_MASTER) - Rank #1 on idiom leaderboard
- 📝 **句子大师** (SENTENCE_MASTER) - Rank #1 on sentence leaderboard
- ⭐ **顶级玩家** (TOP_RANKED) - Top 10 on any leaderboard
- 📅 **坚持不懈** (CONSISTENCY) - Play 7 consecutive days
- 💰 **高分达人** (HIGH_SCORER) - Score 1000+ in single game
- 🎓 **无提示挑战** (HINT_FREE) - Complete 10 games without hints

---

## 🎯 Recommended Start Flow

### For Full Experience (with Frontend):

```bash
# 1. Start everything with Docker
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble
docker-compose up --build

# 2. Wait for all services to start (~2 minutes first time)

# 3. Open your browser tabs:
# - Frontend: http://localhost:3000
# - Swagger: http://localhost:8080/swagger-ui.html

# 4. Register a player and start playing!
```

---

### For Development/Testing:

```bash
# 1. Start backend only
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble/chinese-scramble-backend
./mvnw spring-boot:run

# 2. Wait for startup message

# 3. Test APIs via Swagger
# Open: http://localhost:8080/swagger-ui.html

# 4. Explore all 25+ endpoints interactively
```

---

### For API Development:

```bash
# 1. Start backend
cd chinese-scramble-backend
./mvnw spring-boot:run

# 2. Use your favorite API client
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - Postman: Import from http://localhost:8080/api-docs
# - cURL: See examples above

# 3. Check H2 database
# http://localhost:8080/h2-console
```

---

## 📊 Scoring System Quick Reference

### Base Scores
- **EASY**: 100 points
- **MEDIUM**: 200 points
- **HARD**: 300 points
- **EXPERT**: 500 points

### Time Bonuses
- **< 30 seconds**: +50 points
- **< 60 seconds**: +30 points
- **< 90 seconds**: +15 points

### Accuracy Bonuses
- **100%**: +100 points
- **95-99%**: +50 points
- **90-94%**: +25 points

### Hint Penalties
- **Level 1**: -10 points
- **Level 2**: -20 points
- **Level 3**: -30 points

### Difficulty Multipliers
- **EASY**: 1.0x
- **MEDIUM**: 1.2x
- **HARD**: 1.5x
- **EXPERT**: 2.0x

### Maximum Possible Scores
- **EASY**: 250 points
- **MEDIUM**: 420 points
- **HARD**: 675 points
- **EXPERT**: 1,300 points

---

## 🎮 Game Content

### Idiom Scramble
- **20 Chinese idioms** (成语)
- 4 characters each
- Definitions, pinyin, and usage examples included

### Sentence Crafting
- **15 sentence patterns**
- Various grammar structures
- Progressive difficulty levels
- 3-level hint system per sentence

---

## 🔗 Useful Links

| Resource | URL |
|----------|-----|
| Backend README | `/chinese-scramble-backend/README.md` |
| API Documentation | `/API_DOCUMENTATION.md` |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Health Check | http://localhost:8080/actuator/health |
| Metrics | http://localhost:8080/actuator/metrics |

---

## 💡 Pro Tips

1. **Use Swagger UI** for interactive API exploration - it's the fastest way to test!
2. **Check H2 Console** to see real-time database changes during development
3. **Monitor logs** with `docker-compose logs -f` to debug issues
4. **Use different difficulty levels** - EASY is great for testing, EXPERT for challenges
5. **Try the hint system** - costs points but helps you learn
6. **Track achievements** - they unlock automatically as you play
7. **Check leaderboards** - see how you rank against others

---

## 🆘 Need Help?

### Documentation
- Full API docs: `/API_DOCUMENTATION.md`
- Backend README: `/chinese-scramble-backend/README.md`

### Common Issues
- Check [Troubleshooting](#-troubleshooting) section above
- View logs: `docker-compose logs -f`
- Restart: `docker-compose restart`

### Support
- 📧 Email: dev@govtech.com
- 🐛 Issues: GitHub Issues
- 📚 Docs: Swagger UI

---

## 🎉 Ready to Play!

You're all set! Start the application and begin your Chinese language learning journey with the Word Scramble Game!

**Happy Learning! 加油！** 🇨🇳
