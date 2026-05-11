# Design Assets — OtsoMobile Debug APK

> Dokumen ini menjabarkan seluruh color palette, logos, icons, dan font families yang digunakan pada debug APK.
> Plan: (1) change color palette · (2) change logos & in-app logos · (3) change icons · (4) change default fonts

---

## 1. COLOR PALETTE

### 1.1 Design Tokens (Kotlin) — `app/src/main/java/com/otso/app/ui/theme/OtsoTheme.kt`

#### Dark Mode
| Token | Hex | Description |
|---|---|---|
| `darkBackground` | `#FF000000` | Pure black background |
| `darkInk` | `#FFD6D6D6` | Primary text |
| `darkMuted` | `#FFABABAB` | Secondary/muted text |
| `darkEdge` | `#FF333333` | Borders & dividers |
| `darkSurface` | `#FF1C1C1C` | Surfaces & containers |
| `darkShadow` | `#FF000000` | Drop shadows |

#### Light Mode
| Token | Hex | Description |
|---|---|---|
| `lightBackground` | `#FFF5F5F3` | Warm off-white background |
| `lightInk` | `#FF1A1A1A` | Primary text |
| `lightMuted` | `#FF4D4D4D` | Secondary/muted text |
| `lightEdge` | `#FFE0E2E2` | Borders & dividers |
| `lightSurface` | `#FFF2F4F4` | Surfaces & containers |
| `lightShadow` | `#1A2A3A5A` | Soft navy-tinted shadow (10% opacity) |

#### Accent & Special
| Token | Hex | Description |
|---|---|---|
| `accent` | `#FF001AE2` | Blueprint Blue — primary accent |
| `accentMuted` | `#2E001AE2` | Blueprint Blue 18% opacity |
| `selectionBackground` | `#73001AE2` | Text selection highlight ~45% opacity |
| `transparent` | `#00000000` | Transparent |

#### Highlight Palette (6-color editorial)
| Token | Hex | Swatch |
|---|---|---|
| `highlightYellow` | `#FFF9EB73` | Yellow |
| `highlightOrange` | `#FFFDBA74` | Orange |
| `highlightRed` | `#FFFCA5A5` | Red/Pink |
| `highlightPurple` | `#FFD8B4FE` | Purple |
| `highlightBlue` | `#FF93C5FD` | Blue |
| `highlightGreen` | `#FF86EFAC` | Green |

---

### 1.2 XML Color Resources

#### Production — `app/src/main/res/values/colors.xml`
| Name | Hex | Used For |
|---|---|---|
| `ic_launcher_background` | `#0E1117` | App launcher icon background (very dark blue-black) |
| `accent_primary` | `#0055FF` | Accent reference in XML |

#### Debug — `app/src/debug/res/values/colors.xml`
| Name | Hex | Used For |
|---|---|---|
| `ic_launcher_background` | `#8A5A44` | Debug launcher icon background (brown/sepia) |
| `accent_primary` | `#8A5A44` | Debug accent reference in XML |

> **Note untuk Plan #1:** Untuk mengganti color palette, ubah nilai hex di `OtsoTheme.kt` (design tokens Kotlin) dan `colors.xml` (production & debug).

---

## 2. LOGOS & IN-APP LOGOS

### 2.1 App Launcher Icon (Home Screen)

#### Adaptive Icon Definition
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — Adaptive icon manifest
- `app/src/main/res/drawable/ic_launcher_background.xml` — Background shape XML (warna dari `@color/ic_launcher_background`)

#### Production Launcher Icons
| Density | Foreground File |
|---|---|
| mdpi | `app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | `app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | `app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |
| mdpi | `app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png` |
| hdpi | `app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png` |
| xhdpi | `app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png` |
| xxhdpi | `app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png` |
| xxxhdpi | `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png` |

#### Debug Launcher Icons (Override foreground)
| Density | Foreground File |
|---|---|
| mdpi | `app/src/debug/res/mipmap-mdpi/ic_launcher_foreground.png` |
| hdpi | `app/src/debug/res/mipmap-hdpi/ic_launcher_foreground.png` |
| xhdpi | `app/src/debug/res/mipmap-xhdpi/ic_launcher_foreground.png` |
| xxhdpi | `app/src/debug/res/mipmap-xxhdpi/ic_launcher_foreground.png` |
| xxxhdpi | `app/src/debug/res/mipmap-xxxhdpi/ic_launcher_foreground.png` |

---

### 2.2 In-App Brand Logos

Logo di dalam app di-load melalui `BrandAssets` — source berbeda antara debug dan release build.

#### Release Build — `app/src/release/java/com/otso/app/BrandAssets.kt`
| Resource | File | Usage |
|---|---|---|
| `R.drawable.ic_otso_dark` | `app/src/main/res/drawable/ic_otso_dark.png` | Logo untuk dark mode |
| `R.drawable.ic_otso_light` | `app/src/main/res/drawable/ic_otso_light.png` | Logo untuk light mode |

#### Debug Build — `app/src/debug/java/com/otso/app/BrandAssets.kt`
| Resource | File | Usage |
|---|---|---|
| `R.drawable.ic_kontio_dark` | `app/src/debug/res/drawable/ic_kontio_dark.png` | Logo debug dark mode |
| `R.drawable.ic_kontio_light` | `app/src/debug/res/drawable/ic_kontio_light.png` | Logo debug light mode |

**Dimuat di:**
- `OtsoTabBar.kt` — ukuran 20.dp, identity anchor di tab bar
- `AboutScreen.kt` — tampilan About screen

> **Note untuk Plan #2:** Untuk mengganti in-app logo debug, ganti file PNG di `app/src/debug/res/drawable/` (ic_kontio_dark.png & ic_kontio_light.png). Untuk launcher icon debug, ganti file PNG di `app/src/debug/res/mipmap-*/ic_launcher_foreground.png`.

---

## 3. ICONS

Semua icon adalah custom `ImageVector` yang di-build manual menggunakan path geometry Phosphor Icons v2.1.0. **Tidak ada external icon library** — semua icon didefinisikan di satu file.

**Source: `app/src/main/java/com/otso/app/ui/components/OtsoIcons.kt`**

### 3.1 Catalog Lengkap (36 Icons)

| # | Icon Name | Description | Digunakan Di |
|---|---|---|---|
| 1 | `X` | Close/dismiss (2 diagonal lines) | FindBar, FormattingToolbar, HighlighterPopup |
| 2 | `CaretUp` | Up chevron/arrow | FindBar |
| 3 | `CaretDown` | Down chevron/arrow | FindBar |
| 4 | `Camera` | Camera/scan | KeyboardToolbar |
| 5 | `Undo` | Undo (curved arrow CCW) | KeyboardToolbar |
| 6 | `Redo` | Redo (curved arrow CW) | KeyboardToolbar |
| 7 | `Check` | Checkmark | — |
| 8 | `ArrowCounterClockwise` | Rotate counterclockwise | — |
| 9 | `ArrowLeft` | Left arrow with wings | FormattingToolbar, OtsoBackButton |
| 10 | `Plus` | Add/create (+) | MenuSheet, TabSwitcherSheet |
| 11 | `Minus` | Subtract/remove (−) | MenuSheet |
| 12 | `TextB` | Bold formatting | FormattingToolbar |
| 13 | `TextItalic` | Italic formatting | FormattingToolbar |
| 14 | `TextUnderline` | Underline formatting | FormattingToolbar |
| 15 | `TextStrikethrough` | Strikethrough formatting | FormattingToolbar |
| 16 | `ListBullets` | Bullet list | FormattingToolbar |
| 17 | `ListNumbers` | Numbered list | FormattingToolbar |
| 18 | `Code` | Code/inline code | FormattingToolbar |
| 19 | `Link` | Hyperlink | FormattingToolbar |
| 20 | `Highlighter` | Highlight text | FormattingToolbar |
| 21 | `PaperPlaneTilt` | Send/submit | — |
| 22 | `LetterM` | Monospace toggle | KeyboardToolbar |
| 23 | `TabKey` | Tab insert | KeyboardToolbar |
| 24 | `Tabs` | Tab switcher | — |
| 25 | `Parentheses` | Insert `()` | KeyboardToolbar |
| 26 | `Brackets` | Insert `[]` | KeyboardToolbar |
| 27 | `Slash` | Insert `/` | KeyboardToolbar |
| 28 | `Quotes` | Insert `""` | KeyboardToolbar |
| 29 | `Circle` | Circle outline | — |
| 30 | `ArrowClockwise` | Rotate clockwise | FindBar |
| 31 | `Folder` | Folder | — |
| 32 | `FolderCounterClockwise` | Folder + undo arrow | MenuSheet |
| 33 | `Asterisk` | Asterisk * (loading spinner) | OtsoLoading |
| 34 | `Brain` | Brain / AI icon | MenuSheet |
| 35 | `WarningCircle` | Warning/alert in circle | EditorScreen |
| 36 | `Info` | Information | — |

### 3.2 Icon Usage per Component

| Component | Icons Used |
|---|---|
| `OtsoFindBar` | `CaretDown`, `CaretUp`, `X`, `ArrowClockwise` |
| `OtsoFormattingToolbar` | `TextB`, `TextItalic`, `TextStrikethrough`, `TextUnderline`, `Code`, `Highlighter`, `Link`, `X`, `ArrowLeft`, `ListBullets`, `ListNumbers` |
| `OtsoHighlighterPopup` | `X` (clear/transparent) |
| `OtsoKeyboardToolbar` | `Undo`, `Redo`, `Camera`, `LetterM`, `TabKey`, `Parentheses`, `Brackets`, `Quotes`, `Slash` |
| `OtsoMenuSheet` | `FolderCounterClockwise`, `Minus`, `Plus`, `Brain` |
| `OtsoTabSwitcherSheet` | `Plus` |
| `OtsoLoading` | `Asterisk` (rotating spinner) |
| `EditorScreen` | `WarningCircle` |
| `OtsoBackButton` | `ArrowLeft` |

> **Note untuk Plan #3:** Untuk mengganti icon, edit path geometry di `OtsoIcons.kt`. Setiap icon adalah `ImageVector.Builder` dengan manual path definition.

---

## 4. FONT FAMILIES

**Source: `app/src/main/java/com/otso/app/ui/theme/OtsoTheme.kt`**
**Font files: `app/src/main/res/font/`**

### 4.1 GeneralSans — Primary UI & Editor Font

| File | Weight | FontWeight |
|---|---|---|
| `general_sans_light.otf` | Light | `FontWeight.Light` |
| `general_sans_regular.otf` | Regular | `FontWeight.Normal` |
| `general_sans_medium.otf` | Medium | `FontWeight.Medium` |
| `general_sans_semibold.otf` | Semibold | `FontWeight.SemiBold` |
| `general_sans_bold.otf` | Bold | `FontWeight.Bold` |

#### Typography Styles (GeneralSans)
| Style Token | Size | Weight | Line Height | Usage |
|---|---|---|---|---|
| `editorBody` | 15sp | Normal | 22sp | Main editor body text |
| `editorLarge` | 18sp | Normal | 22sp | Large editor text |
| `uiLabel` | 13sp | Normal | 18sp | UI labels |
| `uiLabelMedium` | 13sp | Medium | 18sp | Emphasized UI labels |
| `uiCaption` | 11sp | Normal | 14sp | Captions & hints |
| `uiTitle` | 16sp | SemiBold | 20sp | Section titles |
| `uiTitleLarge` | 22sp | SemiBold | 26sp | Large titles |
| `uiBodyLarge` | 18sp | SemiBold | 22sp | Large body text |
| `uiDisplayLarge` | 64sp | Bold | 70sp | Display/hero text |
| `uiTechnical` | 11sp | Normal | 14sp | Technical/monospace numbers |

---

### 4.2 JetBrainsMono — Technical & Code Font

| File | Weight | FontWeight | Usage |
|---|---|---|---|
| `jetbrains_mono_regular.ttf` | Regular | `FontWeight.Normal` | Code blocks, monospace toggle |

> **Note untuk Plan #4:** Untuk mengganti font, (1) tambah/ganti file font di `app/src/main/res/font/`, (2) update `FontFamily` definition di `OtsoTheme.kt`, (3) update `Typography` style definitions.

---

## Summary

| Category | Count | Primary Source |
|---|---|---|
| Color tokens (Kotlin) | 22 tokens | `OtsoTheme.kt` |
| XML color resources | 4 entries | `colors.xml` (main & debug) |
| Launcher icon files | 15 PNG + 2 XML | `mipmap-*` folders |
| In-app logo files | 4 PNG | `drawable/` (release & debug) |
| Custom icons | 36 ImageVectors | `OtsoIcons.kt` |
| Font files | 6 files | `res/font/` |
| Typography styles | 10 styles | `OtsoTheme.kt` |

**Primary Accent:** Blueprint Blue `#001AE2`
**Primary Font:** GeneralSans (5 weights)
**Code Font:** JetBrainsMono (Regular)
