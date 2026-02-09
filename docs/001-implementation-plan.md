# BabakPlayer MVP Implementation Plan

1. Read PRD requirements and map mandatory MVP scope to Android implementation tasks.
2. Review `../BabakCast` build and workflow conventions to keep companion parity.
3. Add core dependencies (Media3, lifecycle ViewModel Compose, coroutines, serialization).
4. Add top-level `buildSmoke` Gradle task.
5. Add release signing environment-based config in app Gradle.
6. Register share-intent filters (`ACTION_SEND`, `ACTION_SEND_MULTIPLE`) in manifest.
7. Build app folder layout for modular code with small files (`model`, `data`, `domain`, `ui`, `player`, `util`).
8. Define media and playlist entities (`Playlist`, `PlaylistItem`, import summary, item status).
9. Implement UUID/time helpers and media validation helpers.
10. Implement import-title resolver using PRD priority rule.
11. Implement local metadata storage (JSON file in app-private storage).
12. Implement file-system storage service for playlist file paths and deletion.
13. Implement import engine for `EXTRA_STREAM` + `ClipData` with strict payload-order preservation.
14. Implement MIME + extension validation for `mp3`, `mp4`, `mkv`, `mov`, `webm`.
15. Implement zero-byte rejection and size accounting.
16. Implement repository methods for create/list/delete playlist and delete single item with purge.
17. Implement ExoPlayer manager for queue playback and autoplay-next.
18. Handle decode/playback errors by skipping to next item and marking item status.
19. Build `MainViewModel` for state, import flow, playback control, and deletion actions.
20. Parse incoming intents in `MainActivity` and forward payload to ViewModel.
21. Build polished Compose UI: hero header, import summary card, playlist list, active-now panel.
22. Build smooth player controls: play/pause, next/previous, seek bar, position/time labels.
23. Add delete-file and delete-playlist confirmations in UI.
24. Add `README.md`, `CONTRIBUTING.md`, and `LICENSE` content required by PRD.
25. Add `.githooks/pre-commit` and `scripts/install-hooks.sh` with smoke + optional release checks.
26. Add GitHub Actions workflows for release APK, PR CI, and Pages deploy.
27. Run formatting and local verification (`./gradlew buildSmoke`, unit tests if present).
28. Fix any build/test issues and keep each code file under 200 lines.
29. Provide final implementation summary with known tradeoffs and validation notes.
