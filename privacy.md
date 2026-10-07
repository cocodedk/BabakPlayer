# Privacy Policy — BabakPlayer

**App:** BabakPlayer (`com.cocode.babakplayer`)
**Developer:** CoCode.dk — Babak Bandpey
**Last updated:** 7 October 2026

> The canonical, always-current version of this policy is published at
> **https://player.cocode.dk/privacy/**

**BabakPlayer keeps your media on your device and sends no personal data to the developer.**
It is an Android media player that imports audio and video shared from other apps, plays them one
after another, and can cast them to Google Cast (Chromecast) devices on your local network. It has no user
accounts, no analytics, and no advertising.

## Media and playlists you import

When you share audio or video into BabakPlayer, the app keeps access to the original file where
Android allows it. Where Android does not (for example, files shared from WhatsApp), the app saves a
private copy in its own storage on your device. The playlists you build are stored locally on your
device too. BabakPlayer does not upload your media to the developer. Removing a playlist takes it out
of BabakPlayer; it does not delete your original files or the private copies. The private copies stay
until you clear the app's data or uninstall the app. To read audio and video on your device
the app uses the Android media permissions (`READ_MEDIA_AUDIO` and `READ_MEDIA_VIDEO` on
Android 13+, or read-storage access on Android 12 and below).

## Two builds

BabakPlayer is published in two builds. The **foss build** (`BabakPlayer-foss.apk`, the one submitted to
F-Droid) has no Google Cast and no network access of any kind: from version 14 it requests only the media
permissions described above and `WAKE_LOCK` (so playback continues with the screen off), and it contains
no web server. Everything in the next section applies only to the **full build** (`BabakPlayer.apk`).

## Casting to Chromecast and other Cast devices

The full build of BabakPlayer includes Google Cast so you can play your media on Chromecast, Nest speakers, and other
Cast-enabled devices on your Wi-Fi network. This is the only feature that uses the network, and it
works as follows:

- To find Cast devices, the app scans your local Wi-Fi network. This is why the full build requests the
  Wi-Fi state and network state permissions.
- While you are casting, the app runs a small temporary web server on your phone and streams the
  selected file to the Cast device (a `http://<your-phone>:<port>/…` address). The server is not
  restricted to your Wi-Fi: it listens on every network connection your phone has, for as long as the
  Cast session lasts. Your media is **not uploaded to the developer**.
- The Google Cast framework (a Google component built into the app) communicates with the Cast device
  and with Google's Cast service to set up and control the session, and loads Google's standard media
  receiver onto the Cast device. That activity is handled by Google and is governed by
  [Google's Privacy Policy](https://policies.google.com/privacy). Casting runs only while you have an
  active Cast session.
- In the full build, the internet permission is required by the Google Cast framework and by this local
  streaming server.

## No analytics, ads, or tracking

- No analytics, no crash reporting, and no advertising.
- No third-party tracking SDKs, no cookies, and no advertising identifier.
- The developer receives no usage data, no telemetry, and no personal information from the app.
- The media player the app uses (the Media3 library) passes playback events and performance data, such
  as errors and buffering, to Android's own media metrics service on Android 12 and newer. That service
  is part of your phone's system, not of this app, and the developer never receives the data. Google may
  also collect it if you have turned on sharing of usage and diagnostics data on your device; you control
  that in your phone's settings.

## Device backup

If you have enabled Android Auto Backup or Google account backup, the operating system may include
the app's local data, including your playlists and the private copies of imported media, in your
own personal Google backup. This is controlled entirely by you and Google — the developer has no
access to it. See
[Google's Privacy Policy](https://policies.google.com/privacy) for details.

## External links

The app's About screen has buttons that open, in your browser, the BabakPlayer website and this
privacy policy, the project's source code, issue tracker and releases page on GitHub, and the
developer's website ([cocode.dk](https://cocode.dk)). The buttons only hand the address to Android,
which opens your browser. BabakPlayer itself does not connect to these addresses and does not check for
updates by itself. Your browser then connects to the site, and those sites are governed by their own
privacy policies. This applies to both builds.

## Children

BabakPlayer does not knowingly collect personal data from anyone, including children.

## Changes

If this policy changes, the updated version will be posted here and on the website with a new
"last updated" date.

## Contact

Questions about this policy can be sent to **bb@cocode.dk** (CoCode.dk, developer: Babak Bandpey).
