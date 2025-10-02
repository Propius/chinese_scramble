#!/bin/bash

# Chinese Word Scramble Game - Start with LocalTunnel
# This script starts both backend and frontend, exposing them via localtunnel

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Load tunnel configuration
if [ -f .env.tunnel ]; then
    export $(grep -v '^#' .env.tunnel | xargs)
else
    echo -e "${RED}❌ .env.tunnel file not found!${NC}"
    echo "Creating default .env.tunnel..."
    cat > .env.tunnel << EOF
# LocalTunnel Configuration
# Change this URL anytime to get a different subdomain

# Backend tunnel
BACKEND_TUNNEL_SUBDOMAIN=chinese-scramble-api
BACKEND_PORT=8080

# Frontend tunnel
FRONTEND_TUNNEL_SUBDOMAIN=chinese-scramble
FRONTEND_PORT=3000

# Full tunnel URLs:
# Backend API: https://\${BACKEND_TUNNEL_SUBDOMAIN}.loca.lt
# Frontend: https://\${FRONTEND_TUNNEL_SUBDOMAIN}.loca.lt
EOF
    export BACKEND_TUNNEL_SUBDOMAIN=chinese-scramble-api
    export BACKEND_PORT=8080
    export FRONTEND_TUNNEL_SUBDOMAIN=chinese-scramble
    export FRONTEND_PORT=3000
fi

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                           ║${NC}"
echo -e "${BLUE}║    🎮 Chinese Word Scramble Game - Tunnel Setup 🎮       ║${NC}"
echo -e "${BLUE}║                                                           ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Create logs directory if it doesn't exist
mkdir -p logs

# Check if localtunnel is installed
if ! command -v lt &> /dev/null; then
    echo -e "${YELLOW}📦 Installing localtunnel globally...${NC}"
    npm install -g localtunnel
    echo -e "${GREEN}✅ localtunnel installed${NC}"
    echo ""
fi

# ===========================
# Start Backend
# ===========================
echo -e "${YELLOW}🚀 Starting backend on port ${BACKEND_PORT}...${NC}"
cd chinese-scramble-backend

# Check if backend is already running
if lsof -Pi :${BACKEND_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port ${BACKEND_PORT} is already in use${NC}"
    read -p "Kill existing process and restart? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Killing process on port ${BACKEND_PORT}...${NC}"
        kill -9 $(lsof -ti:${BACKEND_PORT}) 2>/dev/null || true
        sleep 2
    else
        echo -e "${GREEN}Using existing backend instance${NC}"
        cd ..
        SKIP_BACKEND=true
    fi
fi

if [ "$SKIP_BACKEND" != "true" ]; then
    echo -e "${YELLOW}Building and starting Spring Boot application...${NC}"

    if [ ! -f "./mvnw" ]; then
        echo -e "${RED}❌ Maven wrapper (mvnw) not found in chinese-scramble-backend/${NC}"
        echo -e "${YELLOW}Attempting to use system Maven instead...${NC}"
        mvn spring-boot:run > ../logs/backend.log 2>&1 &
    else
        ./mvnw spring-boot:run > ../logs/backend.log 2>&1 &
    fi

    BACKEND_PID=$!
    cd ..

    # Wait for backend to start
    echo -e "${YELLOW}⏳ Waiting for backend to start (this may take 30-60 seconds)...${NC}"
    for i in {1..60}; do
        if curl -s http://localhost:${BACKEND_PORT}/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Backend started successfully!${NC}"
            break
        fi
        if [ $i -eq 60 ]; then
            echo -e "${RED}❌ Backend failed to start. Check logs/backend.log for details.${NC}"
            tail -n 20 logs/backend.log
            exit 1
        fi
        sleep 1
    done
else
    cd ..
fi

echo ""

# ===========================
# Start Frontend
# ===========================
echo -e "${YELLOW}🚀 Starting frontend on port ${FRONTEND_PORT}...${NC}"
cd chinese-scramble-frontend

# Check if frontend is already running
if lsof -Pi :${FRONTEND_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port ${FRONTEND_PORT} is already in use${NC}"
    read -p "Kill existing process and restart? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Killing process on port ${FRONTEND_PORT}...${NC}"
        kill -9 $(lsof -ti:${FRONTEND_PORT}) 2>/dev/null || true
        sleep 2
    else
        echo -e "${GREEN}Using existing frontend instance${NC}"
        cd ..
        SKIP_FRONTEND=true
    fi
fi

if [ "$SKIP_FRONTEND" != "true" ]; then
    echo -e "${YELLOW}Starting React development server...${NC}"

    # Set environment variable for backend URL
    export REACT_APP_API_URL=https://${BACKEND_TUNNEL_SUBDOMAIN}.loca.lt

    npm start > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    cd ..

    # Wait for frontend to start
    echo -e "${YELLOW}⏳ Waiting for frontend to start (this may take 30 seconds)...${NC}"
    for i in {1..30}; do
        if curl -s http://localhost:${FRONTEND_PORT} > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Frontend started successfully!${NC}"
            break
        fi
        if [ $i -eq 30 ]; then
            echo -e "${RED}❌ Frontend failed to start. Check logs/frontend.log for details.${NC}"
            tail -n 20 logs/frontend.log
            exit 1
        fi
        sleep 1
    done
else
    cd ..
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# ===========================
# Start LocalTunnels
# ===========================
echo -e "${YELLOW}🌐 Starting localtunnels...${NC}"
echo -e "${BLUE}Backend Tunnel: https://${BACKEND_TUNNEL_SUBDOMAIN}.loca.lt${NC}"
echo -e "${BLUE}Frontend Tunnel: https://${FRONTEND_TUNNEL_SUBDOMAIN}.loca.lt${NC}"
echo ""

# Run backend tunnel in background
lt --port ${BACKEND_PORT} --subdomain ${BACKEND_TUNNEL_SUBDOMAIN} > logs/backend-tunnel.log 2>&1 &
BACKEND_TUNNEL_PID=$!

# Run frontend tunnel in background
lt --port ${FRONTEND_PORT} --subdomain ${FRONTEND_TUNNEL_SUBDOMAIN} > logs/frontend-tunnel.log 2>&1 &
FRONTEND_TUNNEL_PID=$!

# Wait for tunnels to establish
sleep 5

echo ""
echo -e "${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                           ║${NC}"
echo -e "${GREEN}║    ✅ Chinese Word Scramble Game is now LIVE! ✅          ║${NC}"
echo -e "${GREEN}║                                                           ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📍 Access your game at:${NC}"
echo -e "${GREEN}   🎮 Frontend:    https://${FRONTEND_TUNNEL_SUBDOMAIN}.loca.lt${NC}"
echo -e "${GREEN}   🔌 Backend API: https://${BACKEND_TUNNEL_SUBDOMAIN}.loca.lt${NC}"
echo -e "${GREEN}   📚 Swagger UI:  https://${BACKEND_TUNNEL_SUBDOMAIN}.loca.lt/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📍 Local URLs:${NC}"
echo -e "   🏠 Frontend:  http://localhost:${FRONTEND_PORT}"
echo -e "   🏠 Backend:   http://localhost:${BACKEND_PORT}"
echo ""
echo -e "${YELLOW}⚠️  First-time visitors will see a security page - click 'Continue'${NC}"
echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo -e "   • Change tunnel URLs: Edit .env.tunnel file"
echo -e "   • View backend logs: tail -f logs/backend.log"
echo -e "   • View frontend logs: tail -f logs/frontend.log"
echo -e "   • View tunnel logs: tail -f logs/backend-tunnel.log logs/frontend-tunnel.log"
echo -e "   • Stop: Press Ctrl+C"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop all services...${NC}"

# Cleanup function
cleanup() {
    echo ""
    echo -e "${YELLOW}🛑 Shutting down services...${NC}"

    # Kill tunnels
    if [ ! -z "$BACKEND_TUNNEL_PID" ]; then
        kill $BACKEND_TUNNEL_PID 2>/dev/null || true
    fi
    if [ ! -z "$FRONTEND_TUNNEL_PID" ]; then
        kill $FRONTEND_TUNNEL_PID 2>/dev/null || true
    fi

    # Kill backend
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi

    # Kill frontend
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi

    # Kill any remaining processes on the ports
    lsof -ti:${BACKEND_PORT} | xargs kill -9 2>/dev/null || true
    lsof -ti:${FRONTEND_PORT} | xargs kill -9 2>/dev/null || true

    echo -e "${GREEN}✅ All services stopped${NC}"
    exit 0
}

# Trap Ctrl+C
trap cleanup INT TERM

# Wait forever
wait
