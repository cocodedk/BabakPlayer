# BabakPlayer PRD

## Document Metadata

- Product: `BabakPlayer` (Android)
- Type: Product Requirements Document (PRD)
- Status: Draft v1
- Date: 2026-02-09
- Companion app: `BabakCast`

---

## 1. Product Definition

### 1.1 Problem

`BabakCast` can split large videos into multiple files for messaging apps (for example, WhatsApp). On the receiver side, handling many parts manually is difficult: users need a clean way to import shared files, keep their order, and play them continuously without manual switching.

### 1.2 Solution

Build `BabakPlayer`, an Android companion app that:

- Accepts shared media files from WhatsApp (single or multiple files).
- Saves files locally in the same order they are received.
- Creates a playlist from the incoming batch.
- Uses the first received file description as the playlist identity/title source.
- Plays all items continuously (audio + video).
- Supports deleting one file or the full playlist, with permanent file purge.

### 1.3 Product Positioning

- `BabakCast`: sender/producer app.
- `BabakPlayer`: receiver/player app.
- Together they form a local-first transfer-and-play workflow using common messaging channels.

---

## 2. Goals and Non-Goals

### 2.1 Goals (MVP)

- Reliable import from Android share intents.
- Strict order preservation of received files.
- Playlist playback with autoplay-next until the end.
- Support `mp3` and BabakCast-compatible video formats.
- File lifecycle controls with hard-delete behavior.
- Clear operational docs and CI/release pipeline.

### 2.2 Non-Goals (MVP)

- Cloud sync/accounts.
- Editing/transcoding media.
- DRM bypass.
- Background downloading from YouTube directly.

---

## 3. Primary User Stories

- As a receiver, I can share many split files from WhatsApp to BabakPlayer and import all of them in one step.
- As a receiver, I can trust the playlist order to match the order the files were received.
- As a receiver, I can press play once and let all parts play continuously.
- As a receiver, I can remove one broken part without deleting everything.
- As a receiver, I can delete the whole playlist and be sure all imported files are purged from storage.

---

## 4. BabakCast ↔ BabakPlayer Contract

### 4.1 Supported Input Media (MVP)

- Audio: `mp3`
- Video: `mp4`, `mkv`, `mov`, `webm`

Note: BabakCast split output is currently `mp4` for video parts and `mp3` for extracted audio; BabakPlayer still supports the full listed set for compatibility and future-proofing.

### 4.2 Share Intent Inputs

BabakPlayer must handle:

- `ACTION_SEND` (single file)
- `ACTION_SEND_MULTIPLE` (multiple files)
- `EXTRA_STREAM` and/or `ClipData`
- Optional `EXTRA_TEXT` (caption/description)

### 4.3 Ordering Rule (Critical)

The app must persist and play in the exact order received from the incoming intent payload. No automatic re-sorting by filename, date, or size during import.

### 4.4 Playlist Naming Rule

Playlist title source priority:

1. Description derived from the first received file (if provided by sending app / share payload).
2. `EXTRA_TEXT` share caption.
3. First file display name (filename without extension).
4. Fallback: `Imported playlist <timestamp>`.

---

## 5. Functional Requirements

### FR-1 Import from Share Target

- BabakPlayer is available in Android share sheet for supported media.
- Import operation copies content URIs into app-private storage.
- App validates each file type before import.
- Unsupported files are skipped and reported in summary.

### FR-2 Persist Ordered Playlist

- Create one playlist per import transaction.
- Persist `import_order_index` per media item.
- Playback queue uses ascending `import_order_index`.

### FR-3 Non-Stop Playback

- User can start playlist with one action.
- On completion of current file, next file auto-plays.
- Works for mixed media types in one playlist.
- If one file fails decode, player skips to next and logs error state for that item.

### FR-4 Single File Deletion

- User can delete one item from playlist.
- File is removed from disk immediately.
- Item is removed from queue and playlist metadata.
- If currently playing file is deleted, playback moves to next available item.

### FR-5 Full Playlist Deletion

- User can delete entire playlist.
- All files in playlist are physically deleted from app storage.
- Playlist metadata is deleted.
- Operation is atomic from user perspective: success summary or failure summary.

### FR-6 Purge Guarantees

- No orphan records after delete.
- No orphan files after delete success.
- Failures are surfaced with retry action.

### FR-7 Import Summary UI

- After import, show:
  - Playlist title
  - Count imported / skipped
  - Supported/unsupported summary
  - Total size

---

## 6. UX Flows

### 6.1 Share-to-Play Flow

1. User selects many files in WhatsApp and taps Share.
2. User chooses `BabakPlayer`.
3. BabakPlayer shows `Importing...` progress.
4. Import summary appears.
5. User taps `Play`.
6. Playlist runs continuously.

### 6.2 Delete File Flow

1. User opens playlist.
2. User long-presses or taps menu on a file.
3. User confirms `Delete file`.
4. Item + local file are purged.

### 6.3 Delete Playlist Flow

1. User opens playlist menu.
2. User taps `Delete playlist`.
3. Confirmation modal shows file count + irreversible warning.
4. All files + metadata are purged.

---

## 7. Data Model Requirements

### 7.1 Entities

- `Playlist`
  - `playlist_id` (UUID)
  - `title`
  - `created_at`
  - `source_app` (optional: `whatsapp`, `unknown`)
  - `item_count`
  - `total_bytes`

- `PlaylistItem`
  - `item_id` (UUID)
  - `playlist_id` (FK)
  - `import_order_index` (int, required)
  - `original_display_name`
  - `mime_type`
  - `local_path`
  - `bytes`
  - `duration_ms` (nullable until probed)
  - `status` (`ready`, `decode_failed`, `deleted`)

### 7.2 Storage Rules

- Files stored under app-private directory:
  - Example: `/Android/data/<package>/files/playlists/<playlist_id>/...`
- Never depend on upstream content URI permanence.

---

## 8. Technical Requirements

### 8.1 Playback Engine

- Use ExoPlayer/Media3 for queue playback.
- Must support:
  - `audio/mpeg`
  - common MP4 container profiles
  - common MKV/MOV/WEBM profiles supported by Android decoders

### 8.2 Import Engine

- Read all incoming URIs via `ContentResolver`.
- Copy streams with bounded buffer + cancellation support.
- Validate MIME using:
  - explicit MIME from resolver first
  - extension fallback second

### 8.3 Integrity Checks

- Reject zero-byte files.
- Compute and store file size.
- Optional future: checksum for de-duplication.

---

## 9. Correlation With BabakCast

### 9.1 Companion Contract

- BabakCast produces multi-part files for sharing.
- BabakPlayer consumes those parts and reconstructs continuous playback via ordered playlist.

### 9.2 Compatibility Baseline

- BabakPlayer must remain compatible with current BabakCast output naming patterns (`_partNN`) but must not rely on naming for ordering during initial import.
- BabakPlayer should expose a compatibility note in docs:
  - "Optimized for imports generated by BabakCast."

### 9.3 Release Coordination

- Each BabakPlayer release should include tested BabakCast compatibility version(s) in release notes.

---

## 10. Repository and Delivery Requirements

### 10.1 Required Files

- `README.md` (product overview, install, usage, compatibility with BabakCast)
- `CONTRIBUTING.md` (dev setup, branching, tests, commit expectations, PR checklist)
- `LICENSE` (Apache-2.0, matching BabakCast unless product owner decides otherwise)

### 10.2 Pre-Commit Hooks

BabakPlayer repo must include `.githooks/pre-commit` and `scripts/install-hooks.sh`.

Pre-commit behavior (mirroring BabakCast intent):

1. Run smoke verification task:
   - `./gradlew buildSmoke --no-daemon --quiet`
2. If signing env vars are set (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), also run:
   - `./gradlew assembleRelease --no-daemon --quiet`
3. Block commit on failure.

### 10.3 GitHub Actions Requirements

#### Workflow A: Signed Release APK

- Trigger:
  - `push` to `main`
  - `workflow_dispatch`
- Required secrets:
  - `KEYSTORE_BASE64`
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
- Steps:
  - Checkout
  - Setup Java 17 with Gradle cache
  - Validate secrets
  - Decode keystore
  - Build `assembleRelease`
  - Upload artifact
  - Create GitHub Release with stable APK filename (`BabakPlayer.apk`)

#### Workflow B: GitHub Pages Deploy

- Trigger:
  - `push` to `main` with path filters:
    - `website/**`
    - `.github/workflows/deploy-pages.yml`
  - `workflow_dispatch`
- Use:
  - `actions/configure-pages`
  - `actions/upload-pages-artifact`
  - `actions/deploy-pages`

#### Workflow C: Pull Request / CI Checks

- Trigger:
  - `pull_request` to `main`
  - Optional push on feature branches
- Steps:
  - `./gradlew buildSmoke`
  - unit tests
  - lint
- Required as merge gate.

---

## 11. README / CONTRIBUTING / LICENSE Requirements

### 11.1 README Minimum Sections

- What BabakPlayer does
- Relationship with BabakCast
- Supported media formats
- Share-to-import flow
- Playback behavior
- Delete/purge behavior
- Build/release instructions
- Privacy and local-storage behavior

### 11.2 CONTRIBUTING Minimum Sections

- Local setup
- Hook installation (`./scripts/install-hooks.sh`)
- Build/test commands
- Coding style
- PR checklist:
  - tests passed
  - manual import/playback tested
  - delete/purge behavior validated

### 11.3 LICENSE

- Include Apache License 2.0 text in root `LICENSE`.
- Keep copyright owner/year current.

---

## 12. Acceptance Criteria (MVP Exit)

- Importing 1..50 files from WhatsApp creates a playlist with exact receive-order playback.
- Playlist title is based on first file description rule.
- `mp3`, `mp4`, `mkv`, `mov`, `webm` supported for import and queueing (playback subject to codec availability).
- Single-item deletion purges file and updates queue safely.
- Full-playlist deletion purges all files and metadata.
- Pre-commit hook blocks invalid commits.
- GitHub Actions release build and Pages deployment run successfully.
- README, CONTRIBUTING, and LICENSE exist and match this PRD.

---

## 13. Delivery Plan

### Phase 1: Foundation

- Android project scaffold (`BabakPlayer`)
- Media3 integration
- Share-intent intake skeleton
- Data layer for playlist + items

### Phase 2: Core UX

- Import summary screen
- Playlist screen
- Continuous playback controls
- Single-file and full-playlist deletion

### Phase 3: Reliability and Ops

- Hook + `buildSmoke` setup
- CI/release/pages workflows
- README/CONTRIBUTING/LICENSE
- Compatibility test pass with BabakCast sample outputs

### Phase 4: Hardening

- Error analytics (local only, opt-in)
- Corrupt file handling improvements
- Optional reordering tools (without changing default import order contract)

---

## 14. Open Questions

- Should playlist title prefer share caption over first file description if both exist and differ?
- Should duplicate imports create new playlists always, or deduplicate by hash in later versions?
- Should mixed audio+video playlists default to background audio mode when app is minimized?

