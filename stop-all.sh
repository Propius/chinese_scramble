#!/bin/bash

# Chinese Scramble Game - Stop Script
# This script stops both backend and frontend services

set -e

echo "🛑 Stopping Chinese Scramble Game..."
echo "===================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Kill processes by PID if files exist
if [ -f "logs/backend.pid" ]; then
    BACKEND_PID=$(cat logs/backend.pid)
    echo -e "${BLUE}Stopping backend (PID: $BACKEND_PID)...${NC}"
    kill $BACKEND_PID 2>/dev/null || echo -e "${RED}Backend process not found${NC}"
    rm logs/backend.pid
fi

if [ -f "logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat logs/frontend.pid)
    echo -e "${BLUE}Stopping frontend (PID: $FRONTEND_PID)...${NC}"
    kill $FRONTEND_PID 2>/dev/null || echo -e "${RED}Frontend process not found${NC}"
    rm logs/frontend.pid
fi

# Kill any remaining processes on the ports
echo -e "${BLUE}Cleaning up ports...${NC}"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:3000 | xargs kill -9 2>/dev/null || true

echo -e "${GREEN}✅ All services stopped${NC}"
