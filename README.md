# Berkeh Connector – Clean Build Base

Clean Android build base for Berkeh payment connector.

## Build stack
- Android Gradle Plugin: 8.8.2
- Gradle: 8.10.2 (pinned in GitHub Actions)
- JDK: 17
- compileSdk / targetSdk: 35
- minSdk: 26

## Included
- Launchable Android app
- SMS runtime permission request
- SMS broadcast receiver
- Initial bank detection:
  Mellat, Melli, Tejarat, Saderat, Sepah, Saman, Iran Zamin
- Persian/Arabic digit normalization
- Initial deposit/withdrawal detection
- Initial amount extraction
- GitHub Actions APK artifact build

This clean base intentionally does not yet send transaction data to WordPress.
The next step is to add secure multi-store pairing and API sync after the APK build is verified.
