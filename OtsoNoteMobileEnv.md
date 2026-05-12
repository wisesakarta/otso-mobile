# Environment Separation: Otso (Production) vs Kontio (Development)

## Overview

The project runs two parallel environments that can be installed simultaneously on the same device. They are distinguished by app ID, display name, launcher icon, and background color.

| Property | Production (Otso) | Development (Kontio) |
|---|---|---|
| App ID | `com.otso.app` | `com.otso.app.dev` |
| Display name | Otso | Kontio |
| Launcher icon | White bear, blue gradient bg | Kontio icon, warm brown bg (`#826245`) |
| Default font | General Sans (static OTF) | Excon Light + Regular (static OTF, 2 weights) |
| Accent color | `#001AE2` (Blueprint Blue) | `#826245` (Kontio Brown) |
| Version suffix | _(none)_ | `-dev` |
| Build command | `./gradlew installRelease` | `./gradlew installDebug` |

---

## How It Works — Source Set Separation

We use Android's source set mechanism to separate **Production (Otso)** from **Experiment (Kontio)** without polluting the `main` source set.

1.  **Assets**:
    *   `main/res/drawable/ic_otso_*`: Production logos.
    *   `debug/res/drawable/ic_kontio_*`: Experimental logos.
    *   `debug/res/font/excon_light.otf`, `excon_regular.otf`: Excon static fonts (Light + Regular only).
2.  **Code Abstraction**:
    *   Instead of hardcoding resources in UI components, we use `BrandAssets` properties.
    *   `BrandAssets.kt` is split across source sets:
        *   `app/src/release/java/com/otso/app/BrandAssets.kt` → `ic_otso_*`, `GeneralSans`.
        *   `app/src/debug/java/com/otso/app/BrandAssets.kt` → `ic_kontio_*`, `ExconVariable`.

This ensures that experimental brand names and assets never leak into the Production build.

---

## File Structure

```
app/src/
│
├── main/                                  ← SHARED LOGIC
│   ├── java/com/otso/app/ui/             — UI code using BrandAssets
│   └── res/drawable/                     
│       ├── ic_otso_dark.png              — PRODUCTION logo (Dark)
│       └── ic_otso_light.png             — PRODUCTION logo (Light)
│
├── debug/                                 ← EXPERIMENT (Kontio)
│   ├── java/com/otso/app/BrandAssets.kt  — Returns ic_kontio_* + Excon Light/Regular fonts
│   └── res/
│       ├── drawable/                     
│       │   ├── ic_kontio_dark.png        — EXPERIMENT logo (Dark mode)
│       │   └── ic_kontio_light.png       — EXPERIMENT logo (Light mode)
│       ├── font/
│       │   ├── excon_light.otf           — Excon Light (lightest allowed weight)
│       │   └── excon_regular.otf         — Excon Regular (heaviest allowed weight)
│       ├── mipmap-*/ic_launcher_foreground.png  — Kontio launcher icon (58% safe ratio)
│       └── values/
│           ├── colors.xml                — accent_primary + ic_launcher_background: #826245
│           ├── strings.xml               — app_name: Kontio
│           └── themes.xml                — splash: ic_kontio_dark on #826245 background
│
└── release/                               ← PRODUCTION (Otso)
    └── java/com/otso/app/BrandAssets.kt  — Returns ic_otso_*
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
| `<background>` | `drawable/ic_launcher_background.xml` → `#0E1117` (black) | Same XML → `#826245` (Kontio brown) via `debug/values/colors.xml` override |
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
