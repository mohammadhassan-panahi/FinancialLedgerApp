# AGENTS.md

This repository is an Android app for tracking personal finances, portfolio data, security settings, and market/crypto analysis. The codebase is Kotlin-first and uses Jetpack Compose, Room, DataStore, WorkManager, and Android security APIs.

## Quick start

- Primary app module: [app](app)
- App configuration: [app/build.gradle.kts](app/build.gradle.kts)
- Project docs: [README.md](README.md)
- Root Gradle settings: [settings.gradle.kts](settings.gradle.kts)

Use these commands from the repo root:

- Build the app: `./gradlew assembleDebug`
- Run unit tests: `./gradlew testDebugUnitTest`
- Run all Gradle tasks: `./gradlew tasks`

## Architecture and key directories

- App entrypoint and security bootstrap: [app/src/main/java/com/example/MainActivity.kt](app/src/main/java/com/example/MainActivity.kt)
- Navigation graph and screen wiring: [app/src/main/java/com/example/navigation/NavGraph.kt](app/src/main/java/com/example/navigation/NavGraph.kt)
- Security primitives: [app/src/main/java/com/example/security](app/src/main/java/com/example/security)
- Data layer and persistence: [app/src/main/java/com/example/data](app/src/main/java/com/example/data)
- ViewModels and UI state: [app/src/main/java/com/example/ui](app/src/main/java/com/example/ui)
- Background jobs and reminders: [app/src/main/java/com/example/worker](app/src/main/java/com/example/worker)
- Core calculations/utilities: [app/src/main/java/com/example/util](app/src/main/java/com/example/util)

## Conventions that matter

- Keep Android app changes in the single app module unless a cross-cutting dependency clearly needs a new module.
- Prefer existing Repository/ViewModel patterns over new ad hoc state holders.
- The app uses Room with SQLCipher and local encrypted storage patterns; do not bypass those abstractions when adding data access.
- Security features are centralized around PIN + biometric setup and verification in the security package.
- API keys and environment values are supplied through `.env` / `BuildConfig` in Android Gradle configuration; see [app/build.gradle.kts](app/build.gradle.kts) and [README.md](README.md) before adding secrets.
- The README explicitly documents that some API keys are compiled into the APK and are not a secret at runtime; do not assume these values are safe to expose in logs or screenshots.
- Testing lives under [app/src/test/java](app/src/test/java). Prefer focused, deterministic unit tests for utility logic and data rules.

## Environment and setup notes

- Android Studio is the expected development environment.
- The project expects a local `.env` file with keys such as `GEMINI_API_KEY` and optionally `BRSAPI_KEY` / `CMC_API_KEY` / `NEWS_API_KEY`.
- If you need to run the app locally in a debug setup, follow the README instructions for Android Studio import and app signing expectations.
- This repository intentionally keeps debug-only sample data and migration safety checks in place; do not remove those guardrails without a clear migration plan.

## Working guidance for AI coding agents

- Before changing app behavior, trace the existing data flow through repository -> ViewModel -> UI.
- Keep fixes narrow and consistent with the repo’s current architecture instead of introducing new frameworks or patterns.
- When editing build or security-related files, check [README.md](README.md) and [app/build.gradle.kts](app/build.gradle.kts) first to avoid breaking local setup.
- For database or migration changes, verify Room migration safety and existing local data assumptions.
- Prefer updating existing screens, repositories, and utilities over creating parallel implementations.

## Related documentation

- [README.md](README.md)
- [DESIGN.md](DESIGN.md)
- [app/build.gradle.kts](app/build.gradle.kts)
- [app/src/main/java/com/example/navigation/NavGraph.kt](app/src/main/java/com/example/navigation/NavGraph.kt)
