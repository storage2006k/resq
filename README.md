# ARIS — Emergency Ambulance Routing Intelligence System

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

Production-grade emergency ambulance dispatch and routing system with real-time tracking,
intelligent route optimization, and a military-grade dark command interface.

## Tech Stack

| Layer        | Technology                                       |
|-------------|--------------------------------------------------|
| Backend     | Java 17, Spring Boot 3.2, Spring Security (JWT)  |
| Database    | PostgreSQL 16 (H2 for local dev)                 |
| Frontend    | Thymeleaf + Leaflet.js + WebSocket (STOMP)       |
| Routing     | OSRM (Open Source Routing Machine)               |
| Geocoding   | Nominatim (OpenStreetMap)                        |
| Build       | Maven                                            |
| Deploy      | Docker + Docker Compose                          |

## Quick Start

### Option 1: Docker (Recommended)

```bash
docker-compose up --build
```

Open `http://localhost:8080`

### Option 2: Local Development

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Build and run (uses H2 in-memory database)
mvn spring-boot:run
```

Open `http://localhost:8080`

### Option 3: With Local PostgreSQL

```bash
# Set environment variables
set DB_URL=jdbc:postgresql://localhost:5432/arisdb
set DB_USERNAME=postgres
set DB_PASSWORD=your_password
set DB_DRIVER=org.postgresql.Driver
set HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

mvn spring-boot:run
```

## Default Credentials

| Username     | Password      | Role        |
|-------------|---------------|-------------|
| admin       | admin123      | COMMAND     |
| dispatcher  | dispatch123   | DISPATCHER  |
| coordinator | coord123      | COORDINATOR |
| supervisor  | super123      | SUPERVISOR  |

## API Endpoints

| Method | Endpoint               | Description                   | Auth |
|--------|------------------------|-------------------------------|------|
| POST   | /api/auth/login        | JWT authentication            | No   |
| GET    | /api/dashboard/stats   | Live dashboard statistics     | No   |
| GET    | /api/incidents         | Active incidents              | Yes  |
| POST   | /api/incidents         | Create new incident           | Yes  |
| GET    | /api/hospitals         | All hospitals with bed counts | Yes  |
| GET    | /api/ambulances        | All unit positions & statuses | Yes  |
| POST   | /api/dispatch          | Dispatch ambulance            | Yes  |
| GET    | /api/route             | Calculate optimal route       | Yes  |
| GET    | /api/geocode           | Nominatim geocoding proxy     | Yes  |
| GET    | /api/events            | Recent event logs             | Yes  |
| WS     | /ws → /topic/events    | Live event stream (STOMP)     | No   |

## Project Structure

```
src/main/java/com/aris/
├── config/        SecurityConfig, WebSocketConfig, JwtAuthFilter, DataSeeder
├── controller/    AuthController, IncidentController, HospitalController,
│                  AmbulanceController, RouteController, DashboardController,
│                  DispatchController, EventController, PageController
├── service/       JwtService, IncidentService, HospitalService, AmbulanceService,
│                  DispatchService, RoutingService, NominatimService
├── model/         User, Incident, Hospital, Ambulance, Dispatch, EventLog
├── repository/    JPA repositories for each entity
├── dto/           LoginRequest/Response, IncidentRequest, DispatchRequest, etc.
├── websocket/     EventBroadcaster
└── exception/     GlobalExceptionHandler
```
