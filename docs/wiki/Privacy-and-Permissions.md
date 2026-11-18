# Privacy & Permissions
## Principles
- Local-first by default.
- Ask for permissions only when the feature needs them.
- Explain the purpose before opening Android permission settings.
- Make online features optional and clearly distinguish them from offline behavior.
- Provide clear controls to pause, hide, mute, and reset.
- Do not collect sensitive data merely to simulate personality.
- Never use guilt, streak pressure, or punitive relationship decay.

## Overlay permission
The floating companion requires Android's overlay permission (commonly shown as "display over other apps"). It allows AURELION to draw its companion window above other apps. The overlay should be movable, easy to hide, and should not cover essential controls.

## Notifications / foreground service
If Android requires notification permission or a foreground-service notification for continued presence, explain why and keep the notification honest, minimal, and user-controllable where platform rules permit.

## Future optional integrations
- **AI conversation:** clearly disclose that prompts may be sent to the selected provider. Offer an offline alternative and controls.
- **Voice:** explain microphone use before requesting it. Prefer push-to-talk over always-listening; never enable continuous listening without explicit, informed opt-in.
- **Calendar:** request the narrowest access and avoid sending event details to third parties.
- **Weather:** do not require location; allow manual city selection or use broad weather data only when the user opts in.

## User control
Users should be able to stop the companion, pause behavior, disable sound, reduce movement, clear local personality/relationship data, and reset customization. Future features should not make opting out difficult.
