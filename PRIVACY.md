# AURELION Privacy Notice

**Last reviewed:** 18 November 2026

AURELION is an experimental Android virtual golden eagle companion. The current version is designed to work locally and does not require an account or an internet connection.

## Information stored on the device

AURELION stores the following values in Android SharedPreferences:

- The last position of the floating companion.
- The companion's energy, happiness, affection, curiosity, and hunger values.
- The user's talkativeness and movement-frequency preferences.

These values are used only to restore the companion's state and preferences.

## Network and third-party services

The current Android manifest does not request the INTERNET permission. The current app source does not contain analytics, advertising, account, cloud, or third-party data-transfer features. AURELION does not send the preference values listed above to an AURELION server.

This notice describes the current source and manifest. It must be reviewed again if network-connected features or dependencies are added.

## Android permissions

- Display over other apps: required to draw the floating eagle above other apps.
- Foreground service: required to keep the user-started companion active and visible to Android.
- Notifications: requested on Android 13 and later so the foreground-service notification can appear in the notification drawer. If denied, Android may still show the active service in system task controls.

## Backup and device transfer

The app explicitly includes its three local preference files in Android backup and device-transfer rules. If Android backup or device transfer is enabled, these preferences may be copied using the Android service selected by the device owner.

## Retention and deletion

The preferences remain on the device until app data is cleared or the app is uninstalled. Android backup copies, if enabled, are governed by the user's Android backup account and settings.

## Contact and future changes

This is a small experimental project. Report privacy questions or concerns through the repository's issue tracker. Any future feature that collects or transmits data must be documented before it is enabled.
