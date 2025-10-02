@echo off
REM Chinese Word Scramble Game - Start with LocalTunnel (Windows)
REM This script starts the backend and exposes it via localtunnel

setlocal enabledelayedexpansion

REM Load tunnel configuration
if exist .env.tunnel (
    for /f "tokens=1,2 delims==" %%a in (.env.tunnel) do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set %%a=%%b
        )
    )
) else (
    echo Creating default .env.tunnel...
    (
        echo # LocalTunnel Configuration
        echo # Change this URL anytime to get a different subdomain
        echo.
        echo TUNNEL_SUBDOMAIN=chinese-scramble
        echo TUNNEL_PORT=8080
        echo.
        echo # Full tunnel URL will be: https://^${TUNNEL_SUBDOMAIN^}.loca.lt
        echo # Example: https://chinese-scramble.loca.lt
    ) > .env.tunnel
    set TUNNEL_SUBDOMAIN=chinese-scramble
    set TUNNEL_PORT=8080
)

echo ================================================================
echo.
echo     Chinese Word Scramble Game - Tunnel Setup
echo.
echo ================================================================
echo.

REM Check if localtunnel is installed
where lt >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Installing localtunnel globally...
    call npm install -g localtunnel
    echo Localtunnel installed!
    echo.
)

REM Start backend
echo Starting backend on port %TUNNEL_PORT%...
cd chinese-scramble-backend

REM Check if port is in use
netstat -ano | findstr ":%TUNNEL_PORT%" | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo Port %TUNNEL_PORT% is already in use
    set /p KILL_PROCESS="Kill existing process and restart? (y/n): "
    if /i "!KILL_PROCESS!"=="y" (
        echo Killing process on port %TUNNEL_PORT%...
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%TUNNEL_PORT%" ^| findstr "LISTENING"') do (
            taskkill /F /PID %%a >nul 2>nul
        )
        timeout /t 2 /nobreak >nul
    )
)

echo Building and starting Spring Boot application...
start /b mvnw.cmd spring-boot:run > ..\backend.log 2>&1
cd ..

REM Wait for backend to start
echo Waiting for backend to start (this may take 30-60 seconds)...
set COUNTER=0
:WAIT_LOOP
curl -s http://localhost:%TUNNEL_PORT%/actuator/health >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Backend started successfully!
    goto START_TUNNEL
)
set /a COUNTER+=1
if %COUNTER% GEQ 60 (
    echo Backend failed to start. Check backend.log for details.
    type backend.log | more
    exit /b 1
)
timeout /t 1 /nobreak >nul
goto WAIT_LOOP

:START_TUNNEL
echo.
echo ================================================================
echo.
echo Starting localtunnel...
echo Tunnel URL: https://%TUNNEL_SUBDOMAIN%.loca.lt
echo.

REM Start localtunnel
start /b lt --port %TUNNEL_PORT% --subdomain %TUNNEL_SUBDOMAIN%

timeout /t 3 /nobreak >nul

echo.
echo ================================================================
echo.
echo     Chinese Word Scramble Game is now LIVE!
echo.
echo ================================================================
echo.
echo Access your game at:
echo    Public URL:  https://%TUNNEL_SUBDOMAIN%.loca.lt
echo    Local URL:   http://localhost:%TUNNEL_PORT%
echo    Swagger UI:  https://%TUNNEL_SUBDOMAIN%.loca.lt/swagger-ui.html
echo.
echo First-time visitors will see a security page - click 'Continue'
echo.
echo Tips:
echo    - Change tunnel URL: Edit .env.tunnel file
echo    - View backend logs: type backend.log
echo    - Stop: Press Ctrl+C
echo.
echo ================================================================
echo.
echo Press Ctrl+C to stop all services...
echo.

REM Wait forever
:WAIT_FOREVER
timeout /t 60 /nobreak >nul
goto WAIT_FOREVER
