# Paper Crown

A local-first desktop roguelike game based on Rock-Paper-Scissors (Batu, Gunting, Kertas). Fight a random bot opponent through multiple runs, collect buffs, survive HP-based progression, unlock achievements, and build persistent statistics.

This project demonstrates core OOP concepts — **inheritance**, **polymorphism**, **encapsulation**, **abstraction**, **error handling**, and **JavaFX GUI** — using a Java 21 multi-module Gradle architecture.

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

- JDK 21
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
chmod +x gradlew
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

## OOP Concepts Demonstrated

### 1. Inheritance

#### 1.1 JavaFX View Hierarchy

All desktop UI classes extend JavaFX layout primitives, inheriting layout management, CSS styling, node traversal, and event dispatch:

| Class | Parent | File |
|-------|--------|------|
| `PlayView` | `VBox` | `desktop-client/.../view/PlayView.java` (960 lines) |
| `DashboardView` | `VBox` | `desktop-client/.../view/DashboardView.java` |
| `HistoryView` | `VBox` | `desktop-client/.../view/HistoryView.java` |
| `AchievementsView` | `VBox` | `desktop-client/.../view/AchievementsView.java` |
| `SettingsView` | `VBox` | `desktop-client/.../view/SettingsView.java` |
| `MainView` | `BorderPane` | `desktop-client/.../view/MainView.java` |
| `SidebarItem` | `HBox` | `desktop-client/.../view/SidebarItem.java` |
| `StatCard` | `VBox` | `desktop-client/.../component/StatCard.java` |
| `RunCard` | `VBox` | `desktop-client/.../component/RunCard.java` |
| `AchievementCard` | `VBox` | `desktop-client/.../component/AchievementCard.java` |
| `BuffCard` | `VBox` | `desktop-client/.../component/BuffCard.java` |
| `ChartContainer` | `StackPane` | `desktop-client/.../component/ChartContainer.java` |
| `Toast` | `HBox` | `desktop-client/.../component/Toast.java` |
| `PaperCrownApp` | `Application` | `desktop-client/.../PaperCrownApp.java` |

#### 1.2 Repository Interface Inheritance

All six repository interfaces extend `JpaRepository<T, ID>`, inheriting `findAll()`, `findById()`, `save()`, `delete()`, `count()`, and pagination/query-by-example support **without writing a single line of implementation code**:

| Interface | Extends | File |
|-----------|---------|------|
| `RunRepository` | `JpaRepository<RunEntity, Long>` | `backend-service/.../repository/RunRepository.java` |
| `RoundRepository` | `JpaRepository<RoundEntity, Long>` | `backend-service/.../repository/RoundRepository.java` |
| `BuffRepository` | `JpaRepository<BuffEntity, Long>` | `backend-service/.../repository/BuffRepository.java` |
| `RunBuffRepository` | `JpaRepository<RunBuffEntity, Long>` | `backend-service/.../repository/RunBuffRepository.java` |
| `AchievementRepository` | `JpaRepository<AchievementEntity, Long>` | `backend-service/.../repository/AchievementRepository.java` |
| `SettingRepository` | `JpaRepository<SettingEntity, Long>` | `backend-service/.../repository/SettingRepository.java` |

Custom query methods (e.g. `findTopByStatusOrderByCreatedAtDesc(RunStatus)`, `findBySettingKey(String)`) are declared by naming convention and implemented automatically by Spring Data.

#### 1.3 Enum Inheritance

All four shared enums — `Move`, `BuffType`, `RoundOutcome`, `RunStatus` — implicitly extend `java.lang.Enum<E>`, inheriting `name()`, `ordinal()`, `valueOf()`, `values()`, and `compareTo()`.

#### 1.4 JPA Entity Classes

Six `@Entity`-annotated classes (`RunEntity`, `RoundEntity`, `BuffEntity`, `RunBuffEntity`, `AchievementEntity`, `SettingEntity`) inherit object-relational mapping, lifecycle callbacks, and dirty-checking from JPA/Hibernate via class-level annotation — no code duplicated across them.

---

### 2. Polymorphism

#### 2.1 Runtime Polymorphism — Switch on Enums (Behavior Dispatch)

Backend and desktop both use enum-based `switch` to dispatch behavior at runtime:

**Backend (domain logic):**

| Location | Enum | File & Line | What It Distinguishes |
|----------|------|-------------|----------------------|
| `RunService.submitMove()` | `RoundOutcome` | `RunService.java:113-131` | WIN → heal, LOSS → check shield/IgnoreLoss → decrement HP, DRAW → check DrawAsWin |
| `BuffService.applyBuff()` | `effectKey` (String) | `BuffService.java:47-61` | 8 buff effects: +MaxHP, Heal, Shield, DoubleReward, StreakBonus, Reroll, DrawAsWin, IgnoreLoss |
| `AchievementService.checkAchievements()` | `criteriaType` (String) | `AchievementService.java:62-70` | 6 criteria: TotalWins, RunsCompleted, TotalRounds, RoundsSurvived, WinStreak, RunsWon |

**Desktop (UI rendering):**

| Location | Enum/String | File & Line | What It Distinguishes |
|----------|-------------|-------------|----------------------|
| `PlayView.showResult()` | `RoundOutcome` | `PlayView.java:450-477` | Different audio, CSS class, and animation per outcome |
| `PlayView.updateHistory()` | `RoundOutcome` | `PlayView.java:841-851` | Icon (trophy/skull/minus) and color per round result |
| `RunCard.createRoundRow()` | `RoundOutcome` | `RunCard.java:77-80` | Unicode symbol (✓/✗/—) per outcome |
| `SidebarItem.resolveIcon()` | `iconStr` (String) | `SidebarItem.java:20-27` | Maps icon key to Ikonli material design icon |
| `Toast` constructor | `Toast.Type` (enum) | `Toast.java:26-38` | FontAwesome icon and CSS class per notification type |
| `DashboardView.createStatCard()` | `label` (String) | `DashboardView.java:149-155` | FontAwesome icon per stat label |
| `AchievementCard.resolveIcon()` | `iconStr` (String) | `AchievementCard.java:59-71` | Maps icon key to FontAwesome icon |
| `BuffCard.resolveIcon()` | `iconStr` (String) | `BuffCard.java:52-61` | Maps icon key to FontAwesome icon |

#### 2.2 Runtime Polymorphism — `instanceof` Type Checking

- `ChartContainer.applyTheme()` (`ChartContainer.java:52-82`) — uses `instanceof` to style `CategoryPlot`, `XYPlot`, and `PiePlot` differently (each plot type needs different axis/legend/renderer treatment).

#### 2.3 Interface-Based Polymorphism (Dependency Injection)

Spring injects JDK dynamic proxies for every repository interface at runtime:

- `RunService` constructor receives `RunRepository`, `RoundRepository`, `BuffRepository`, `RunBuffRepository` as interface types — the actual proxy class is generated by Spring Data, completely transparent to the caller.
- Tests use Mockito `@Mock` to substitute interface implementations without changing production code.

#### 2.4 Compile-Time Polymorphism — Method Overloading

| Class | Overloaded Methods | File & Line | Purpose |
|-------|--------------------|-------------|---------|
| `StatCard` | `setValue(String)` / `setValue(int)` / `setValue(double)` | `StatCard.java:48-62` | Accept multiple numeric types; formats doubles with 1 decimal place |
| `SettingsController` | `updateSetting(String key, String value)` / `updateSettings(Map<String, String>)` | `SettingsController.java:34,39` | Single key-value update vs. batch update |
| `EntityMapper` | `toRunDTO(RunEntity)` / `toRunSummaryDTO(RunEntity)` | `EntityMapper.java:14,41` | Full DTO (with rounds+buffs) vs. summary-only DTO |
| `Toast` | `show(parent, text, type)` / `show(parent, text, type, duration)` | `Toast.java:46,50` | Default 4s display vs. custom display duration |
| `BackendClient` | `get(String)` / `get(String, Class<T>)` | `BackendClient.java:106,124` | Raw JSON string vs. deserialized typed response |

---

### 3. Encapsulation

#### 3.1 Private Fields with Accessors

**All 6 JPA entities** follow the JavaBean encapsulation contract:

| Entity | Private Fields | Getter/Setter Pairs | File & Lines |
|--------|----------------|-------------------|--------------|
| `RunEntity` | 11 fields (id, status, currentHp, maxHp, roundNumber, totalWins, totalLosses, totalDraws, shield, createdAt, endedAt) + 2 collections | 13 pairs | `RunEntity.java:15-93` |
| `RoundEntity` | 6 fields (id, run, roundNumber, playerMove, botMove, outcome, createdAt) | 7 pairs | `RoundEntity.java:12-62` |
| `BuffEntity` | 6 fields (id, name, description, buffType, effectKey, icon) | 6 pairs | `BuffEntity.java:10-51` |
| `RunBuffEntity` | 5 fields (id, run, buff, appliedAt, usedAt, consumed) | 6 pairs | `RunBuffEntity.java:10-51` |
| `AchievementEntity` | 8 fields (id, name, description, icon, criteriaType, criteriaValue, unlocked, unlockedAt, progress) | 9 pairs | `AchievementEntity.java:9-68` |
| `SettingEntity` | 3 fields (id, settingKey, settingValue) | 3 pairs | `SettingEntity.java:10-31` |

**All 8 shared DTOs** (`MoveRequest`, `MoveResponse`, `RunDTO`, `RoundDTO`, `BuffDTO`, `AchievementDTO`, `StatsDTO`, `SettingDTO`) also use private fields exposed exclusively through getters and setters — no public fields anywhere in the shared module.

#### 3.2 Package-Private Internals (Test Seams)

| Class | Method | File & Line | Visibility |
|-------|--------|-------------|------------|
| `GameEngine` | `setRandomForTesting(Random)` | `GameEngine.java:33` | Package-private — only accessible within `com.papercrown.backend.service` (production code + tests) |
| `BuffService` | `setRandomForTesting(Random)` | `BuffService.java:70` | Package-private — same pattern for deterministic test runs |

Both expose deterministic randomness to tests while **hiding test-seeding from the public API** — callers from other packages cannot inject randomness.

#### 3.3 Hidden Implementation Complexity

| Class | Encapsulates | File & Lines |
|-------|-------------|--------------|
| `BackendClient` | HTTP connection timeout config, `HttpClient` lifecycle, JSON serialization/deserialization, HTTP status code interpretation, typed response parsing — all inside private `get()`, `post()`, `put()` methods. Callers see only `startRun()`, `submitMove()`, `selectBuff()`, `getStats()`. | `BackendClient.java:106-179` |
| `ChartContainer` | Swing-JavaFX interop (`SwingNode`), `JFreeChart` construction, dark-theme application across plot types, font/color/padding configuration — all internal. Exposes a clean JavaFX `Node` to layout code. | `ChartContainer.java:15-114` |
| `AudioManager` | Sound file loading from resources, `javafx.scene.media.MediaPlayer` pooling, volume management, playback status caching — all private. Only `play()`, `setSoundEnabled()`, `setMasterVolume()` are public. | `AudioManager.java:11-64` |
| `StatsService` | Run aggregation, win-rate computation, best-strike calculation, move-usage counting across multiple runs — `getStats()` returns a clean `StatsDTO`. No raw entities leak to callers. | `StatsService.java:27-74` |
| `PlayViewModel` | Private `ExecutorService` for async HTTP calls, `Platform.runLater()` for JavaFX thread updates, observable-property wiring — callers bind UI to `StringProperty`/`BooleanProperty` without knowing about threads or HTTP. | `PlayViewModel.java:18-125` |

---

### 4. Abstraction

#### 4.1 Repository Pattern (Data Access Abstraction)

Spring Data JPA generates implementation at runtime from method-name convention. The developer declares **what** data to query — never **how**:

| Repository | Custom Query Methods Declared | File & Line |
|------------|-------------------------------|-------------|
| `RunRepository` | `findTopByStatusOrderByCreatedAtDesc(RunStatus)`, `findByStatus(RunStatus)`, `countByStatus(RunStatus)` | `RunRepository.java:13-20` |
| `RoundRepository` | `findByRunIdOrderByRoundNumberAsc(Long)`, `findByRunId(Long)` | `RoundRepository.java:12-16` |
| `BuffRepository` | `findByBuffType(BuffType)`, `findByEffectKey(String)` | `BuffRepository.java:8-13` |
| `RunBuffRepository` | `findByRunIdAndConsumedFalse(Long)` | `RunBuffRepository.java:10-13` |
| `AchievementRepository` | (inherits all from JpaRepository) | `AchievementRepository.java:10` |
| `SettingRepository` | `findBySettingKey(String)` | `SettingRepository.java:10-12` |

#### 4.2 Service Layer (Business Logic Abstraction)

Six services hide their own domain complexity behind clean method signatures:

| Service | Responsibility | Key Public Methods | File & Lines |
|---------|---------------|-------------------|--------------|
| `GameEngine` | Core RPS resolution rules | `resolve(Move, Move) → RoundOutcome`, `randomBotMove() → Move` | `GameEngine.java:11-36` |
| `RunService` | Full run lifecycle orchestration | `startRun()`, `submitMove()`, `selectBuff()`, `abandonRun()`, `getUnfinishedRun()` | `RunService.java:19-217` |
| `BuffService` | Buff catalog + effect resolution | `getRandomBuffChoice()`, `applyBuff()` | `BuffService.java:18-70` |
| `AchievementService` | Multi-criteria progress + unlock detection | `getAllAchievements()`, `checkAchievements()` | `AchievementService.java:22-107` |
| `StatsService` | Aggregate statistics across runs | `getStats() → StatsDTO` | `StatsService.java:17-74` |
| `SettingsService` | Key-value configuration | `getSettings()`, `getSetting()`, `updateSetting()`, `updateSettings()` | `SettingsService.java:16-62` |

Callers (controllers, desktops) use these services without needing to understand RPS win maps, buff effect side-effects, or database queries.

#### 4.3 Controller Layer (REST API Abstraction)

Five controllers expose clean REST endpoints. They delegate to services and never contain business logic:

| Controller | Endpoints | File & Lines |
|------------|-----------|--------------|
| `RunController` | `POST /api/runs`, `POST /api/runs/{id}/round`, `POST /api/runs/{id}/buff`, `GET /api/runs/unfinished`, `DELETE /api/runs/{id}` | `RunController.java:16-74` |
| `StatsController` | `GET /api/stats` | `StatsController.java:12-24` |
| `AchievementController` | `GET /api/achievements` | `AchievementController.java:14-26` |
| `SettingsController` | `GET /api/settings`, `GET /api/settings/{key}`, `PUT /api/settings`, `PUT /api/settings/{key}` | `SettingsController.java:11-47` |
| `HealthController` | `GET /api/health` | `HealthController.java:10-16` |

#### 4.4 Entity-DTO Mapping Abstraction

`EntityMapper` provides typed conversion methods (`toRunDTO(RunEntity)`, `toRunSummaryDTO(RunEntity)`, `toRoundDTO(RoundEntity)`, `toBuffDTO(BuffEntity)`, `toAchievementDTO(AchievementEntity)`) — services and controllers call these without dealing with JPA entity internals, `@OneToMany` collections, or lazy-loading concerns.

---

### 5. Error Handling & Exceptions

#### 5.1 Centralized Global Exception Handler

`GlobalExceptionHandler` (`backend-service/.../exception/GlobalExceptionHandler.java:12-38`) uses `@ControllerAdvice` to intercept exceptions from **all** controllers at a single location. No controller contains try-catch blocks:

| `@ExceptionHandler` | Java Exception | → HTTP Status | Lines |
|---------------------|----------------|---------------|-------|
| `handleNotFound` | `NoSuchElementException` | 404 NOT_FOUND | 14-18 |
| `handleBadState` | `IllegalStateException` | 409 CONFLICT | 20-24 |
| `handleBadArgument` | `IllegalArgumentException` | 400 BAD_REQUEST | 26-30 |
| `handleGeneral` | `Exception` (catch-all) | 500 INTERNAL_SERVER_ERROR | 32-37 |

Each handler returns a JSON body `{"error": "…message…"}`. No custom exception subclasses are needed — standard `java.lang` exceptions with descriptive messages suffice.

#### 5.2 Explicit Exception Throwing (Business Rules)

| Location | Exception | Condition | File & Line |
|----------|-----------|-----------|-------------|
| `RunService.startRun()` | `IllegalStateException` | An unfinished run already exists | `RunService.java:52` |
| `RunService.getRunById()` | `NoSuchElementException` | Run ID not found (via `.orElseThrow()`) | `RunService.java:80` |
| `RunService.submitMove()` | `NoSuchElementException` | Run ID not found | `RunService.java:92` |
| `RunService.submitMove()` | `IllegalStateException` | Run is already completed | `RunService.java:95` |
| `RunService.submitMove()` | `IllegalArgumentException` | Player move is null | `RunService.java:99` |
| `RunService.selectBuff()` | `NoSuchElementException` | Run or buff ID not found | `RunService.java:167-171` |
| `RunService.abandonRun()` | `NoSuchElementException` | Run ID not found | `RunService.java:200` |
| `BuffService.applyBuff()` | `IllegalArgumentException` | Unknown buff effectKey | `BuffService.java:62` |

These exceptions propagate to `GlobalExceptionHandler` without any intermediate catching — clean separation between throwing (services) and handling (global handler).

#### 5.3 Desktop Client — Graceful Degradation

All ViewModels catch backend failures to prevent crashes and keep the UI responsive:

| Location | Behavior | File & Line |
|----------|----------|-------------|
| `BackendClient.isHealthy()` | Returns `false` on any exception — no crash | `BackendClient.java:34-41` |
| `BackendClient.getUnfinishedRun()` | Returns `null` on failure — UI shows "no active run" | `BackendClient.java:49-53` |
| `BackendClient.get()` | Wraps `IOException`/`InterruptedException` in `RuntimeException` with status code | `BackendClient.java:107-121` |
| `BackendClient.post()` | Wraps HTTP failures and JSON parse errors in `RuntimeException` | `BackendClient.java:140-158` |
| `BackendClient.put()` | Wraps non-2xx responses in `RuntimeException` | `BackendClient.java:166-174` |
| `PlayViewModel.startNewRun()` | Catches errors and sets observable `error` property for UI display | `PlayViewModel.java:64-65` |
| `PlayViewModel.submitMove()` | Catches failures silently (retry on next user action) | `PlayViewModel.java:53,81` |
| `PlayViewModel.startNewRun()` | `finally` block always resets `loading` — UI never gets stuck | `PlayViewModel.java:66-68` |

---

### 6. Design Patterns

#### 6.1 MVVM (Model-View-ViewModel)

The desktop client follows MVVM with **no FXML** — all UI is built programmatically:

| Layer | Role | Key Classes |
|-------|------|-------------|
| **Model** | Data + backend communication | `BackendClient`, shared DTOs, Spring Boot services |
| **View** | Layout construction, CSS, animations, property binding | `MainView`, `PlayView`, `DashboardView`, `HistoryView`, `AchievementsView`, `SettingsView` |
| **ViewModel** | Observable JavaFX properties, async operation management, state mutation | `PlayViewModel`, `DashboardViewModel`, `HistoryViewModel`, `AchievementsViewModel`, `SettingsViewModel` |

Views bind directly to ViewModel properties (e.g. `textProperty().bind(viewModel.messageProperty())`) and never call backend methods directly. ViewModels never reference UI classes — they only expose `StringProperty`/`BooleanProperty`/`ObjectProperty`.

#### 6.2 Repository Pattern

Six Spring Data `JpaRepository` implementations abstract all database access. Services declare dependencies on interfaces — Spring injects runtime proxy implementations. This decouples business logic from persistence.

#### 6.3 Dependency Injection (IoC)

`RunService` constructor (`RunService.java:34-38`) receives `RunRepository`, `RoundRepository`, `BuffRepository`, `RunBuffRepository`, `GameEngine`, `BuffService`, `AchievementService`, `StatsService` — all injected by Spring. No `new` keyword for dependencies. This enables:
- Unit testing with Mockito mocks
- Swapping implementations without changing service code
- Lifecycle management delegated to the Spring container

#### 6.4 Observable/Observer (via JavaFX Properties)

All ViewModels expose 4–14 `public final` JavaFX properties (`StringProperty`, `BooleanProperty`, `IntegerProperty`, `ObjectProperty`, `ListProperty`). Views bind their text, visibility, and style properties to these observables, receiving automatic updates when the ViewModel changes state. This is the JavaFX-native implementation of the Observer pattern.

#### 6.5 Strategy (via Enum Switch Dispatch)

`BuffService.applyBuff()` uses a `switch` on the buff `effectKey` string to select the correct strategy for each buff type (heal, shield, max-hp-up, etc.). Each `case` block implements a different algorithm — same interface, interchangeable behavior at runtime.

---

### 7. JavaFX GUI (Programmatic, No FXML)

**Reusable components** (`desktop-client/.../component/`):

| Component | Description |
|-----------|-------------|
| `StatCard` | Numeric stat display with icon, accent colors, and CSS pseudo-classes. Overloaded `setValue()` accepts `String`, `int`, or `double`. |
| `RunCard` | Expandable/collapsible run entry showing rounds, outcomes, buffs, and HP trajectory. |
| `AchievementCard` | Three visual states — unlocked (full color), in-progress (dimmed + progress bar), locked (greyed out + lock icon). |
| `BuffCard` | Buff selection card with icon, name, description, hover scale animation, and click handler. |
| `ChartContainer` | Wraps JFreeChart in a `SwingNode` for JavaFX embedding. Accepts any `JFreeChart`, applies dark theme via `instanceof` on plot type. |
| `Toast` | Animated notification sliding in from the right edge with auto-dismiss after configurable duration. |

**Animations** — Win celebration (scalable particle burst), shake on loss, fade-in page transitions, staggered card entrance via sequential `TranslateTransition` + `FadeTransition`.

**Styling** — Integrated dark fantasy theme with root CSS variables, pseudo-classes for interactive states, and hover transitions.

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
paper-crown/
├── desktop-client/                   # JavaFX desktop application
│   └── src/main/java/com/papercrown/desktop/
│       ├── component/                # Reusable UI components
│       │   ├── StatCard.java         #   Stats display card
│       │   ├── RunCard.java          #   Collapsible run entry
│       │   ├── AchievementCard.java  #   Achievement tile
│       │   ├── BuffCard.java         #   Buff selection card
│       │   ├── ChartContainer.java   #   JFreeChart wrapper
│       │   └── Toast.java            #   Animated notification
│       ├── service/                  # Backend HTTP client
│       │   └── BackendClient.java    #   REST API access layer
│       ├── util/                     # Audio manager
│       │   └── AudioManager.java     #   Sound playback
│       ├── view/                     # JavaFX views (MVVM)
│       │   ├── MainView.java         #   Root navigation shell
│       │   ├── SidebarItem.java      #   Sidebar nav button
│       │   ├── PlayView.java         #   Game play screen
│       │   ├── DashboardView.java    #   Stats overview
│       │   ├── HistoryView.java      #   Run history
│       │   ├── AchievementsView.java #   Achievement gallery
│       │   └── SettingsView.java     #   Settings page
│       ├── viewmodel/                # ViewModel layer
│       │   ├── PlayViewModel.java    #   Game state & actions
│       │   ├── DashboardViewModel.java
│       │   ├── HistoryViewModel.java
│       │   ├── AchievementsViewModel.java
│       │   └── SettingsViewModel.java
│       └── PaperCrownApp.java        # JavaFX entry point
├── backend-service/                  # Spring Boot REST API
│   └── src/main/java/com/papercrown/backend/
│       ├── config/                   # CORS configuration
│       ├── controller/               # REST controllers
│       │   ├── RunController.java
│       │   ├── StatsController.java
│       │   ├── AchievementController.java
│       │   └── SettingsController.java
│       ├── entity/                   # JPA entities
│       │   ├── RunEntity.java
│       │   ├── RoundEntity.java
│       │   ├── BuffEntity.java
│       │   ├── RunBuffEntity.java
│       │   ├── AchievementEntity.java
│       │   └── SettingEntity.java
│       ├── exception/                # Error handling
│       │   └── GlobalExceptionHandler.java
│       ├── mapper/                   # Entity-DTO mapping
│       │   └── EntityMapper.java
│       ├── repository/               # JPA repositories
│       │   ├── RunRepository.java
│       │   ├── RoundRepository.java
│       │   ├── BuffRepository.java
│       │   └── ...
│       └── service/                  # Business logic
│           ├── GameEngine.java       #   RPS resolution
│           ├── RunService.java        #   Run lifecycle
│           ├── BuffService.java       #   Buff effects
│           ├── StatsService.java      #   Statistics
│           ├── AchievementService.java#   Achievements
│           └── SettingsService.java   #   Settings
├── shared/                           # Shared DTOs and enums
│   └── src/main/java/com/papercrown/shared/
│       ├── dto/                      # Data transfer objects
│       │   ├── MoveRequest.java
│       │   ├── MoveResponse.java
│       │   ├── RunDTO.java
│       │   ├── RoundDTO.java
│       │   ├── StatsDTO.java
│       │   ├── AchievementDTO.java
│       │   ├── BuffDTO.java
│       │   └── SettingDTO.java
│       └── enums/                    # Shared enumerations
│           ├── Move.java
│           ├── RoundOutcome.java
│           ├── RunStatus.java
│           └── BuffType.java
├── docker/                           # Docker Compose for PostgreSQL
├── infra/                            # Setup scripts
├── DESIGN.md                         # Design documentation
├── PRODUCT.md                        # Product context
├── TODO.md                           # Roadmap
└── AGENTS.md                         # Agent guidelines
```

## Settings

- Fullscreen, volume, sound effects, and animations are configurable in-app
- Settings persist across restarts via the backend API
