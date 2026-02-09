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
4. Refactor import pipeline to create reference items instead of copied files.
5. Refactor repository delete operations to metadata-only removal.
6. Add playlist-load reconciliation pass to remove missing items and keep valid ones.
7. Update playback URI resolution to support both `content://` and file paths.
8. Update user-facing copy where delete wording implies physical purge.
9. Run targeted unit tests, then full `buildSmoke`.
10. Summarize behavior changes and edge cases.
