# Otso Mobile

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png" alt="Otso" width="320">
</p>

<p align="center">
  <strong>v2.2.0</strong>
</p>

**Otso Note** is a product of the **Otso Department**, a division of **Technical Standard**. Driven by the mission to achieve **"The Renaissance of Software"** and uphold the culture of **"Tools for Tough,"** we focus on creating high-fidelity instruments for power users. 

We are a massive team because we are open-source. I, **Karta Wisesa**, believe that building this tool in the open is the only way to empower those who truly care about the craft of digital writing.

---

**Otso Note** adalah produk dari **Otso Department**, bagian dari **Technical Standard**. Didorong oleh misi untuk mewujudkan **"The Renaissance of Software"** dan mempertahankan budaya **"Tools for Tough,"** kami berfokus pada penciptaan instrumen beresolusi tinggi bagi para *power users*.

Kami adalah tim yang masif karena kami berbasis *open-source*. Saya, **Karta Wisesa**, percaya bahwa membangun alat ini secara terbuka adalah cara terbaik untuk memberdayakan mereka yang benar-benar peduli pada seni penulisan digital.

---

## Key Features

- **High-Performance Engine:** Fluid rendering for large documents utilizing `LazyColumn` recycle pooling, `ConcurrentHashMap` color caching, and strict `@Stable` state hoisting. Targeting 120 FPS.
- **Multi-tab Editing:** Seamless context switching with full session restore.
- **Encoding Mastery:** Native support for UTF-8, UTF-8 BOM, UTF-16 LE/BE.
- **Line Ending Control:** LF, CRLF, CR conversions.
- **Advanced Find & Replace:** Full-text match highlighting with regex support.
- **Text Highlighter:** Color wheel-based highlighting with persistent span styling.
- **On-Device OCR:** Neural vision preprocessing (TFLite) + ML Kit text recognition for image-to-text extraction.
- **On-Device Translation:** ML Kit-powered offline translation with automatic language detection.
- **Storage Independence:** Internal storage for quick notes + SAF (Storage Access Framework) for external file management.
- **Typography Control:** Custom font loading directly from device storage.
- **Glassmorphism UI:** Multi-layer glass material toolbars with Squircle geometry and Phosphor icon set.
- **Adaptive UI:** System / Dark / Light theme with DataStore persistence.
- **Fluid Navigation:** Gesture-based tab manager (swipe down) and floating keyboard accessory toolbar.
- **Native Experience:** Android 12+ Splash Screen integration.

---

## Requirements

- Android 12 (API 31) or higher
- ARM64 device (Optimized via ABI Splits)

---

## Build

```bash
./gradlew assembleDebug
```

## Production Release
Local execution of assembleRelease is disabled for security. Production builds are exclusively handled by the GitHub Actions CI/CD Pipeline. Triggering the `release.yml` workflow will automatically build, sign (via injected Keystore Secrets), and upload the R8-minified APK.

---

## Release Channel
Current release: **v2.2.0**
Release assets:
- `OtsoNote-v2.2.0-Release` (arm64-v8a optimized, via GitHub Actions Artifacts)

---

## Repository Structure
```
app/src/main/java/com/otso/app/
├── core/
│   ├── TextCodec          # Encoding detection (UTF-8/16, BOM)
│   ├── FileIO             # SAF + internal storage I/O
│   ├── SessionIO          # Tab session persistence
│   ├── FontManager        # Custom font loader from device
│   ├── OcrEngine          # ML Kit text recognition + Otsu binarization
│   ├── NeuralVisionEngine # TFLite neural image preprocessing
│   ├── IntelligenceEngine # Language ID + entity extraction
│   ├── TranslationEngine  # ML Kit on-device translation
│   └── OtsoPreferences    # DataStore preferences
├── model/                 # TabDocument, RichTextAST (@Stable node parser)
├── ui/
│   ├── components/        # OtsoEditor, OtsoTabBar, OtsoFindBar,
│   │                      # OtsoFormattingToolbar, OtsoKeyboardToolbar,
│   │                      # OtsoColorWheel, OtsoHighlighterPopup,
│   │                      # OtsoMenuSheet, OtsoIcons (Phosphor)
│   ├── screens/           # EditorScreen, AboutScreen, AstPreviewScreen
│   └── theme/             # OtsoTheme, design tokens
├── viewmodel/             # EditorViewModel, RichTextState
└── MainActivity.kt        # Single-activity entry
```

---

## Desktop Counterpart
Otso Desktop (Win32, C++17): [github.com/wisesakarta/otso.git](https://github.com/wisesakarta/otso.git)

---

## License
MIT License — see [LICENSE](LICENSE)

Crafted by Technical Standard / Karta Sena Wisesa or Farhan Arif if you get confused who's Karta Wisesa
