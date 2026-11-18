# Privacy & Permissions

## Current release behaviour

AURELION is designed to operate locally. In the current source configuration:

- It has no INTERNET permission and does not make network requests.
- It does not include analytics, advertising, accounts, location, microphone, contacts, or cloud-sync integrations.
- Companion position, pet-care values, and pacing preferences are stored in Android SharedPreferences on the device.
- Android backup and device-transfer rules include only those three AURELION preference files. If Android backup is enabled by the device owner, these settings may be included in the backup or transfer service they selected.
- The app listens for the system power-connected broadcast while the companion service is running; it does not collect a power or device-usage history.

This is a source/manifest-level inventory. Recheck it whenever dependencies or permissions change. Packaged-build inspection and network-traffic review remain part of device QA.

## Permissions

### Display over other apps
SYSTEM_ALERT_WINDOW is required for the floating eagle. It allows the user-requested companion window to appear above other apps. Users can revoke it in Android Settings; AURELION checks it before startup and during overlay updates.

### Foreground service
FOREGROUND_SERVICE and FOREGROUND_SERVICE_SPECIAL_USE keep the explicitly started companion visible to Android while it hosts the overlay. The foreground notification identifies AURELION and includes a stop action. The special-use subtype is documented in the manifest because this persistent interactive overlay does not fit the standard media, location, connected-device, or health service categories.

### Notifications
On Android 13 and later, notification permission is requested when starting the companion. Denying it does not itself prohibit a foreground service, but Android may show its active-service information in system task controls rather than the notification drawer.

## Local data and backup

The app stores:
- aurelion_overlay_prefs: last companion position.
- aurelion_pet_stats: energy, happiness, affection, curiosity, and hunger.
- aurelion_settings: talkativeness and movement-frequency preferences.

The three files are explicitly included in Android cloud-backup and device-transfer rules so a device migration can preserve companion state. No server operated by AURELION receives this data.

## Future optional integrations

AI, voice, calendar, weather, analytics, accounts, and online features are not part of the current implementation. If introduced, they must be opt-in, documented before use, and accompanied by updated permissions and data-flow disclosures.

## User control

Users can stop the companion from the app or its foreground-service notification. They can revoke overlay and notification permissions in Android Settings. The project should continue to avoid guilt, streak pressure, and punitive relationship decay.
