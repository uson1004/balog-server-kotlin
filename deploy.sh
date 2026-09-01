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

if ! command -v podman >/dev/null; then
  sudo dnf install -y podman
fi

set -a
. "$APP_DIR/.env"
set +a

if ! podman container exists balog-mysql; then
  podman run -d --name balog-mysql --restart=always \
    -e MYSQL_DATABASE=balog \
    -e MYSQL_ROOT_PASSWORD \
    -p 127.0.0.1:3307:3306 \
    -v balog-mysql:/var/lib/mysql \
    docker.io/library/mysql:8.4
else
  podman start balog-mysql >/dev/null 2>&1 || true
fi

if ! podman container exists balog-redis; then
  podman run -d --name balog-redis --restart=always \
    -p 127.0.0.1:6380:6379 \
    -v balog-redis:/data \
    docker.io/library/redis:7
else
  podman start balog-redis >/dev/null 2>&1 || true
fi

for _ in {1..30}; do
  if podman exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" balog-mysql mysqladmin ping -h localhost --silent && podman exec balog-redis redis-cli ping; then
    break
  fi
  sleep 2
done

podman exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" balog-mysql mysqladmin ping -h localhost --silent
podman exec balog-redis redis-cli ping >/dev/null

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
