# 🌐 LocalTunnel Setup Guide - Chinese Word Scramble Game

## Quick Reference

### 🚀 One-Command Start

```bash
./start-with-tunnel.sh
```

Your game will be live at: **https://chinese-scramble.loca.lt**

---

## 🔧 Change Your Tunnel URL

### Method 1: Edit Configuration File (Recommended)

```bash
# Edit .env.tunnel
nano .env.tunnel

# or
vim .env.tunnel

# or use any text editor
code .env.tunnel  # VS Code
```

**Change this line:**
```bash
TUNNEL_SUBDOMAIN=chinese-scramble  # ← Change to whatever you want!
```

**Examples:**
```bash
TUNNEL_SUBDOMAIN=my-game          # → https://my-game.loca.lt
TUNNEL_SUBDOMAIN=demo-2024        # → https://demo-2024.loca.lt
TUNNEL_SUBDOMAIN=john-chinese-app # → https://john-chinese-app.loca.lt
TUNNEL_SUBDOMAIN=team-alpha       # → https://team-alpha.loca.lt
```

**Then restart:**
```bash
./start-with-tunnel.sh
```

---

### Method 2: Command Line (Quick Test)

```bash
# Temporary change (doesn't save)
TUNNEL_SUBDOMAIN=my-custom-name ./start-with-tunnel.sh
```

---

### Method 3: Manual LocalTunnel Command

```bash
# Start backend first
cd chinese-scramble-backend
./mvnw spring-boot:run

# In another terminal, start tunnel with custom subdomain
npx localtunnel --port 8080 --subdomain your-custom-name

# Example:
npx localtunnel --port 8080 --subdomain johns-game
# → https://johns-game.loca.lt
```

---

## 📋 Configuration File Reference

### .env.tunnel File Structure

```bash
# LocalTunnel Configuration
# Change this URL anytime to get a different subdomain

TUNNEL_SUBDOMAIN=chinese-scramble  # Your custom subdomain
TUNNEL_PORT=8080                    # Backend port (default: 8080)

# Full tunnel URL will be: https://${TUNNEL_SUBDOMAIN}.loca.lt
# Example: https://chinese-scramble.loca.lt
```

### Available Settings

| Setting | Description | Example |
|---------|-------------|---------|
| `TUNNEL_SUBDOMAIN` | Your custom URL subdomain | `my-game`, `demo`, `test-app` |
| `TUNNEL_PORT` | Backend port to expose | `8080`, `8081`, `3000` |

---

## 🎯 Common Subdomain Examples

### Personal Use
```bash
TUNNEL_SUBDOMAIN=johns-game
TUNNEL_SUBDOMAIN=mary-chinese-app
TUNNEL_SUBDOMAIN=alex-demo
```

### Team/Project Use
```bash
TUNNEL_SUBDOMAIN=team-alpha-game
TUNNEL_SUBDOMAIN=project-demo-2024
TUNNEL_SUBDOMAIN=sprint-review-v1
```

### Testing/Staging
```bash
TUNNEL_SUBDOMAIN=dev-chinese-game
TUNNEL_SUBDOMAIN=staging-word-scramble
TUNNEL_SUBDOMAIN=test-environment
```

### Demo/Presentation
```bash
TUNNEL_SUBDOMAIN=client-demo
TUNNEL_SUBDOMAIN=investor-presentation
TUNNEL_SUBDOMAIN=showcase-2024
```

---

## 🔄 Quick Switch Between URLs

### Save Multiple Configurations

Create different tunnel configs:

```bash
# .env.tunnel.dev
TUNNEL_SUBDOMAIN=dev-chinese-game
TUNNEL_PORT=8080

# .env.tunnel.demo
TUNNEL_SUBDOMAIN=demo-presentation
TUNNEL_PORT=8080

# .env.tunnel.team
TUNNEL_SUBDOMAIN=team-alpha
TUNNEL_PORT=8080
```

**Switch between them:**
```bash
# Use dev config
cp .env.tunnel.dev .env.tunnel
./start-with-tunnel.sh

# Use demo config
cp .env.tunnel.demo .env.tunnel
./start-with-tunnel.sh
```

---

## ✅ Verify Your Tunnel

### Check Tunnel Status

```bash
# Test local connection
curl http://localhost:8080/actuator/health

# Test tunnel connection (replace with your subdomain)
curl https://chinese-scramble.loca.lt/actuator/health

# Should return: {"status":"UP"}
```

### Access Your Game

Once tunnel is running, access these URLs:

| Service | URL Template | Example |
|---------|--------------|---------|
| **Game API** | `https://{subdomain}.loca.lt` | https://chinese-scramble.loca.lt |
| **Swagger UI** | `https://{subdomain}.loca.lt/swagger-ui.html` | https://chinese-scramble.loca.lt/swagger-ui.html |
| **Health Check** | `https://{subdomain}.loca.lt/actuator/health` | https://chinese-scramble.loca.lt/actuator/health |
| **H2 Console** | `https://{subdomain}.loca.lt/h2-console` | https://chinese-scramble.loca.lt/h2-console |

---

## 🐛 Troubleshooting

### ❌ Subdomain Already Taken

**Error:**
```
Error: The subdomain chinese-scramble is already in use
```

**Solution:**
```bash
# Choose a different subdomain
nano .env.tunnel

# Change to something unique:
TUNNEL_SUBDOMAIN=chinese-scramble-2024
TUNNEL_SUBDOMAIN=my-unique-game
```

---

### ❌ Tunnel Not Starting

**Check if localtunnel is installed:**
```bash
which lt
# or
lt --version
```

**Install/reinstall:**
```bash
npm install -g localtunnel
```

---

### ❌ Backend Not Accessible

**Verify backend is running:**
```bash
curl http://localhost:8080/actuator/health
```

**Check backend logs:**
```bash
tail -f logs/backend.log
```

**Restart backend:**
```bash
cd chinese-scramble-backend
./mvnw spring-boot:run
```

---

### ❌ Security Warning Page

**This is normal!** LocalTunnel shows a warning on first access.

**What to do:**
1. Click "Continue" or "Click to Continue"
2. You'll be taken to your game
3. This only appears once per browser session

---

## 🎓 Advanced Configuration

### Change Backend Port

If your backend runs on a different port:

```bash
# Edit .env.tunnel
TUNNEL_SUBDOMAIN=my-game
TUNNEL_PORT=8081  # ← Change this

# Also update backend application.properties
# server.port=8081
```

### Use Environment Variables

```bash
# Set as environment variable
export TUNNEL_SUBDOMAIN=my-custom-game
export TUNNEL_PORT=8080

./start-with-tunnel.sh
```

### Run Multiple Tunnels

```bash
# Terminal 1 - Backend on 8080
cd chinese-scramble-backend
./mvnw spring-boot:run

# Terminal 2 - Tunnel 1
lt --port 8080 --subdomain game-api

# Terminal 3 - Another service on 3000
cd frontend
npm start

# Terminal 4 - Tunnel 2
lt --port 3000 --subdomain game-frontend
```

---

## 📊 Monitoring Your Tunnel

### View Backend Logs

```bash
# Real-time logs
tail -f logs/backend.log

# Last 50 lines
tail -n 50 logs/backend.log

# Search for errors
grep ERROR logs/backend.log
```

### Check Tunnel Connectivity

```bash
# Continuous monitoring (every 5 seconds)
watch -n 5 'curl -s https://chinese-scramble.loca.lt/actuator/health'

# Single check
curl -I https://chinese-scramble.loca.lt
```

### Test API Endpoints

```bash
# Register player
curl -X POST https://chinese-scramble.loca.lt/api/players/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@test.com","password":"password123"}'

# Health check
curl https://chinese-scramble.loca.lt/actuator/health
```

---

## 🛑 Stop Everything

### Using the Script

```bash
# Press Ctrl+C in the terminal running start-with-tunnel.sh
# Script automatically cleans up:
# - Stops backend
# - Stops tunnel
# - Kills all related processes
```

### Manual Cleanup

```bash
# Kill backend
lsof -ti:8080 | xargs kill -9

# Kill localtunnel
pkill -f localtunnel

# Or kill specific process
ps aux | grep "spring-boot:run"
kill -9 <PID>
```

---

## 💡 Best Practices

### 1. Choose Descriptive Subdomains
✅ Good: `demo-chinese-game`, `sprint-review-v2`
❌ Avoid: `test`, `abc`, `123`

### 2. Keep Tunnel Running
- Don't close terminal while tunnel is active
- Use `screen` or `tmux` for persistent sessions

### 3. Share URLs Carefully
- Only share with intended users
- Don't expose production data
- Use for demos/testing only

### 4. Monitor Backend Health
```bash
# Set up monitoring
while true; do
  curl -s https://chinese-scramble.loca.lt/actuator/health
  sleep 30
done
```

### 5. Use HTTPS Endpoints
- LocalTunnel provides HTTPS by default
- All API calls should use `https://` not `http://`

---

## 📱 Mobile Testing

### Test on Your Phone

1. **Start tunnel with your subdomain**
   ```bash
   ./start-with-tunnel.sh
   ```

2. **Get your tunnel URL**
   ```
   https://chinese-scramble.loca.lt
   ```

3. **Open on mobile browser**
   - Safari (iOS)
   - Chrome (Android)
   - Any mobile browser

4. **Click "Continue" on security page**

5. **Test all features**
   - Registration
   - Game play
   - Leaderboards
   - Achievements

---

## 🔒 Security Notes

### ⚠️ Important Warnings

1. **Don't expose production databases**
   - Use H2 in-memory database for tunneling
   - Never expose PostgreSQL with real data

2. **Temporary access only**
   - Tunnels are for testing/demos
   - Not suitable for production

3. **URL is public**
   - Anyone with the URL can access
   - No authentication on LocalTunnel itself

4. **Data in transit**
   - LocalTunnel uses HTTPS
   - Your data is encrypted

### ✅ Safe Usage

```bash
# Use H2 (in-memory) - Safe for demos
spring.datasource.url=jdbc:h2:mem:chinesescramble

# Don't use PostgreSQL with tunnel
# spring.datasource.url=jdbc:postgresql://... ❌
```

---

## 🆘 Need Help?

### Resources

- 📖 LocalTunnel Docs: https://theboroer.github.io/localtunnel-www/
- 🐛 Report Issues: https://github.com/localtunnel/localtunnel/issues
- 💬 Community: Stack Overflow (tag: localtunnel)

### Common Questions

**Q: Can I use custom domain?**
A: No, LocalTunnel only provides *.loca.lt subdomains. Use ngrok or Cloudflare Tunnel for custom domains.

**Q: How long does tunnel stay active?**
A: As long as your backend is running and you maintain internet connection.

**Q: Can I use same subdomain later?**
A: Yes! Your subdomain in .env.tunnel is reusable.

**Q: Is there a rate limit?**
A: Free tier has reasonable limits. For heavy usage, consider ngrok or Cloudflare.

---

## 🎉 You're All Set!

Your Chinese Word Scramble Game can now be accessed from anywhere in the world!

**Quick reminder:**
```bash
# Change subdomain: Edit .env.tunnel
# Start tunnel: ./start-with-tunnel.sh
# Share URL: https://your-subdomain.loca.lt
# Stop: Ctrl+C
```

Happy tunneling! 🚀
