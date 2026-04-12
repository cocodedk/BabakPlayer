# CLAUDE.md — BabakPlayer

## Project Overview

BabakPlayer is an Android companion app for BabakCast. It imports shared media parts (e.g. from WhatsApp), preserves receive order, and plays them continuously as one playlist. Supports English and Persian (RTL) localization, Google Cast, and local-first private storage.

- **Language / Runtime**: Kotlin, Java 17 (Temurin), Android SDK
- **Framework**: Jetpack Compose, ExoPlayer, Google Cast SDK
- **Architecture**: Clean Architecture + MVVM (ViewModel → Repository → local storage)
- **Package / Namespace**: `com.cocode.babakplayer`

---

## Required Skills — ALWAYS Invoke These

These skills **must** be invoked when the relevant situation arises. Never skip them.

| Situation | Skill |
|-----------|-------|
| Before any new feature or screen | `superpowers:brainstorming` |
| Planning multi-step changes | `superpowers:writing-plans` |
| Writing or fixing core logic | `superpowers:test-driven-development` |
| First sign of a bug or failure | `superpowers:systematic-debugging` |
| Before completing a feature branch | `superpowers:requesting-code-review` |
| Before claiming any task done | `superpowers:verification-before-completion` |
| Working on UI / frontend | `frontend-design:frontend-design` |
| After implementing — reviewing quality | `simplify` |

---

## Architecture

```
BabakPlayer/
├── app/
│   ├── src/main/java/com/cocode/babakplayer/
│   │   ├── ui/          ← Compose screens and ViewModels
│   │   ├── data/        ← Repositories and local storage
│   │   ├── domain/      ← Use cases and domain models
│   │   └── service/     ← Media playback and Cast services
│   └── src/main/res/    ← Resources (strings, drawables, layouts)
├── website/             ← GitHub Pages (English + Persian)
├── scripts/             ← Setup and utility scripts
└── .github/workflows/   ← CI, release, and Pages automation
```

### Layer Rules
- `ui/` depends on `domain/` only — never imports from `data/` directly
- `domain/` has no Android dependencies — pure Kotlin
- `data/` implements `domain/` interfaces
- State is owned by ViewModels; Composables are stateless where possible

---

## Coding Conventions

- [ ] All models are **immutable** — use `copy()` for mutations
- [ ] Functions are **pure** where possible — no hidden side effects
- [ ] State is a single source of truth per feature (ViewModel)
- [ ] No hardcoded strings — use `strings.xml` for all user-visible text
- [ ] Strict Kotlin typing everywhere; avoid `!!` operator

---

## Engineering Principles

### File Size
- **200-line maximum per file** — extract a class, function, or Composable when approaching the limit

### DRY · SOLID · KISS · YAGNI
- Extract shared logic into named utilities; never copy-paste
- Single Responsibility: one class/function does one thing
- Don't add features not yet needed
- Delete dead code immediately

### TDD
- Write the failing test first, make it pass, then refactor
- Test names describe behaviour: `"should skip failed decode and continue queue"`
- One assertion per test — keep tests focused and readable

### Commit hygiene
- Follow Conventional Commits: `feat: ...` / `fix: ...` / `chore: ...`
- The `commit-msg` hook enforces this automatically

---

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run unit tests
./gradlew lintDebug              # Run lint checks
./gradlew buildSmoke             # Full smoke check (build + tests + lint) — used in CI and pre-commit
./gradlew assembleRelease        # Build signed release APK (requires signing env vars)
```

---

## Key Files

| File | Purpose |
|------|---------|
| `CLAUDE.md` | This file — project conventions and session startup |
| `version.txt` | Simple integer version (incremented by release workflow) |
| `.github/workflows/ci.yml` | CI on PRs and branches |
| `.github/workflows/release-apk.yml` | Signed release builds + GitHub Releases |
| `.github/workflows/deploy-pages.yml` | GitHub Pages deployment |
| `.githooks/pre-commit` | Smoke check before every commit |
| `.githooks/commit-msg` | Conventional Commits enforcement |
| `scripts/install-hooks.sh` | One-time hook installer |
| `scripts/setup-repo.sh` | One-time branch protection + repo settings |

---

## Starting a New Session

1. Read this file
2. Run `./gradlew buildSmoke` to confirm everything passes
3. Invoke `superpowers:brainstorming` before touching any feature
4. Follow the Required Skills table — every skill is mandatory, not optional
