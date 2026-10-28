# WiFiRent BD - Linux Deployment

## Prerequisites
- Java 21 JDK
- Maven 3.8+
- PostgreSQL 14+
- Git

## 1. Clone and Build

```bash
git clone <repository>
cd E:/wifirent-bd/backend
chmod +x mvnw
./mvnw clean package -DskipTests
```

## 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with production values
nano .env
```

## 3. Set Up Database

```bash
sudo -u postgres createdb wifirent_bd
sudo -u postgres psql -c "CREATE USER wifirent WITH PASSWORD 'changeme';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE wifirent_bd TO wifirent;"
```

## 4. Run as Service (systemd)

Create `/etc/systemd/system/wifirent-bd.service`:

```ini
[Unit]
Description=WiFiRent BD Backend
After=network.target postgresql.service

[Service]
Type=simple
User=wifirent
WorkingDirectory=/opt/wifirent-bd/backend
EnvironmentFile=/opt/wifirent-bd/backend/.env
ExecStart=/usr/bin/java -jar target/wifirent-bd-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable wifirent-bd
sudo systemctl start wifirent-bd
```

## 5. Reverse Proxy (Nginx)

```nginx
server {
    listen 80;
    server_name api.wifirentbd.example.com;

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /actuator/health {
        proxy_pass http://localhost:8081/actuator/health;
    }
}
```

## 6. Firewall

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp  # Only if needed directly
```
