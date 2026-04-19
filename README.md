# Otso Mobile

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" alt="Otso" width="72">
</p>

<p align="center">
  <strong>v1.0.0-rc.2</strong>
</p>

**Otso** is a high-fidelity Android text editor focused on speed, stability, and craftsmanship — the mobile counterpart to [Otso Desktop](https://github.com/wisesakarta/solum).

**Otso** adalah editor teks Android beresolusi tinggi yang berfokus pada kecepatan, stabilitas, dan pengerjaan yang cermat.

---

## Features

- Multi-tab editing with full session restore
- Encoding support: UTF-8, UTF-8 BOM, UTF-16 LE/BE
- Line ending support: LF, CRLF, CR
- Find & Replace with full-text match highlighting
- Internal storage for notes + SAF (Storage Access Framework) for external files
- Custom font loading from device storage
- System / Dark / Light theme with DataStore persistence
- Gesture-based navigation — swipe down for tab manager
- Floating keyboard accessory toolbar

---

## Requirements

- Android 12 (API 31) or higher
- ARM64 or x86_64 device

---

## Build

```bash
./gradlew assembleDebug
```

Release APK:
```bash
./gradlew assembleRelease
```

---

## Release Channel
Mobile release tags follow: `mobile/v*`
Current release: `mobile/v1.0.0-rc.2`
Release assets:
- `Otso-mobile-v1.0.0-rc.2-arm64.apk`
- `Otso-mobile-v1.0.0-rc.2-x86_64.apk`

---

## Repository Structure
```
app/src/main/java/com/otso/app/
├── core/        # TextCodec, FileIO, SessionIO, OtsoPreferences
├── model/       # TabDocument, data classes
├── ui/
│   ├── components/  # OtsoEditor, OtsoTabBar, OtsoFindBar, OtsoKeyboardToolbar, etc.
│   ├── screens/     # EditorScreen, AboutScreen
│   └── theme/       # OtsoTheme, design tokens
└── viewmodel/   # EditorViewModel
```

---

## Desktop Counterpart
Otso Desktop (Win32, C++17): [github.com/wisesakarta/otso.git](https://github.com/wisesakarta/otso.git)

---

## License
MIT License — see [LICENSE](LICENSE)

Crafted by Technical Standard
