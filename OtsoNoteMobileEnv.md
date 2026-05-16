# Environment Separation: Otso (Production) vs Kontio (Development)

## Overview

The project still ships two installable variants, but branding assets are now unified to keep integrity and reduce asset debt.

| Property | Production (Otso) | Development (Kontio) |
|---|---|---|
| App ID | `com.otso.app` | `com.otso.app.dev` |
| Display name | Otso | Kontio |
| Launcher icon source | `OtsoAssets_New/icons/launcher/otso_icon_use2.png` | Same source |
| In-app logo source | `OtsoAssets_New/icons/in_app/otso_icon_dark.png` / `otso_icon_light.png` | Same source |
| Default font family | iA Writer Quattro S (Regular, Italic, Bold, Bold Italic) | Same font family |
| Accent color | `#EFDCAC` | `#EFDCAC` |
| Version suffix | _(none)_ | `-dev` |

---

## Current Source-Set Rules

1. Shared runtime branding assets live in `app/src/main`.
2. `debug` source set keeps only environment-specific values (`strings.xml`, `themes.xml`, `colors.xml`).
3. `release` source set keeps `BrandAssets.kt` mapping for production package build.
4. Both `debug` and `release` now resolve logos to:
   - `R.drawable.ic_otso_dark`
   - `R.drawable.ic_otso_light`

---

## Canonical Asset Locations

### Authoring Source (human-managed)

- `OtsoAssets_New/fonts/ia_writer_quattro_s_regular.ttf`
- `OtsoAssets_New/fonts/ia_writer_quattro_s_italic.ttf`
- `OtsoAssets_New/fonts/ia_writer_quattro_s_bold.ttf`
- `OtsoAssets_New/fonts/ia_writer_quattro_s_bold_italic.ttf`
- `OtsoAssets_New/icons/in_app/otso_icon_dark.png`
- `OtsoAssets_New/icons/in_app/otso_icon_light.png`
- `OtsoAssets_New/icons/launcher/otso_icon_use2.png`

### Android Runtime (build-consumed)

- Fonts: `app/src/main/res/font/ia_writer_quattro_s_*.ttf`
- In-app logos: `app/src/main/res/drawable/ic_otso_dark.png`, `ic_otso_light.png`
- Launcher icon layers:
  - `app/src/main/res/mipmap-*/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-*/ic_launcher.png`
  - Adaptive mapping: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

---

## Cleanup Performed

- Removed legacy font assets from `app/src/main/res/font`:
  - `excon_*`
  - `general_sans_*`
  - `jetbrains_mono_regular.ttf`
- Removed debug-only duplicated icon assets:
  - `app/src/debug/res/drawable/ic_kontio_*`
  - `app/src/debug/res/mipmap-*/ic_launcher_foreground.png`
- Kept debug `values/` overrides only.

---

## Color Tokens

- `app/src/main/res/values/colors.xml`
  - `accent_primary = #EFDCAC`
  - `ic_launcher_background = #EFDCAC`
- `app/src/debug/res/values/colors.xml`
  - `accent_primary = #EFDCAC`
  - `ic_launcher_background = #EFDCAC`
- `app/src/main/java/com/otso/app/ui/theme/OtsoTheme.kt`
  - `Accent = 0xFFEFDCAC`
  - `AccentMuted = 0x2EEFDCAC`
  - `SelectionBackground = 0x73EFDCAC`

---

## Build-Type Behavior

```kotlin
buildTypes {
    debug {
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
    }
    release {
        isMinifyEnabled = true
        signingConfig = signingConfigs.getByName("release")
    }
}
```

---

## Notes

- Workflow gate before any implementation: read `AGENTS.md` and `Muji.md` first, then execute changes.
- Global editor monospace toggle was removed as non-essential UI chrome.
- Inline semantic code styling still exists via `FontFamily.Monospace` in rich-text mapping.
- Splash icon now resolves through `@drawable/ic_splash_logo` with day/night assets:
  - `res/drawable/ic_splash_logo.xml` -> `ic_otso_dark`
  - `res/drawable-night/ic_splash_logo.xml` -> `ic_otso_light`
