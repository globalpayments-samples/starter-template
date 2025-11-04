#!/bin/bash

# Build Android app
cd "$(dirname "$0")"

echo "Current working directory: $(pwd)"
if [ "$(uname -s | grep -i 'mingw\|cygwin\|msys')" ]; then
  GRADLE_CMD="gradlew.bat"
else
  GRADLE_CMD="gradlew"
fi

if [ ! -f "$GRADLE_CMD" ]; then
  echo "ERROR: $GRADLE_CMD not found in android directory ($(pwd))."
  echo "To generate it, run: gradle wrapper"
  echo "Or open the project in Android Studio and sync Gradle."
  ls -l
  exit 1
fi
./$GRADLE_CMD assembleDebug
if [ $? -ne 0 ]; then
  echo "ERROR: Gradle build failed"
  exit 1
fi

 # Start Node.js backend


# Kill any process using port 8000 (cross-platform)
echo "Checking for processes using port 8000..."
if command -v lsof >/dev/null 2>&1; then
  PID=$(lsof -ti:8000 2>/dev/null || true)
  if [ -n "$PID" ]; then
    echo "Killing process $PID on port 8000."
    kill -9 $PID 2>/dev/null || true
  fi
elif command -v powershell.exe >/dev/null 2>&1; then
  PIDS=$(powershell.exe -Command "Get-NetTCPConnection -LocalPort 8000 -State Listen | Select-Object -ExpandProperty OwningProcess" 2>/dev/null || true)
  if [ -n "$PIDS" ]; then
    for PID in $PIDS; do
      echo "Killing process $PID on port 8000 (Windows PowerShell)."
      powershell.exe -Command "Stop-Process -Id $PID -Force" 2>/dev/null || true
    done
  fi
fi

echo ""
echo "🚀 Starting Node.js backend server..."
cd ../nodejs

# Start Node.js server in background (cross-platform compatible)
if [ "$(uname -s | grep -i 'mingw\|cygwin\|msys')" ]; then
  # Windows Git Bash - use detach mode
  node server.js > server.log 2>&1 &
  SERVER_PID=$!
  echo "✅ Backend started (PID: $SERVER_PID)"
  echo "📋 Backend logs: ../nodejs/server.log"
else
  # Unix/Linux - use nohup
  nohup node server.js > server.log 2>&1 &
  echo "✅ Backend started (logs in server.log)"
fi

sleep 2
cd ../android

# Print instructions for running on emulator/device
cat <<EOF

✅ Android app built successfully!
🚀 Backend server starting on port 8000...

To install and run on emulator:
  ./gradlew installDebug

Or open Android Studio and run the app.
EOF
