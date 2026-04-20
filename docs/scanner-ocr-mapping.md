# Scanner ↔ OCR Mapping (Experimental)

## End-to-End Flow

1. User taps `Scan` in keyboard toolbar.
2. `EditorScreen` starts Google ML Kit Document Scanner.
3. Scanner returns page image URI.
4. `EditorViewModel.importScannedText()` forwards URI to shared OCR pipeline.
5. `OcrEngine.extract()` returns recognized text + engine label.
6. Text is inserted at current cursor in active tab.
7. Loading overlay appears during processing (`isOcrProcessing`).

## Key Files

- UI trigger + scanner integration:
  - `app/src/main/java/com/otso/app/ui/screens/EditorScreen.kt`
  - `app/src/main/java/com/otso/app/ui/components/OtsoKeyboardToolbar.kt`
- OCR orchestration + insertion behavior:
  - `app/src/main/java/com/otso/app/viewmodel/EditorViewModel.kt`
- OCR engines:
  - `app/src/main/java/com/otso/app/core/OcrEngine.kt`
- OCR test harness:
  - `app/src/androidTest/java/com/otso/app/OcrHardTest.kt`
  - `app/src/androidTest/java/com/otso/app/OcrImageOneProbeTest.kt`

## Current Engine Modes

- `MLKIT_BASELINE`
- `MLKIT_PREPROCESSED`
- `MLKIT_MULTISCALE`
- `MLKIT_LINEBOOST` (experimental 3rd engine)
- `MLKIT_HYBRID` (safe fallback path)

## Current Insert Rule

- OCR/scanner output is **not** opened in a new tab.
- Output is inserted into active tab at current cursor selection.
- If cursor is at the bottom, output appears at the bottom.

## Release Packaging Note

- ABI split enabled in Gradle (`arm64-v8a` and `x86_64`).
- Release builds therefore produce two APK variants by ABI.
