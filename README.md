# Paper Crown

A local-first desktop roguelike game based on Rock-Paper-Scissors (Batu, Gunting, Kertas). Fight a random bot opponent through multiple runs, collect buffs, survive HP-based progression, unlock achievements, and build persistent statistics.

## Architecture

```text
JavaFX Desktop  ──REST──>  Spring Boot Backend  ──>  PostgreSQL
    (MVVM)                     (Service Layer)         (via Flyway)
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| Desktop Client | JavaFX 23, Ikonli, JFreeChart |
| Backend Service | Spring Boot 3.4.3, JPA/Hibernate |
| Database | PostgreSQL 16 via Docker |
| Shared Contracts | Multi-module Gradle project |
| Testing | JUnit 5, Mockito |

> Runtime note: project ini bisa dijalankan dengan Java 21. Source/bytecode tetap ditargetkan ke Java 21 agar kompatibel dengan Spring Boot 3.4.3.

## Prerequisites

Pastikan sudah terpasang:

- JDK 26
- Gradle
- Docker dan Docker Compose
- Git

Cek versi Java dan Gradle:

```bash
java --version
gradle --version
```

> Project ini tetap menyediakan Gradle Wrapper (`./gradlew`). Command di bawah memakai wrapper agar versi Gradle yang digunakan konsisten dengan project.

## Quick Start

Jalankan semua command dari root repository `paper-crown/`.

### 1. Prepare Gradle Wrapper

Jalankan perintah Gradle Wrapper terlebih dahulu, lalu pastikan file `gradlew` bisa dieksekusi:

```bash
gradle wrapper
./gradlew --version
```

### 2. Start PostgreSQL

```bash
docker compose -f docker/docker-compose.yml up -d
```

Cek database sudah hidup:

```bash
docker compose -f docker/docker-compose.yml ps
```

### 3. Start Backend

Buka terminal pertama:

```bash
./gradlew :backend-service:bootRun
```

Backend berjalan di:

```text
http://localhost:8080
```

### 4. Launch Desktop Client

Buka terminal kedua, lalu jalankan:

```bash
./gradlew :desktop-client:run
```

Pastikan backend di terminal pertama tetap berjalan saat desktop client dibuka.

## Build & Test

```bash
./gradlew build                       # Compile all modules + run tests
./gradlew test                        # Run all tests
./gradlew :backend-service:bootRun    # Start backend API
./gradlew :desktop-client:run         # Launch JavaFX desktop client
```

## Game Rules

- You choose **Rock**, **Paper**, or **Scissors** each round
- Bot chooses randomly
- **Win** → survive the round
- **Loss** → lose 1 HP
- **Draw** → no HP loss
- **0 HP** → run ends

### Buffs

Every few rounds, choose from 3 random buffs:

| Type | Examples |
|------|----------|
| Survival | +1 Max HP, Heal 1 HP, Shield |
| Scoring | Double reward, Bonus streak points |
| Utility | Reroll token, Draw counts as win, Ignore loss |

### Achievements

11 achievements across 5 criteria types — milestones auto-unlock as you play.

## Project Structure

```text
├── desktop-client/        # JavaFX desktop application
│   └── src/main/java/.../
│       ├── component/     # Reusable UI components
│       ├── service/       # Backend HTTP client
│       ├── util/          # Audio manager
│       ├── view/          # JavaFX views (MVVM)
│       ├── viewmodel/     # ViewModel layer
│       └── PaperCrownApp.java
├── backend-service/       # Spring Boot REST API
│   └── src/main/java/.../
│       ├── controller/    # REST controllers
│       ├── service/       # Business logic
│       ├── repository/    # JPA repositories
│       ├── entity/        # JPA entities
│       └── config/        # CORS, exception handling
├── shared/                # DTOs and enums
├── docker/                # Docker Compose for PostgreSQL
└── infra/                 # Setup scripts
```

## Settings

- Fullscreen, volume, sound effects, and animations are configurable in-app
- Settings persist across restarts via the backend API
