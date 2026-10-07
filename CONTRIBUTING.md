# Contributing to BabakPlayer

Built by [Cocode](https://cocode.dk).

## Local Setup
1. Install Android Studio (latest stable) and Android SDK.
2. Ensure Java 17 is available.
3. Open the project and sync Gradle.

## Install Git Hooks
```bash
./scripts/install-hooks.sh
```

## Build and Test Commands
```bash
./gradlew assembleDebug
./gradlew testFullDebugUnitTest testFossDebugUnitTest
./gradlew lintFullDebug lintFossDebug
./gradlew buildSmoke
```

## Coding Style
- Kotlin style: official Kotlin conventions.
- Keep files small and focused.
- Prefer clear naming and explicit behavior over hidden side effects.

## PR Checklist
- [ ] `./gradlew buildSmoke` passes.
- [ ] Manual share-import test completed.
- [ ] Continuous playback across imported parts verified.
- [ ] Playlist removal updates the saved list without deleting original media or app-private copies.
- [ ] Updated docs if behavior changed.
