# 🚀 LocalTunnel Quick Start

## One Command Setup

```bash
cd /Users/shengkai/Documents/govtech-jagveer/word_scramble
./start-with-tunnel.sh
```

Your game will be live at: **https://chinese-scramble.loca.lt**

---

## Change URL

```bash
# Edit .env.tunnel
nano .env.tunnel

# Change this:
TUNNEL_SUBDOMAIN=your-custom-name

# Save and restart
./start-with-tunnel.sh
```

---

## Stop

Press `Ctrl+C` in the terminal

---

## Troubleshooting

### ❌ "./mvnw: No such file or directory"

**Fix:**
```bash
cd chinese-scramble-backend
mvn -N wrapper:wrapper
cd ..
./start-with-tunnel.sh
```

This generates the Maven wrapper files needed to run the backend.

---

### ❌ "Port 8080 already in use"

**Fix:**
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or the script will prompt you automatically
./start-with-tunnel.sh
```

---

### ❌ "localtunnel not found"

**Fix:**
```bash
npm install -g localtunnel
./start-with-tunnel.sh
```

---

### ❌ Backend won't start

**Check logs:**
```bash
tail -f logs/backend.log
```

**Common issues:**
- Java 21 not installed: `java -version`
- Port conflict: `lsof -i:8080`
- Maven issues: `mvn clean install`

---

## URLs After Starting

| Service | URL |
|---------|-----|
| **Public API** | https://chinese-scramble.loca.lt |
| **Swagger UI** | https://chinese-scramble.loca.lt/swagger-ui.html |
| **Health Check** | https://chinese-scramble.loca.lt/actuator/health |
| **Local API** | http://localhost:8080 |

---

## Test Your Tunnel

```bash
# From another terminal
curl https://chinese-scramble.loca.lt/actuator/health

# Should return: {"status":"UP"}
```

---

## Share With Others

Send them this URL: **https://chinese-scramble.loca.lt/swagger-ui.html**

⚠️ **Note:** First-time visitors will see a security warning page. Click "Continue".

---

## Need More Help?

See full guides:
- `LOCALTUNNEL_GUIDE.md` - Complete tunnel documentation
- `STARTUP_GUIDE.md` - Full startup options
- `README.md` - Project overview

---

**Quick Reminder:**
- Edit `.env.tunnel` to change URL
- Script auto-installs localtunnel
- Logs saved to `backend.log`
- Press Ctrl+C to stop everything
