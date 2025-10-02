#!/bin/bash

# Chinese Scramble Game - Complete Startup Script
# This script starts both backend and frontend services

set -e

echo "🚀 Starting Chinese Scramble Game..."
echo "===================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running from correct directory
if [ ! -d "chinese-scramble-backend" ] || [ ! -d "chinese-scramble-frontend" ]; then
    echo -e "${RED}Error: Must be run from project root directory${NC}"
    exit 1
fi

# Function to check if port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Create logs directory if it doesn't exist
mkdir -p logs

# Kill existing processes on ports
echo -e "${BLUE}Cleaning up existing processes...${NC}"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:3000 | xargs kill -9 2>/dev/null || true
sleep 2

# Start Backend
echo -e "${BLUE}Starting backend on port 8080...${NC}"
cd chinese-scramble-backend

# Check if mvnw exists
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}Error: mvnw not found in backend directory${NC}"
    exit 1
fi

# Start backend in background
nohup ./mvnw spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}Backend started with PID: $BACKEND_PID${NC}"

cd ..

# Wait for backend to be ready
echo -e "${BLUE}Waiting for backend to start...${NC}"
for i in {1..30}; do
    if check_port 8080; then
        echo -e "${GREEN}✓ Backend is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Error: Backend failed to start${NC}"
        cat logs/backend.log | tail -20
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Test backend health
echo -e "${BLUE}Testing backend health...${NC}"
HEALTH_CHECK=$(curl -s http://localhost:8080/actuator/health || echo "failed")
if [[ $HEALTH_CHECK == *"UP"* ]]; then
    echo -e "${GREEN}✓ Backend health check passed${NC}"
else
    echo -e "${RED}Warning: Backend health check failed${NC}"
fi

# Start Frontend
echo -e "${BLUE}Starting frontend on port 3000...${NC}"
cd chinese-scramble-frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo -e "${BLUE}Installing frontend dependencies...${NC}"
    npm install
fi

# Start frontend in background
nohup npm start > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}Frontend started with PID: $FRONTEND_PID${NC}"

cd ..

# Wait for frontend to be ready
echo -e "${BLUE}Waiting for frontend to start...${NC}"
for i in {1..60}; do
    if check_port 3000; then
        echo -e "${GREEN}✓ Frontend is ready!${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}Error: Frontend failed to start${NC}"
        cat logs/frontend.log | tail -20
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Create logs directory if it doesn't exist
mkdir -p logs

# Save PIDs to file for easy cleanup
echo $BACKEND_PID > logs/backend.pid
echo $FRONTEND_PID > logs/frontend.pid

echo ""
echo -e "${GREEN}===================================="
echo "✅ All services started successfully!"
echo "===================================="
echo ""
echo -e "📱 Frontend:  ${BLUE}http://localhost:3000${NC}"
echo -e "🔧 Backend:   ${BLUE}http://localhost:8080${NC}"
echo -e "📊 Swagger:   ${BLUE}http://localhost:8080/swagger-ui.html${NC}"
echo -e "💾 H2 Console:${BLUE}http://localhost:8080/h2-console${NC}"
echo ""
echo -e "📋 Logs:"
echo -e "   Backend:  ${BLUE}tail -f logs/backend.log${NC}"
echo -e "   Frontend: ${BLUE}tail -f logs/frontend.log${NC}"
echo ""
echo -e "🛑 To stop all services:"
echo -e "   ${BLUE}./stop-all.sh${NC}"
echo ""
