---
trigger: always_on
description: Ajiro Agent Jetpack Compose design gate. Applies on top of Sauron design-engineering router for native Android UI.
---

# Android Compose Design Gate

Upstream `design-engineering` skill is web oriented (landing pages, Tailwind, GSAP). Use it for routing only. All native Android UI work must follow this gate.

## Semantic structure

- Build screens from semantic Compose layout primitives (`Column`, `Row`, `Box`, `LazyColumn`) with clear content hierarchy.
- Never pass mutable `SnapshotStateList` to `LazyColumn items(...)`. Expose immutable values via `derivedStateOf`.
- Form rows use `FormItem` in `ui/components/ui/Form.kt`.

## Material 3 tokens

- Use `AppShapes` in `ui/theme/Shape.kt`. Use `MaterialTheme.colorScheme` and `MaterialTheme.typography`. No hardcoded colors or text sizes.
- Icons: `Icons.Rounded.XXX` only. Exception is toast action icons in `AppToast.kt`.

## Touch targets and grid

- Minimum touch target 48dp x 48dp for all interactive elements.
- Spacing rhythm 4dp and 8dp baseline only. No 5dp, 7dp, or other off-grid values.

## Haptics and motion

- Never use `LocalHapticFeedback` directly. Use `PremiumHaptics` via `rememberPremiumHaptics()` in `ui/hooks/PremiumHaptics.kt`.
- Use `MotionPolicy` in `ui/motion/MotionPolicy.kt`. Respect reduce motion setting. Top-level routes fade only. Child routes slide plus fade.
- Animation specs: standard spring `dampingRatio 0.5f stiffness 400f`. Clicky elements `dampingRatio 0.6f stiffness 300f`.

## Architecture boundary for UI work

- Route: `gandalf` plans, `aragorn` approves module boundary, `merry` gates tests.
- Never call `navController.navigate(Screen.Chat(...))` directly. Use `navigateToChatPage(...)` in `utils/ChatUtil.kt`.
- New screens register `Screen` subtype in `RouteActivity.kt` plus `composable<Screen.X>` in `AppRoutes`. Settings screens also add `SettingsDestination` entry. New ViewModels register in `di/ViewModelModule.kt`.
