# Multi-Variant Redesign — Implementation Status

## Approved Product Decisions

- Runtime choices: **Legacy** plus four new production designs.
- Light designs: **Pearl Light** and **Sage Touring**.
- Dark designs: **Midnight Drive** and **Carbon Gold**.
- Existing installs default to Legacy.
- Fresh installs default to Pearl Light.

## Implemented

### Runtime Design Selection

- Added `DesignMode`, `DesignPreferences`, and `DesignTheme`.
- The selected design is applied before activity layout inflation.
- Selection persists in the dedicated `design_preferences` store.
- Existing installs are detected through package install/update timestamps.
- A new **App design** drawer option previews and switches between all five choices.
- Switching recreates only `HomeActivity`; it does not invoke feature click handlers.

### Enterprise Design System

- Added semantic attributes for app backgrounds, splash, cards, surfaces, outlines, text, icons, inputs, navigation, and ad surfaces.
- Added four complete Material 3 theme mappings and adaptive dialog/date-picker styling.
- Pearl Light uses bright neutral surfaces, polished blue, soft gradients, and rounded shadows.
- Sage Touring uses warm natural surfaces, calm teal, larger curves, and softer shadows.
- Midnight Drive uses deep navy tonal elevation and electric blue for low-light driving.
- Carbon Gold uses luxury black surfaces, compact geometry, and restrained warm-gold accents.
- Added distinct app backgrounds, elevated feature cards, shadows, and home headers.
- Updated shared primary buttons, cards, chips, chat bubbles, progress, timeline, microphone, and score visuals to resolve through theme tokens.

### Full UI Coverage

- Tokenized all reachable feature layouts, forms, record rows, dialogs, intro content, and ad layouts.
- Legacy mappings point to the original resources and colors.
- All four mappings provide adaptive surfaces, elevated cards, text hierarchy, outlines, icons, and primary actions.
- Added a distinct header treatment and copy for each new design while retaining every feature tile and ID.
- Back/menu icon tint now adapts for light and dark contrast.
- Preserved native and banner ad container IDs and existing AdMob view hierarchy.

### Behavior Preservation

No changes were made to:

- Navigation graph or destination IDs.
- Home feature interstitial callbacks or two-second debounce.
- Splash five-second delay or interstitial flow.
- App-open ad manager.
- Native, small-native, banner, or interstitial loading call sites.
- Billing product `removeads` or its entitlement store.
- Room database, migrations, entities, or DAOs.
- Existing feature SharedPreferences keys.
- Permissions, notifications, alarms, receivers, files, or external intents.

### Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes.
- Added unit coverage for every theme mapping, migration from the previous Executive preference, and safe unknown-value fallback.
- IDE lint reports no errors in edited application sources.
- `git diff --check` passes.
- Confirmed all existing `native_ad` and `banner_ad` layout IDs remain present.
- Confirmed existing ad and `removeads` call sites remain in place.

## Manual QA Still Required

An Android device was not connected. The available local emulator could not start because an old snapshot operation was locked. Before release:

1. Clear or repair the Pixel 8 Pro AVD snapshot lock.
2. Install the generated debug APK.
3. Capture Legacy, Pearl, Sage, Midnight, and Carbon screenshots for the app shell and every feature wave.
4. Validate real ad loading with test IDs.
5. Validate purchased remove-ads state.
6. Test design persistence after process death and device restart.
7. Complete TalkBack, font-scale, RTL, and API 24–36 device checks.

The debug APK is generated under `app/build/outputs/apk/debug/`.
