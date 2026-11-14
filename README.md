# Brawl Stars AI Coach — GitHub APK starter

Android starter project with a GitHub Actions workflow that builds a debug APK in the cloud.

## Upload to GitHub from Android
1. Extract this ZIP on your phone.
2. Create a new repository on GitHub.
3. Upload the **contents** of this folder to the repository root (not the outer folder).
4. Open the repository's **Actions** tab.
5. Run **Build Android APK**, or push to `main`.
6. Open the completed workflow run and download the `BrawlStars-AI-Coach-debug` artifact.
7. Extract the downloaded artifact and install `app-debug.apk`. Android may ask you to allow installation from that browser/file manager.

## Current prototype status
- Android app shell is included.
- The app can request Android's user-approved screen-capture permission.
- GitHub Actions builds a debug APK.
- Real-time object detection, projectile tracking, hitbox visualization, and any game-control logic are **not implemented yet**.

This starter is for visual analysis/overlay development. It does not automate aiming, shooting, or gameplay inputs.
