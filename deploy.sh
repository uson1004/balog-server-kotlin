#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/home/opc/app
JAR_PATH=$(find "$APP_DIR/build/libs" -maxdepth 1 -type f -name '*.jar' -print -quit)

if [[ -z "$JAR_PATH" ]]; then
  echo "No application JAR found"
  exit 1
fi

if ! command -v java >/dev/null; then
  sudo dnf install -y java-17-openjdk-headless
fi

if systemctl is-active --quiet firewalld; then
  sudo firewall-cmd --permanent --add-port=8080/tcp
  sudo firewall-cmd --reload
fi

if [[ -f "$APP_DIR/application.pid" ]] && kill -0 "$(cat "$APP_DIR/application.pid")" 2>/dev/null; then
  kill "$(cat "$APP_DIR/application.pid")"
fi

cd "$APP_DIR"
nohup java -jar "$JAR_PATH" > application.log 2>&1 &
echo $! > application.pid
sleep 10
kill -0 "$(cat application.pid)"
