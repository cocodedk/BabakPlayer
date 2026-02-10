# Plan: Storage-Reference Playlists (No Duplication, Safe Deletes)

1. Lock scope from request:
   - Playlist creation must reference original files (no duplication/copy).
   - Deleting a playlist must delete metadata only, not physical files.
   - If referenced files are missing later, playlists must auto-adjust.
2. Add failing tests first for required behaviors.
3. Implement a small domain policy layer for:
   - reference-path behavior (store source URI/path directly),
   - playlist reconciliation when files disappear,
   - delete policy (no physical purge for referenced files).
4. Add URI permission lifecycle management:
   - Introduce a small URI permission manager that calls `takePersistableUriPermission()` for imported reference-path `content://` items and persists a token/record tied to each playlist item.
   - Update the import pipeline to call this manager while creating reference items.
   - Update delete flows to call `releasePersistableUriPermission()` when an item is deleted or dereferenced.
   - Ensure playlist-load reconciliation checks persisted permission state first and attempts reacquire/cleanup before deciding an item is missing.
5. Refactor import pipeline to create reference items instead of copied files.
6. Refactor repository delete operations to metadata-only removal.
7. Add playlist-load reconciliation pass to remove missing items and keep valid ones.
8. Update playback URI resolution to support both `content://` and file paths.
9. Update user-facing copy where delete wording implies physical purge.
10. Run targeted unit tests, then full `buildSmoke`.
11. Summarize behavior changes and edge cases.
