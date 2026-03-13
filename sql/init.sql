-- ═══════════════════════════════════════════════════
-- RESQ — Docker Compose Init Script
-- Only used when Flyway is disabled (direct pg init)
-- ═══════════════════════════════════════════════════

CREATE DATABASE resq_db;
GRANT ALL PRIVILEGES ON DATABASE resq_db TO resq_user;
