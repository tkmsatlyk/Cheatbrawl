# Cheatbrawl AI Coach

Android starter project. GitHub Actions builds a debug APK in the cloud.

## Build from an Android phone
1. Upload the **contents** of this project ZIP to the repository root.
2. Ensure `.github/workflows/build-apk.yml` is included at that exact path.
3. Open the repository's Actions tab.
4. Run **Build Android APK** or push a commit to `main`.
5. Open the completed run and download the `Cheatbrawl-debug` artifact.
6. Extract the artifact and install `app-debug.apk`.

## Current prototype
- Android app shell
- User-approved Android screen-capture permission request
- GitHub Actions APK build

Real-time object recognition, projectile tracking, hitbox visualization, and hazard guidance are not implemented yet. This starter does not automate gameplay inputs.
