# Routine Tool

A focused task management Android app designed to help you capture tasks effortlessly and stay focused on what matters.

## Build Requirements

- JDK 17 or higher
- Android SDK with API level 35
- Gradle 8.11.1 (provided via wrapper)

## Build Instructions

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run app
adb shell am start -n com.routinetool/.MainActivity
```

## Project Structure

- `app/src/main/java/com/routinetool/` - Main application code
  - `data/` - Data layer (Room database, entities, DAOs, repositories)
  - `ui/` - UI layer (Compose screens, themes, components)
  - `di/` - Dependency injection (Koin modules)
- `gradle/libs.versions.toml` - Centralized dependency version catalog

## Tech Stack

- Kotlin 2.3.0
- Jetpack Compose (Material 3)
- Room Database 2.7.0
- Koin 4.0.0 (Dependency Injection)
- Navigation Compose 2.8.5
- kotlinx-datetime 0.7.1

## Development Status

Phase 1: Foundation & Quick Capture (In Progress)
- [x] Project structure and dependencies
- [x] Room database with Task entity
- [x] Repository pattern with Koin DI
- [x] Material 3 theme (monochrome + accent)
- [ ] Quick capture UI
- [ ] Task list with deadline ordering
- [ ] Task completion flow
