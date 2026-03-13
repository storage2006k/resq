#!/bin/bash
set -e

echo "═══════════════════════════════════════════"
echo "   RESQ — Production Deployment Script"
echo "   Target: Ubuntu 22.04 LTS (AWS EC2)"
echo "═══════════════════════════════════════════"

# ── 1. System Update ──
echo "[1/8] Updating system packages..."
sudo apt update && sudo apt upgrade -y

# ── 2. Install Docker ──
echo "[2/8] Installing Docker..."
sudo apt install -y docker.io docker-compose-plugin git curl wget
sudo systemctl enable docker && sudo systemctl start docker
sudo usermod -aG docker $USER

# ── 3. Install Certbot for SSL ──
echo "[3/8] Installing Certbot..."
sudo apt install -y certbot

# ── 4. Obtain SSL Certificate ──
echo "[4/8] Obtaining SSL certificate..."
read -p "Enter your domain (e.g., resq.yourdomain.com): " DOMAIN
sudo certbot certonly --standalone -d $DOMAIN

# Copy certs
mkdir -p nginx/ssl
sudo cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem ./nginx/ssl/
sudo cp /etc/letsencrypt/live/$DOMAIN/privkey.pem ./nginx/ssl/
sudo chmod 644 ./nginx/ssl/*.pem

# ── 5. Update nginx config with domain ──
echo "[5/8] Configuring nginx..."
sed -i "s/yourdomain.com/$DOMAIN/g" nginx/nginx.conf

# ── 6. Configure environment ──
echo "[6/8] Setting up environment..."
if [ ! -f .env ]; then
    cp .env.template .env
    echo ">>> IMPORTANT: Edit .env with your production credentials!"
    echo ">>> Run: nano .env"
    read -p "Press Enter after editing .env..."
fi

# ── 7. Build and deploy ──
echo "[7/8] Building and deploying..."
docker compose up -d --build

# Wait for services
echo "Waiting for services to start..."
sleep 15

# ── 8. Verify ──
echo "[8/8] Verifying deployment..."
docker compose ps

# Health check
echo ""
echo "Testing health endpoint..."
curl -s http://localhost:8080/actuator/health | head -1
echo ""

echo "═══════════════════════════════════════════"
echo "   DEPLOYMENT COMPLETE!"
echo "   App: https://$DOMAIN"
echo "   Health: https://$DOMAIN/actuator/health"
echo ""
echo "   Useful commands:"
echo "     docker compose logs -f resq-app"
echo "     docker compose restart resq-app"
echo "     docker compose down"
echo "═══════════════════════════════════════════"

# ── Setup auto-renew SSL ──
echo "Setting up SSL auto-renewal..."
(crontab -l 2>/dev/null; echo "0 3 * * 1 certbot renew --quiet && docker compose restart nginx") | crontab -
echo "SSL auto-renewal cron job added (weekly)."
