# Privacy Policy — BabakPlayer

**App:** BabakPlayer (`com.cocode.babakplayer`)
**Developer:** CoCode.dk — Babak Bandpey
**Last updated:** 14 July 2026

> The canonical, always-current version of this policy is published at
> **https://cocodedk.github.io/BabakPlayer/privacy.html**

**BabakPlayer keeps your media on your device and sends no personal data to the developer.**
It is an Android media player that imports audio and video shared from other apps, plays them in
order, and can cast them to Google Cast (Chromecast) devices on your local network. It has no user
accounts, no analytics, and no advertising.

## Media and playlists you import

When you share audio or video into BabakPlayer, the files are copied into the app's private storage
on your device, and the playlists you build are stored locally there too. This content stays on your
device — it is never uploaded to the developer or to any server. It is removed when you delete an
item, clear a playlist, clear the app's data, or uninstall the app. To read audio and video on your
device the app uses the Android media permissions (`READ_MEDIA_AUDIO` and `READ_MEDIA_VIDEO` on
Android 13+, or read-storage access on Android 12 and below).

## Casting to Chromecast and other Cast devices

BabakPlayer includes Google Cast so you can play your media on Chromecast, Nest speakers, and other
Cast-enabled devices on your Wi-Fi network. This is the only feature that uses the network, and it
works as follows:

- To find Cast devices, the app scans your local Wi-Fi network. This is why it requests the Wi-Fi
  state and network state permissions.
- While you are casting, the app runs a small temporary web server on your phone and streams the
  selected file directly to the Cast device over your local network (a `http://<your-phone>:<port>/…`
  address that only exists on your own network). Your media travels from your phone to your Cast
  device on your own network — it is **not uploaded to the developer or to the public internet**.
- The Google Cast framework (a Google component built into the app) communicates with the Cast device
  and with Google's Cast service to set up and control the session, and loads Google's standard media
  receiver onto the Cast device. That activity is handled by Google and is governed by
  [Google's Privacy Policy](https://policies.google.com/privacy). Casting runs only while you have an
  active Cast session.
- The internet permission is required by the Google Cast framework and by this local streaming server.

## No analytics, ads, or tracking

- No analytics, no crash reporting, and no advertising.
- No third-party tracking SDKs, no cookies, and no advertising identifier.
- The developer receives no usage data, no telemetry, and no personal information from the app.

## Device backup

If you have enabled Android Auto Backup or Google account backup, the operating system may include
the app's local data in your own personal Google backup. This is controlled entirely by you and
Google — the developer has no access to it. See
[Google's Privacy Policy](https://policies.google.com/privacy) for details.

## External links

The app's About screen contains links to the developer's website ([cocode.dk](https://cocode.dk))
and the project's GitHub repository. Selecting them opens your browser; those sites are governed by
their own privacy policies.

## Children

BabakPlayer does not knowingly collect personal data from anyone, including children.

## Changes

If this policy changes, the updated version will be posted here and on the website with a new
"last updated" date.

## Contact

Questions about this policy can be sent to **bb@cocode.dk** (CoCode.dk, developer: Babak Bandpey).
