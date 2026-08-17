# Android Carplay — Full App Redesign Implementation Plan

## 1. Objective

Redesign the complete Android Carplay app to a polished, enterprise-grade standard while preserving:

- Every existing feature and navigation path.
- Current ad formats, placements, triggers, frequency, loading behavior, and remove-ads entitlement.
- Existing Room data, SharedPreferences, files, permissions, notifications, receivers, and external intents.
- The current design as a selectable **Legacy** mode.

Three new visual directions will first be designed as representative concepts. After review, one direction will become the production **New Design**. The runtime app will initially expose two choices:

1. **Legacy Design** — the current UI, retained without visual changes.
2. **New Design** — the approved enterprise direction.

The theme architecture will make it inexpensive to ship the other concepts later as additional user-selectable themes, but implementing four complete production interfaces before selecting a direction would multiply layout work and regression risk.

---

## 2. Current App Baseline

The app is a single-module native Android application using:

- Kotlin, XML layouts, ViewBinding, Material 3, and Android Navigation.
- Approximately 45 fragments, 78 layouts, and 146 drawable resources.
- One splash activity and one navigation-host activity.
- Room for feature records and multiple SharedPreferences stores.
- AdMob native, small-native, banner, interstitial, and app-open ads.
- Google Play Billing product `removeads`.
- Firebase Remote Config, Crashlytics, Analytics, and Performance.
- Background behavior through reminder, boot, and Bluetooth receivers.

The current interface mixes a light gradient home experience with newer dark feature screens. Colors and sizing are spread across resource files, inline XML values, drawable XML, and Kotlin setters. This must be tokenized before a reliable multi-design system can be implemented.

---

## 3. Proposed Design Directions

Each concept will be demonstrated on the same representative screens so the comparison is fair: splash, intro, home/dashboard, navigation drawer, one data list, one form, one driving tool, one map screen, one dialog, and each ad format.

### Direction A — Executive Drive

Recommended starting direction.

- Premium dark graphite surfaces with restrained blue accents.
- Strong hierarchy, compact cards, clear status indicators, and refined typography.
- Data-dense but calm dashboards suitable for maintenance, expenses, driving scores, and vehicle records.
- Consistent enterprise component library across all features.
- Best fit for the app’s current dark feature screens and lowest migration risk.

### Direction B — Clear Road

- Light neutral surfaces with high-contrast text and spacious layouts.
- Accessibility-first controls, larger touch targets, and clear section separation.
- Friendly, modern fleet-management feel.
- Best readability in daylight and for forms, documents, and maintenance records.
- Requires more work to reconcile with existing dark-only dialogs and visual assets.

### Direction C — Digital Cockpit

- Driver-focused dark navy interface with high-visibility instrumentation.
- Large glanceable values, contextual status colors, and reduced visual noise.
- Best for speedometer, trip, parking, tire pressure, voice, and driving score features.
- Most visually distinctive direction, but requires the most structural work on non-driving screens.

### Selection Gate

No full-app redesign implementation begins until stakeholders approve one direction. Review should score:

- Brand fit.
- Daylight/night readability.
- Accessibility and touch ergonomics.
- Consistency across driving and record-management features.
- Ad integration quality.
- Engineering effort and regression risk.

Deliverable: one approved direction plus a signed-off component specification.

---

## 4. Non-Negotiable Behavior and Monetization Contract

### Ads

The redesign must preserve all current behavior:

- App-open ads on the existing process lifecycle trigger.
- Splash delay and interstitial flow before `HomeActivity`.
- Home feature taps must continue through `CheckInterstitial`, then navigate from its callback.
- The existing two-second home-tile debounce remains.
- Native ad containers remain on all screens currently calling `CheckNative`.
- Banner containers remain on screens currently calling `CheckBanner`.
- `SaveFragment` retains its small-native placement.
- Screens that only contain dormant ad containers must not begin loading ads accidentally.
- Debug builds continue to use test ad units; release builds continue to use Remote Config values.
- Remote Config keys and current runtime behavior remain unchanged during redesign.
- Native ad assets remain visually identifiable as ads and retain all required AdMob views.

Required stable IDs include `native_ad` and `banner_ad`, unless both layout and binding call sites are deliberately migrated together.

### Remove Ads

- Both existing remove-ads entry points remain available.
- Billing product stays `removeads` with type `INAPP`.
- Successful purchases continue to write the existing `removeads` entitlement through `SharedPrefrence`.
- That same entitlement continues to suppress native, banner, interstitial, and app-open ads.
- Theme selection must not read or write subscription preferences.

### App Features and State

The following remain unchanged:

- Navigation destination IDs and Safe Args contracts.
- Room database name, version, entities, DAOs, and migrations.
- All existing SharedPreferences file names and keys.
- Document, receipt, and vehicle-photo storage paths.
- Reminder alarms, notification channel IDs, boot rescheduling, and Bluetooth auto-launch.
- Permission timing and handling unless separately approved as a product change.
- Maps, camera, speech, phone, settings, sharing, privacy, and other external intents.
- Home/back/exit behavior and intro flow.

---

## 5. Theme Architecture

### Runtime Modes

Add a dedicated preference model:

```text
DesignMode.LEGACY
DesignMode.NEW
```

Store it in a dedicated `DesignPreferences` API. Do not reuse ad, subscription, intro, weather, or feature preference files.

Recommended rollout default:

- Existing installs: `LEGACY` until the user chooses the new design.
- New installs: show a one-time design choice after intro, with the approved new design preselected.

If install-age detection is not introduced, use `LEGACY` as the universal safe default.

### Application Strategy

- Resolve the selected theme before `setContentView` in both activities.
- Use Material theme overlays and semantic attributes for colors, typography, shape, elevation, icons, spacing, system bars, dialogs, and ad chrome.
- Keep Legacy resources mapped to the exact current appearance.
- Apply the approved New Design through a separate theme mapping.
- On selection, persist the choice and recreate only the host activity.
- Verify that activity recreation does not display an extra app-open ad, reload an interstitial, reset the current destination, or duplicate analytics.

### Semantic Tokens

Create tokens based on meaning, not a particular color:

- Background: app, elevated, card, input, scrim.
- Text: primary, secondary, disabled, inverse.
- Action: primary, secondary, destructive.
- State: success, warning, danger, information.
- Driving: safe, caution, critical, active.
- Shape: small control, card, dialog, prominent panel.
- Typography: display metric, page title, section title, body, label, caption.
- Spacing: page gutter, section gap, card padding, control gap.
- Layout resources: app background, feature card, toolbar, navigation drawer, and ad surface.

Raw colors in layouts, drawables, and Kotlin should be replaced with semantic tokens. Functional colors such as emergency danger and tire critical must preserve their meaning and contrast in both modes.

### Structural Layout Differences

Most screens should share XML structure and change through tokens/styles. Duplicate a layout only when the selected concept needs a materially different information hierarchy.

For a duplicated layout:

- Preserve every view ID and compatible view type used by ViewBinding.
- Resolve the layout resource through a theme attribute.
- Inflate the selected resource, then bind it to the existing generated binding with `BindingClass.bind(root)`.
- Do not duplicate fragment, adapter, ViewModel, navigation, ad, or billing logic.

Likely structural candidates: home dashboard, intro, navigation drawer, speedometer, driving score, and AI assistant. Forms, lists, dialogs, and record screens should normally remain shared and token-driven.

---

## 6. Enterprise Component System

Build and document reusable XML styles/components for:

- App bars and back navigation.
- Home feature tiles and section headers.
- Primary, secondary, destructive, icon, and text buttons.
- Cards, list rows, status chips, empty states, and dividers.
- Text fields, dropdowns, date/time controls, toggles, and validation states.
- Metric panels, gauges, progress states, and driving status indicators.
- Dialogs, bottom sheets, toasts/snackbars, and permission explanations.
- Loading, error, and no-content states.
- Native/small-native/banner ad frames that preserve SDK-required child views.
- Remove-ads promotion that is visually consistent but not confused with ad content.

Every component specification must include normal, pressed, focused, disabled, loading, error, and selected states where relevant.

---

## 7. Screen Migration Scope

### App Shell

- Launcher/splash.
- Intro pager.
- Home dashboard.
- Navigation drawer.
- Exit, rating, privacy/share, permission, and feature dialogs.

### Connectivity

- Vehicle selection.
- Connect.
- Connection type and system-setting handoffs.

### Vehicle Records

- Documents, capture, save, and media.
- Maintenance and add-maintenance.
- Fuel and add-fuel.
- Expenses and add-expense.
- Insurance and add-insurance.
- Mileage and add-mileage.
- Vehicle profile and add-vehicle.
- Service timeline.

### Driving and Safety

- Parking, park location, and find vehicle.
- Speedometer.
- Trip tracker.
- Driving score.
- Dashcam.
- DND driving.
- Tire pressure.
- Fatigue alerts.
- Emergency.
- Voice commands.
- Predictive maintenance.

### Information and Assistance

- Weather.
- Nearby places.
- AI assistant.
- Reminders.
- Feedback.

### Currently Unreachable Screens

Auto Launch, Connection Guide, Mirror Guide, Troubleshoot, and Cars Database have UI code but are not connected in the active navigation graph. They should receive the shared theme foundation, but full custom redesign should wait until product confirms whether they will be restored. They must not be made reachable as an accidental side effect.

---

## 8. Implementation Phases

### Phase 0 — Baseline and Regression Guardrails

- Capture screenshots/video of all reachable screens and major states.
- Record ad placement, format, trigger, container ID, and no-ad entitlement behavior.
- Record the complete navigation and external-intent matrix.
- Add a stable view-ID inventory for fragment bindings.
- Create test fixtures with representative Room and preference data.
- Establish release build and smoke-test baselines.

Exit criteria: signed behavior-parity checklist and reproducible baseline build.

### Phase 1 — Three Design Concepts

- Define typography, color, shape, spacing, iconography, and motion for A/B/C.
- Design the representative comparison screens.
- Demonstrate native, banner, interstitial loading dialog, and remove-ads presentation.
- Review accessibility contrast and common phone sizes.
- Select one direction.

Exit criteria: approved New Design and component specification.

### Phase 2 — Design-System Foundation

- Introduce semantic attrs, styles, dimensions, and drawable mappings.
- Implement `DesignMode` and `DesignPreferences`.
- Add Legacy/New themes and activity theme resolution.
- Add a Design option to the existing drawer with no interstitial trigger.
- Tokenize shared app shell, dialogs, and ad frames while matching Legacy screenshots.
- Replace programmatic raw colors with theme attribute resolution.

Exit criteria: switching works, persists across restart, and Legacy has no intentional visual delta.

### Phase 3 — New App Shell and Core Navigation

- Implement splash, intro, home, drawer, common toolbar, dialogs, and shared states.
- Keep home click listeners, ad callback navigation, billing buttons, and permission behavior unchanged.
- Validate back stack and state restoration after design switching.

Exit criteria: all destinations are reachable in both modes with identical ad triggers.

### Phase 4 — Feature Migration in Waves

Wave 1: forms and record management.

- Maintenance, fuel, expenses, insurance, mileage, car profile, documents, reminders, and service timeline.

Wave 2: driving and safety.

- Parking, speedometer, trip, driving score, dashcam, DND, tire pressure, fatigue, emergency, voice, and predictive maintenance.

Wave 3: information and assistance.

- Weather, nearby, AI assistant, feedback, and connectivity screens.

Each wave includes layouts, adapters, dialogs, empty/error/loading states, and its current ads.

Exit criteria per wave: screenshot review, behavior smoke test, ad parity test, and data persistence test in both modes.

### Phase 5 — Accessibility, Responsiveness, and Polish

- Verify touch targets, text contrast, font scaling, screen reader labels, focus order, and RTL safety.
- Test small/large phones, orientation behavior currently supported, and Android API 24–36.
- Move user-facing hardcoded text to string resources.
- Optimize PNG/WebP/vector assets and remove only confirmed-unused redesign assets.
- Keep motion short and optional; honor system animator settings.

Exit criteria: accessibility and device matrix accepted.

### Phase 6 — Release Hardening

- Run complete Legacy/New regression matrix.
- Test debug test ads and release Remote Config ad units separately.
- Verify purchased users see no ad format.
- Verify design switching never causes an extra ad impression.
- Validate process death, cold start, warm resume, activity recreation, offline start, and configuration changes.
- Roll out in staged percentages with Crashlytics/ANR monitoring.

Exit criteria: production AAB approved with rollback plan.

---

## 9. Verification Matrix

At minimum, test both `LEGACY` and `NEW` for:

- Cold launch, warm resume, background/foreground, and offline launch.
- Splash interstitial available, unavailable, failed, and removed by purchase.
- Every home tile with interstitial shown, skipped by counter, failed, and dismissed.
- Native, small-native, and banner load/failure/removed states.
- Design switch while on home and while on a nested destination.
- Process death and app restart with the selected design.
- Existing Room records after app update.
- Every persisted preference-driven feature.
- Permission granted, denied, denied permanently, and system-setting return.
- Reminder notification, boot reschedule, and Bluetooth auto-launch.
- Font scaling, TalkBack, RTL layout direction, and supported screen sizes.

Automated coverage should include:

- Unit tests for design preference serialization and theme mapping.
- Layout inflation tests for all duplicated Legacy/New layouts.
- Assertions that required binding IDs and ad container IDs exist in both versions.
- Navigation smoke tests for every home action.
- Billing entitlement tests around the existing preference contract.
- Screenshot tests for representative screens and component states.

---

## 10. Explicitly Out of Scope

Unless separately approved, the redesign will not:

- Add, remove, restore, or reorder app features.
- Change ad frequency, Remote Config keys, ad unit selection, or placement behavior.
- Fix existing Remote Config key mismatches as part of visual work.
- Add purchase restoration or change the billing product.
- Change intro persistence, permission timing, or current dead-code behavior.
- Migrate the app to Jetpack Compose.
- Change Room schemas, preference keys, storage paths, notification channels, or receivers.
- Wire currently unreachable screens into navigation.

These may be valid future improvements, but combining them with the redesign would make behavior parity impossible to verify.

---

## 11. Principal Risks and Controls

- **Ad regression:** preserve containers and call sites; maintain a placement inventory and impression test matrix.
- **Billing regression:** keep `removeads` and `SharedPrefrence` contract unchanged.
- **ViewBinding regression:** duplicate layouts only with identical IDs/types and automated inflation checks.
- **Legacy visual drift:** capture baselines before tokenization and run screenshot comparison after each wave.
- **State loss during switching:** restore current navigation state and test activity recreation/process death.
- **Unintended extra app-open ad:** explicitly test and guard internal theme recreation.
- **Inconsistent theme coverage:** prohibit new raw colors and migrate drawable/Kotlin color hotspots.
- **APK growth:** prefer shared layouts, vector/WebP assets, and semantic resources over four asset copies.
- **Accessibility debt:** make contrast, labels, touch targets, and font scale part of component acceptance.
- **Scope expansion:** keep feature changes and existing bug fixes in separate work items.

---

## 12. Approval Decisions Needed Before Development

1. Choose Direction A, B, or C after representative concepts are produced.
2. Confirm whether existing users should start in Legacy and new installs in New, or whether Legacy should be the universal default.
3. Confirm whether the production picker should expose only Legacy/New or all three new concepts.
4. Confirm whether currently unreachable screens are future scope.
5. Approve the baseline parity checklist, especially current ad quirks that must remain unchanged.

Once these decisions are signed off, implementation should proceed phase by phase, with no full-app visual rewrite merged before the Legacy foundation and behavior-parity tests are in place.
