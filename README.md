# BabakPlayer

BabakPlayer is an Android companion app for BabakCast, the app that splits large media into parts you can send through messaging apps. BabakPlayer imports those parts when you share them to it (for example from WhatsApp) and plays them one after another as one playlist.

## Download
<!-- cocode-apps:install:start -->
- Coming to F-Droid
- [Download the Android installation file (APK) from GitHub](https://github.com/cocodedk/BabakPlayer/releases/latest/download/BabakPlayer.apk)
- [Add the app to Obtainium, an app that keeps it up to date](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/cocodedk/BabakPlayer)
<!-- cocode-apps:install:end -->

## Website
- [English](https://player.cocode.dk/)
- [Dansk (Danish)](https://player.cocode.dk/da/)
- [فارسی (Persian)](https://player.cocode.dk/fa/)

## Features
- Share-to-import: receive one or many files from WhatsApp or any other app that can share them.
- Order: files shared with a caption are grouped into one playlist and sorted by file name (part2 before part10). Files shared without a caption keep the order they were shared in.
- Continuous playback of audio and video as one playlist, with autoplay-next and a seek interval you can set.
- Google Cast to Nest speakers, Chromecast and other Cast devices on the same Wi-Fi network. Cast is in the full GitHub build only; the F-Droid build does not include it.
- English, Persian and Danish interface, with the Persian layout running right-to-left.
- Private: no account, no cloud sync, no backend. Removing a playlist removes it from BabakPlayer only; your original files stay where they are, and so do the private copies BabakPlayer made (see Storage and Removal Behavior).

## Relationship with BabakCast
- `BabakCast`: splits large media into parts and shares them.
- `BabakPlayer`: imports those parts and plays them one after another.
- Compatibility note: import supported audio and video parts shared from BabakCast.

## Supported Media Formats
- Audio: `mp3`
- Video: `mp4`, `mkv`, `mov`, `webm`

## Share-to-Import Flow
1. Select one or many files in WhatsApp (or another app).
2. Tap Share and choose BabakPlayer.
3. BabakPlayer adds them to a playlist: it keeps a reference to the original file when Android allows it, and otherwise copies the file into app-private storage.
4. The imported files appear in the playlist. The app does not show how many files were imported or skipped.
5. Playback starts automatically after the import. Use Pause to stop it temporarily.

## App Tabs
- `BabakPlayer`: playback controls and the files of the current playlist.
- `Playlists`: browse, play and remove saved playlists.
- `Settings`: theme mode, language, autoplay-next and seek interval. The "Auto-dismiss import summary" switch has no effect in this version, because no import summary is shown.
- `About`: what the app does, version, privacy policy and project links.

## Google Cast
Cast is in the full GitHub build only. The F-Droid build does not include it.
- Cast audio to compatible Nest speakers, and audio or video to compatible Chromecast devices and other Cast devices on your local network.
- A Cast button appears in the player screen when compatible devices are available.
- The app transfers the playlist and playback position when you switch between phone and Cast playback.
- Requires the phone and Cast device to be on the same Wi-Fi network.

## Playback Behavior
- Files play in playlist order (see Features for how a playlist is ordered).
- Autoplay-next can be toggled in settings.
- If a file cannot be played, it is marked as failed and playback continues with the next file, if there is one.
- A playlist can mix audio and video. Whether a file plays depends on your device.

## Localization
- Built-in languages: English, Persian (`fa`) and Danish (`da`). Choose the language under Settings, App language.
- The Persian interface runs in RTL layout automatically.

## Storage and Removal Behavior
- Import: BabakPlayer asks Android to keep read access to the shared file and plays it from where it is. If Android does not grant that, or for files shared from WhatsApp, it copies the file into app-private storage under `files/imported_media`.
- Playlists (titles, order and item details) are stored as metadata in `files/playlists/index.json` in app-private storage.
- Remove playlist: removes the playlist and its items from the list. No media file is deleted. You can remove whole playlists; removing a single file is not available in the current interface.
- Original files stay in device storage. Copies under `files/imported_media` are not deleted when you remove a playlist; they stay until you clear the app's data or uninstall the app.
- When playlists load, entries whose private copy no longer exists are dropped. For files BabakPlayer plays from where they are, an entry can remain after the original file is deleted, and that file then fails to play.

## Privacy
- No account, no cloud sync, no backend requirement.
- Shared files are referenced where Android allows it and otherwise copied into app-private storage.
- Removing a playlist removes its entry from BabakPlayer; it does not delete the original files or the copies kept in app-private storage (see Storage and Removal Behavior above).
- BabakPlayer does not upload your media to the developer. Android may include the app's private copies and playlists in your device backup.
- No analytics, crash reporting or advertising.

Read the full [privacy policy](https://player.cocode.dk/privacy/).

## Build
### Debug build
```bash
./gradlew assembleDebug
```

### Release build (signed)
Set required environment variables:
- `KEYSTORE_PATH`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Then build:
```bash
./gradlew assembleRelease
```

### CI smoke check
```bash
./gradlew buildSmoke
```

## Contributing
Local setup, git hooks, the build and test commands, coding style and the pull request checklist are in
[CONTRIBUTING.md](CONTRIBUTING.md). Bugs and ideas go to the
[issues page](https://github.com/cocodedk/BabakPlayer/issues).

## License
Apache-2.0 | © 2026 [Cocode](https://cocode.dk) | BabakPlayer
