# Stack Research

**Domain:** Neurodivergent-focused task/routine management Android app
**Researched:** 2026-01-28
**Confidence:** HIGH (verified against official docs and release pages)

## Recommended Stack

### Language & Compiler

| Component | Choice | Version | Rationale | Confidence |
|-----------|--------|---------|-----------|------------|
| Language | **Kotlin** | 2.3.0 | Standard for Android. K2 compiler stable. Multiplatform-ready for future web/iOS. | HIGH |
| Build | **Gradle + AGP** | AGP 8.12+ | Current stable Android Gradle Plugin. Kotlin 2.3.0 supports AGP 8.2.2–8.13.0. | HIGH |

### UI Framework

| Component | Choice | Version | Rationale | Confidence |
|-----------|--------|---------|-----------|------------|
| UI Toolkit | **Jetpack Compose** | BOM 2024.12+ | Declarative UI, Material 3 support, single-activity architecture. Industry standard for new Android apps. | HIGH |
| Design System | **Material Design 3** | compose-material3 | Dynamic theming (Material You), accessibility built-in. M3 Expressive for engaging interactions. | HIGH |
| Navigation | **Navigation Compose** | 2.8+ | Single-activity architecture with composable destinations. Type-safe navigation with Kotlin serialization. | HIGH |

### Data Layer

| Component | Choice | Version | Rationale | Confidence |
|-----------|--------|---------|-----------|------------|
| Database | **Room** | 2.7+ | Google's recommended ORM for local SQLite. Compile-time SQL verification, Flow integration, migration support. | HIGH |
| Date/Time | **kotlinx-datetime** | 0.7.1 | Multiplatform date/time library. Handles timezone, duration, intervals. Future-proof for KMP. | HIGH |
| Serialization | **kotlinx-serialization** | 1.7+ | Multiplatform JSON serialization. Used for data export/import, navigation args. | HIGH |

### Architecture

| Component | Choice | Version | Rationale | Confidence |
|-----------|--------|---------|-----------|------------|
| DI | **Koin** | 4.0+ | Lightweight, Kotlin-first DI. Simpler than Hilt for this project scale. KMP-compatible. | MEDIUM |
| State | **ViewModel + StateFlow** | lifecycle 2.8+ | Survives configuration changes. SavedStateHandle for process death. | HIGH |
| Async | **Kotlin Coroutines + Flow** | 1.9+ | Standard for async Android. Room returns Flow for reactive queries. | HIGH |

### Background Work

| Component | Choice | Version | Rationale | Confidence |
|-----------|--------|---------|-----------|------------|
| Scheduling | **WorkManager** | 2.10+ | Reliable background task scheduling. Survives device restarts. For notifications and task reminders. | HIGH |
| Precise Timing | **AlarmManager** | Android API | For time-critical reminders (medication cooldowns). WorkManager doesn't guarantee exact timing. | HIGH |

### Testing

| Component | Choice | Rationale |
|-----------|--------|-----------|
| Unit Tests | **JUnit 5 + MockK** | Standard Kotlin testing. MockK over Mockito for Kotlin coroutine support. |
| UI Tests | **Compose Testing** | compose-ui-test for composable testing. |
| Database Tests | **Room Testing** | In-memory database for fast tests. |

## What NOT to Use

| Technology | Why Not |
|------------|---------|
| **Flutter/React Native** | Cross-platform frameworks add abstraction. Kotlin + Compose is the native Android path AND has its own multiplatform story (KMP/CMP) for future expansion. |
| **Hilt/Dagger** | Overkill for this project scope. Annotation processing adds build time. Koin is simpler and KMP-compatible. |
| **SharedPreferences** | Room handles all structured data better. SharedPreferences only for simple key-value settings. |
| **XML Views** | Legacy UI system. Compose is the present and future. No reason to use XML for a greenfield project. |
| **Firebase** | No cloud backend needed for v1. Adds unnecessary complexity and dependency. |
| **Realm** | Room is Google-recommended and has better Compose/Flow integration. Realm adds SDK weight. |

## Cross-Platform Considerations

The stack is chosen with future KMP (Kotlin Multiplatform) expansion in mind:

- **Kotlin** → shared across all platforms
- **kotlinx-datetime** → multiplatform library
- **kotlinx-serialization** → multiplatform library
- **Koin** → KMP-compatible DI
- **Room** → KMP support added in Room 2.7
- **Compose Multiplatform** → 1.10.0 (Jan 2026) with iOS stable, web advancing

**Migration path:** Keep business logic and data layer in shared KMP modules from the start. UI layer stays platform-specific (Jetpack Compose for Android, Compose Multiplatform for iOS/web when needed).

## Minimum Android Version

- **Target SDK:** 35 (Android 15)
- **Min SDK:** 26 (Android 8.0) — covers 95%+ of active devices, provides modern notification channels and WorkManager support

## Sources

- [Kotlin 2.3.0 Release](https://blog.jetbrains.com/kotlin/2025/12/kotlin-2-3-0-released/)
- [Kotlin Releases](https://kotlinlang.org/docs/releases.html)
- [Compose Multiplatform 1.10.0](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)
- [Room KMP Support](https://developer.android.com/kotlin/multiplatform/room)
