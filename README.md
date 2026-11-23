# HostelManager

HostelManager is a modern Android application built with Jetpack Compose, Kotlin Coroutines & Flow, and Room Database to manage hostel facilities, residents, stays, room assignments, payment recording, and expense tracking.

## Features
- **Hostels & Rooms Management**: Add and organize hostels, rooms, and bed occupancies.
- **Resident Lifecycle Management**: Manage active residents, past residents, room transfers, check-outs, and re-admissions with full history preservation.
- **Payment & Expense Tracking**: Record resident payments, track billing cycles, calculate due balances, and manage operational expenses.
- **Reports & Analytics**: Generate hostel financial summaries, monthly occupancy rates, and rent collection reports.
- **Dark Mode Support**: Material 3 adaptive light and dark color schemes.
- **Repository Architecture**: Clean architecture with Kotlin repositories, dependency injection container, and room transactions.

## Tech Stack & Architecture
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Repository Pattern (`AppContainer`)
- **Database**: Room Database (with KSP schema generation and `@Transaction` safety)
- **Async & Reactive**: Kotlin Coroutines & `StateFlow` / `collectAsStateWithLifecycle`
- **Build System**: AGP 9.5+, Gradle Version Catalog (`libs.versions.toml`)

## Building and Running
1. Open the project in Android Studio (2026.2.2 or higher recommended).
2. Sync the project with Gradle files.
3. Build & run on an Android emulator or physical device running API level 29+.

## Testing
Run unit tests with Gradle:
```bash
./gradlew test
```
Run Room instrumentation tests on a device/emulator:
```bash
./gradlew connectedAndroidTest
```
