# Environment Separation: Otso (Production) vs Kontio (Development)

## Overview

The project runs two parallel environments that can be installed simultaneously on the same device. They are distinguished by app ID, display name, launcher icon, and background color.

| Property | Production (Otso) | Development (Kontio) |
|---|---|---|
| App ID | `com.otso.app` | `com.otso.app.dev` |
| Display name | Otso | Kontio |
| Launcher icon | White bear, blue gradient bg | White bear, orange bg (`#ff5400`) |
| Version suffix | _(none)_ | `-dev` |
| Build command | `./gradlew installRelease` | `./gradlew installDebug` |

---

## How It Works — Android Source Set Override

Android's Gradle build system merges resources from multiple source sets in priority order:

```
debug/res/  >  main/res/
```

Any resource file placed in `app/src/debug/res/` with the **same filename** as one in `app/src/main/res/` will automatically override the main version when building the `debug` variant. The production `release` build is never affected.

No Kotlin code was changed. `R.drawable.ic_otso_dark`, `R.drawable.ic_otso_light`, and `@mipmap/ic_launcher` resolve to the correct variant at compile time automatically.

---

## File Structure

```
app/src/
│
├── main/res/                              ← PRODUCTION assets (Otso)
│   ├── drawable/
│   │   ├── ic_launcher_background.xml    — Background shape, color: #0E1117
│   │   ├── ic_otso_dark.png              — In-app logo, dark mode
│   │   └── ic_otso_light.png             — In-app logo, light mode
│   ├── mipmap-anydpi-v26/
│   │   └── ic_launcher.xml               — Adaptive icon definition (API 26+)
│   ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/
│   │   ├── ic_launcher.png               — Legacy fallback (Pre-API 26)
│   │   └── ic_launcher_foreground.png    — Adaptive icon foreground layer
│   └── values/
│       ├── colors.xml                    — ic_launcher_background: #0E1117
│       └── strings.xml                   — app_name: "Otso"
│
└── debug/res/                             ← DEVELOPMENT overrides (Kontio)
    ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/
    │   └── ic_launcher_foreground.png    — Kontio launcher icon (orange bg)
    └── values/
        ├── colors.xml                    — ic_launcher_background: #ff5400
        └── strings.xml                   — app_name: "Kontio"
```

---

## Launcher Icon — Adaptive Icon Layer Composition

The launcher icon is assembled at runtime by Android from two layers defined in `mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<adaptive-icon>
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
```

| Layer | Production resolves to | Development resolves to |
|---|---|---|
| `<background>` | `drawable/ic_launcher_background.xml` → `#0E1117` (black) | Same XML → `#ff5400` (orange) via `debug/values/colors.xml` override |
| `<foreground>` | `mipmap-*/ic_launcher_foreground.png` (blue gradient bear) | `debug/mipmap-*/ic_launcher_foreground.png` (orange bg bear) |

---

## Launcher Icon — Adaptive Icon Safe Zone

Android adaptive icons have a fixed canvas of **108dp × 108dp**, split into two zones:

```
┌─────────────────────────────────┐
│         108dp total canvas      │
│   ┌─────────────────────────┐   │
│18dp│                         │18dp│  ← BLEED ZONE — always clipped by launcher
│   │     72dp × 72dp          │   │
│   │      SAFE ZONE           │   │  ← Only this area is guaranteed visible
│   │                         │   │
│   └─────────────────────────┘   │
└─────────────────────────────────┘
```

The outer **18dp on each side (16.7%)** is the bleed zone — it is always masked by the launcher shape (circle, squircle, etc.) and must never contain critical content. The subject must fit within the **center 72dp (66.7%)** safe zone.

### Safe Zone Compliance Audit

Analysis via Pillow bounding box detection on the xxxhdpi (432px) assets:

| Asset | Subject coverage | Safe zone (≤66.7%) | Status |
|---|---|---|---|
| Otso `ic_launcher_foreground.png` | 66.9% | Borderline | Passes visually |
| Kontio (original source) | **99.1%** | Exceeds by 32.4% | Bear severely clipped |
| Kontio (fixed) | 66.7% | Exact | Fully visible |

### Fix Applied to Kontio

The Kontio source PNG (1105×1105px) was designed full-bleed — the bear filled 99% of the canvas. The fix scales the content to 66.7% of each canvas size and centers it on a transparent background, so the `<background>` layer fills the remaining bleed zone seamlessly.

```python
SAFE_RATIO = 0.667  # 72dp / 108dp

safe_px  = int(canvas_px * SAFE_RATIO)   # e.g., 288px at xxxhdpi
scaled   = img.resize((safe_px, safe_px), Image.LANCZOS)
canvas   = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
offset   = (canvas_px - safe_px) // 2    # center: 72px at xxxhdpi
canvas.paste(scaled, (offset, offset), scaled)
```

---

## Launcher Icon — Density Specifications

| Density | Canvas | Safe zone | Content size | Offset | Path |
|---|---|---|---|---|---|
| mdpi | 108 × 108 px | 72 × 72 px | 72 × 72 px | 18px | `mipmap-mdpi/ic_launcher_foreground.png` |
| hdpi | 162 × 162 px | 108 × 108 px | 108 × 108 px | 27px | `mipmap-hdpi/ic_launcher_foreground.png` |
| xhdpi | 216 × 216 px | 144 × 144 px | 144 × 144 px | 36px | `mipmap-xhdpi/ic_launcher_foreground.png` |
| xxhdpi | 324 × 324 px | 216 × 216 px | 216 × 216 px | 54px | `mipmap-xxhdpi/ic_launcher_foreground.png` |
| xxxhdpi | 432 × 432 px | 288 × 288 px | 288 × 288 px | 72px | `mipmap-xxxhdpi/ic_launcher_foreground.png` |

Source asset for Kontio: 1105 × 1105 px, RGBA, downsampled with LANCZOS filter then safe-zone padded.

---

## Gradle Configuration

```kotlin
// app/build.gradle.kts
buildTypes {
    debug {
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
    }
    release {
        isMinifyEnabled = true
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(...)
    }
}
```

---

## Adding or Updating Dev Assets

To update a Kontio asset, replace the file in `app/src/debug/res/` at the correct path and run `./gradlew installDebug`. Production build is unaffected.

To add a new resource override for the dev environment only, mirror the path from `main/res/` into `debug/res/` with the same filename.

### Launcher Icon Asset Requirements

When replacing `ic_launcher_foreground.png` for either environment:

1. **Provide one high-res source PNG** (minimum 432×432px, recommended 1024px+, RGBA mode).
2. **Subject must be within the center 66.7%** of the canvas — if the source is full-bleed, apply the safe-zone padding script above before placing in the mipmap folders.
3. **Generate all 5 density files** from the single source using LANCZOS downsampling (see density table above).
4. The `<background>` color layer is controlled separately via `values/colors.xml` — the foreground PNG should use a transparent outer region so the background layer shows through the bleed zone.
