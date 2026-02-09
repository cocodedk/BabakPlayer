# BabakPlayer UI/UX + Tabs + Persian Plan

## 1. Scope and Goals
1. Redesign the app visual language to look premium, modern, and coherent.
2. Replace default icon style with a custom BabakPlayer launcher identity and in-app icon consistency.
3. Add a bottom-tab navigation model with `Player`, `Settings`, and `About` tabs.
4. Add full Persian (`fa`) language support with proper RTL behavior and language switching support.
5. Keep each code file under 200 lines (target near 150 lines where possible).

## 2. Phase A: Visual Direction and Design Tokens
1. Define a new visual direction document in code comments and constants:
   - cinematic dark gradient background
   - amber/cyan accent pairing
   - clearer elevation and card hierarchy
   - refined spacing system (4/8/12/16/24)
2. Expand theme tokens:
   - background/surface/outline/critical/success colors
   - typography levels for page titles, section headers, body, labels
   - rounded shape set for cards/buttons/chips/sliders
3. Add motion guidelines:
   - short fade/slide for list and tab transitions
   - low-duration interactions for play controls
4. Add state visuals:
   - selected item glow
   - import success/skip/failed badges
   - disabled and busy states with clear contrast

## 3. Phase B: Icon System
1. Create a custom launcher concept (BabakPlayer “B + play wave” motif).
2. Replace adaptive icon assets:
   - foreground vector
   - background vector
   - monochrome vector (for Android themed icons)
3. Update app icon references and verify all densities render correctly.
4. Add in-app icon usage rules:
   - outlined for secondary actions
   - filled for primary actions
   - consistent icon size and stroke weight
5. Validate icon legibility at small sizes (launcher and toolbar/tab icons).

## 4. Phase C: Navigation + App Structure
1. Introduce a single source of truth for tabs:
   - `Player`
   - `Settings`
   - `About`
2. Refactor root UI scaffold:
   - top app bar
   - bottom navigation bar with 3 tabs
   - shared snackbar host and dialogs
3. Keep `Player` tab as playback + playlists home.
4. Move app-level preferences and non-play controls out of player surface into `Settings`.
5. Add `About` tab for app metadata and companion app links.

## 5. Phase D: Settings Tab (Functional)
1. Add a Settings screen model and state holder.
2. Add settings categories:
   - Appearance: theme mode (dark/system/light)
   - Language: English/Persian
   - Playback: autoplay-next toggle, seek interval preference
   - Import: keep import summary auto-dismiss toggle
3. Persist settings locally (DataStore or lightweight JSON settings store).
4. Bind settings live to UI where applicable:
   - language updates labels/text resources
   - theme updates colors dynamically
   - autoplay-next respected by playback flow
5. Add reset-to-default action with confirmation dialog.

## 6. Phase E: About Tab
1. Add About screen sections:
   - app name and version
   - purpose and local-first privacy note
   - “Companion to BabakCast” explanation
2. Add actionable rows:
   - open project website
   - open repository/issues link
   - license summary
3. Add compatibility section:
   - supported formats (`mp3`, `mp4`, `mkv`, `mov`, `webm`)
   - note on codec/device dependency
4. Add legal summary and attribution footer.

## 7. Phase F: Persian (fa) Localization + RTL
1. Create `values-fa/strings.xml` and translate all user-facing strings.
2. Extract all hardcoded strings from Kotlin into resources.
3. Ensure all screens are localized:
   - player controls
   - import summary
   - deletion dialogs
   - settings labels
   - about content
4. Validate RTL layout:
   - row ordering in controls and settings
   - alignment for titles/body text
   - icon mirroring where necessary
5. Add locale-aware formatting:
   - dates
   - sizes/durations labels structure
6. Add in-app language selector behavior:
   - immediate apply for Compose text resources
   - persist across app restarts

## 8. Phase G: UX Polish Pass
1. Improve perceived smoothness:
   - avoid janky recompositions in player slider
   - debounce heavy state updates
2. Improve readability:
   - better contrast checks in dark/light modes
   - larger tap targets for delete/play controls
3. Improve empty/loading states with friendlier visuals and concise copy.
4. Tune spacing and typography for small screens and large screens.

## 9. Phase H: Testing and Validation
1. Manual test matrix:
   - English + Persian
   - LTR + RTL behavior
   - each tab on phone portrait
   - share import and playback lifecycle
2. Behavioral checks:
   - import ordering unchanged
   - decode failure skip still works
   - file/playlist deletion still purges files
3. Build checks:
   - `./gradlew buildSmoke`
   - lint warnings reviewed
4. Asset checks:
   - launcher icon correctness in emulator launcher
   - tab icons at multiple densities

## 10. Implementation Order (Execution Sequence)
1. Introduce settings model + persistence.
2. Add tab navigation scaffold and split screens.
3. Migrate strings to resources and add `values-fa` translations.
4. Apply refreshed theme + spacing + component style updates.
5. Replace launcher and in-app icon set.
6. Implement About tab content and links.
7. Polish motion and state visuals.
8. Run full smoke checks and UX regression pass.

## 11. Deliverables
1. New UI theme and component polish.
2. New launcher/app icon set.
3. `Player`, `Settings`, `About` tabbed app shell.
4. Fully localized English + Persian strings.
5. Persisted settings with language/theme behavior.
6. Updated docs describing tabs and localization.

## 12. Acceptance Criteria
1. App visually departs from template look and feels intentionally designed.
2. All major controls and tabs use consistent iconography and spacing.
3. Settings tab persists choices and applies them reliably.
4. About tab includes app metadata, compatibility notes, and links.
5. Persian language works across all visible UI copy.
6. RTL layout is usable and visually correct.
7. `buildSmoke` passes after all changes.
