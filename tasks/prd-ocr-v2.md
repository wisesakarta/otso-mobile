# PRD: Otso Vision v2 — Neural OCR Engine

## Pengantar

Otso Vision v2 adalah evolusi major dari pipeline OCR Otso Note, dari sebuah wrapper ML Kit menjadi **engine pengenalan teks milik sendiri** dengan brand credential "Otso". Cakupan meliputi: peningkatan akurasi untuk printed text, receipt/nota Indonesia, dukungan tulisan tangan, dan pengenalan multi-script (CJK, Arabic, Devanagari). Ini mencakup training custom TFLite model untuk image binarization/preprocessing dan generalisasi semantic cleanup menjadi arsitektur yang dapat diperluas tanpa rebuild.

Target rilis: **v3.0.0** (Major Release)

---

## Tujuan

- Melatih dan mengintegrasikan custom TFLite model preprocessing milik Otso (`otso_vision_v2.tflite`)
- Meningkatkan akurasi OCR untuk dokumen cetak, nota belanja Indonesia, dan tulisan tangan
- Menambahkan dukungan pengenalan multi-script (Latin + CJK + Devanagari)
- Mengganti hardcoded semantic cleanup dengan sistem rule berbasis konfigurasi eksternal
- Tidak ada perubahan UI — seluruh improvement terjadi di belakang layar (engine-only)
- Mempertahankan kompatibilitas penuh dengan pipeline hybrid yang sudah ada

---

## User Stories

### US-001: Dataset Collection Pipeline
**Deskripsi:** Sebagai engineer, saya perlu mengumpulkan dan menyusun dataset training agar model Otso Vision dapat dilatih dengan data yang representatif.

**Kriteria Penerimaan:**
- [ ] Membuat direktori `training/` di root repository (di-gitignore) dengan struktur: `training/images/`, `training/labels/`, `training/scripts/`
- [ ] Membuat script Python (`training/scripts/prepare_dataset.py`) untuk mengonversi pasangan gambar-teks menjadi format yang kompatibel dengan TFLite Model Maker atau TensorFlow
- [ ] Mendokumentasikan kategori dataset minimum: (a) dokumen cetak Latin, (b) nota/receipt Indonesia, (c) tulisan tangan, (d) dokumen CJK
- [ ] Minimum 200 pasangan gambar-teks per kategori untuk training awal
- [ ] Dataset harus mencakup variasi: pencahayaan buruk, rotasi, blur, low-DPI
- [ ] Typecheck/lint lolos

### US-002: Custom Model Training Pipeline
**Deskripsi:** Sebagai engineer, saya perlu membangun pipeline training yang reproducible agar model Otso Vision dapat dilatih ulang kapan saja dengan dataset yang diperbarui.

**Kriteria Penerimaan:**
- [ ] Membuat script Python (`training/scripts/train_binarization.py`) yang melatih model binarization/preprocessing menggunakan TensorFlow
- [ ] Model arsitektur: lightweight U-Net atau MobileNet-based segmentation untuk document binarization
- [ ] Model harus mampu: (a) adaptive thresholding yang lebih baik dari Otsu, (b) noise removal, (c) shadow/lighting normalization
- [ ] Output model berupa file `.tflite` dengan ukuran ≤ 5 MB (agar APK tidak membengkak)
- [ ] Membuat script konversi (`training/scripts/convert_to_tflite.py`) dari SavedModel → TFLite dengan quantization
- [ ] Membuat script evaluasi (`training/scripts/evaluate.py`) yang membandingkan akurasi model Otso vs heuristic saat ini vs raw ML Kit
- [ ] Mendokumentasikan metrik evaluasi: Character Error Rate (CER), Word Error Rate (WER)
- [ ] Typecheck/lint lolos

### US-003: Integrasi Model TFLite ke NeuralVisionEngine
**Deskripsi:** Sebagai pengguna, saya ingin OCR menghasilkan teks yang lebih akurat secara otomatis tanpa harus mengubah pengaturan apa pun.

**Kriteria Penerimaan:**
- [ ] File `otso_vision_v2.tflite` ditempatkan di `app/src/main/assets/`
- [ ] `NeuralVisionEngine.kt` memuat model saat aplikasi dimulai (`loadModel()` dipanggil dari `OtsoApplication` atau lazy-init saat pertama kali digunakan)
- [ ] Fungsi `neuralBoost()` menggunakan TFLite Interpreter untuk preprocessing, bukan heuristic thresholding
- [ ] Fallback ke heuristic jika model gagal dimuat (backward compatible)
- [ ] Waktu inference model ≤ 200ms pada perangkat mid-range (Snapdragon 6-series)
- [ ] Memory footprint tambahan model ≤ 10 MB saat loaded
- [ ] Build berhasil (assembleDebug) dan OCR berfungsi pada device
- [ ] Typecheck/lint lolos

### US-004: Dukungan Multi-Script Recognition
**Deskripsi:** Sebagai pengguna yang menulis dalam bahasa non-Latin, saya ingin OCR dapat mengenali teks dalam bahasa Mandarin, Jepang, Korea, Hindi, dan Arabic.

**Kriteria Penerimaan:**
- [x] Menambahkan ML Kit recognizer untuk Chinese/Japanese/Korean (`com.google.mlkit:text-recognition-chinese`, `com.google.mlkit:text-recognition-japanese`, `com.google.mlkit:text-recognition-korean`)
- [x] Menambahkan ML Kit recognizer untuk Devanagari (`com.google.mlkit:text-recognition-devanagari`)
- [x] `OcrEngine.kt` secara otomatis mendeteksi script dominan menggunakan `IntelligenceEngine.identifyLanguage()` lalu memilih recognizer yang sesuai
- [x] Jika deteksi gagal, fallback ke Latin recognizer (perilaku saat ini)
- [x] Hybrid mode menjalankan kedua recognizer (Latin + detected script) dan memilih hasil terbaik berdasarkan `qualityScore()`
- [x] Build berhasil dan OCR berfungsi untuk teks Latin DAN non-Latin pada device
- [x] Typecheck/lint lolos

### US-005: Configurable Semantic Cleanup Rules
**Deskripsi:** Sebagai engineer, saya ingin aturan pembersihan semantik dapat diperbarui tanpa harus rebuild aplikasi, agar engine tetap bersih dan extensible.

**Kriteria Penerimaan:**
- [x] Membuat file konfigurasi JSON: `app/src/main/assets/ocr_cleanup_rules.json`
- [x] Format konfigurasi: array of `{ "pattern": "<regex>", "replacement": "<string>", "category": "<string>" }`
- [x] `OcrEngine.kt` memuat rules dari JSON saat inisialisasi
- [x] Menghapus semua hardcoded brand-specific rules dari `cleanupSemanticNoise()`
- [x] Memigrasikan rules yang ada (Indomaret, digit-to-letter fixes) ke file JSON
- [x] Menambahkan kategori rules: `"digit_correction"`, `"brand_normalization"`, `"unit_correction"`
- [x] Rules diterapkan secara berurutan sesuai urutan di JSON
- [x] Build berhasil dan OCR cleanup berfungsi identik dengan sebelumnya pada device
- [x] Typecheck/lint lolos

### US-006: Handwriting Recognition Support
**Deskripsi:** Sebagai pengguna, saya ingin dapat memfoto catatan tulisan tangan dan mendapatkan hasil teks yang dapat diedit.

**Kriteria Penerimaan:**
- [ ] Model Otso Vision v2 dilatih dengan dataset tulisan tangan (minimum 200 samples)
- [ ] Preprocessing pipeline menambahkan mode khusus handwriting: dilasi ringan + contrast boost agresif sebelum OCR
- [ ] `OcrEngine.EngineMode` menambahkan mode `HANDWRITING` yang menggunakan preprocessing khusus
- [ ] Hybrid mode secara otomatis mencoba mode handwriting jika printed text menghasilkan skor rendah
- [ ] Character Error Rate untuk handwriting ≤ 15% pada dataset evaluasi
- [ ] Build berhasil dan handwriting OCR berfungsi pada device
- [ ] Typecheck/lint lolos

---

## Persyaratan Fungsional

- FR-1: Sistem harus memuat custom TFLite model (`otso_vision_v2.tflite`) dari assets saat aplikasi dimulai atau saat pertama kali OCR digunakan (lazy loading)
- FR-2: Sistem harus melakukan fallback ke heuristic binarization jika model TFLite gagal dimuat
- FR-3: Sistem harus secara otomatis mendeteksi script bahasa dan memilih ML Kit recognizer yang sesuai (Latin, Chinese, Japanese, Korean, Devanagari)
- FR-4: Sistem harus memuat aturan semantic cleanup dari file JSON (`ocr_cleanup_rules.json`) di assets
- FR-5: Sistem harus mengevaluasi kualitas hasil OCR menggunakan `qualityScore()` yang sudah ada dan memilih hasil terbaik dari semua pipeline
- FR-6: Waktu total OCR (preprocessing + recognition + cleanup) tidak boleh melebihi 3 detik untuk gambar beresolusi standar (12 MP) pada perangkat mid-range
- FR-7: Ukuran total model TFLite tidak boleh melebihi 5 MB untuk menjaga ukuran APK tetap ringan (SOP Goal: LIGHTWEIGHT)
- FR-8: Sistem harus menyertakan mode preprocessing khusus untuk handwriting yang menggunakan dilasi dan contrast boost agresif

---

## Bukan-Tujuan (Non-Goals)

- **Tidak ada perubahan UI** — Tidak ada tombol baru, dialog baru, atau perubahan visual apa pun
- **Tidak ada real-time camera OCR** — Hanya pemrosesan gambar statis (dari galeri atau scan)
- **Tidak ada cloud API** — Semua pemrosesan sepenuhnya on-device
- **Tidak ada OCR untuk video** — Hanya frame gambar tunggal
- **Tidak ada auto-correction grammar** — Engine hanya mengekstrak dan membersihkan noise, tidak memperbaiki tata bahasa
- **Tidak ada model training di dalam aplikasi** — Training dilakukan offline di environment pengembangan

---

## Pertimbangan Teknis

### Dependencies Baru
```kotlin
// build.gradle.kts — additions
implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
implementation("com.google.mlkit:text-recognition-japanese:16.0.1")
implementation("com.google.mlkit:text-recognition-korean:16.0.1")
implementation("com.google.mlkit:text-recognition-devanagari:16.0.1")
```

### Model Architecture (Rekomendasi)
- **Base**: MobileNetV3-Small backbone untuk feature extraction
- **Head**: Lightweight decoder (3-layer conv) untuk binary mask output
- **Input**: 256×256 grayscale image
- **Output**: 256×256 binary mask (text vs background)
- **Quantization**: INT8 post-training quantization untuk ukuran ≤ 5MB
- **Framework**: TensorFlow 2.x → TFLite converter

### Training Environment
- Python 3.10+, TensorFlow 2.14+
- Dataset disimpan lokal di `training/` (gitignored)
- Output model disimpan di `app/src/main/assets/`

### APK Size Impact
| Komponen | Estimasi Ukuran |
|---|---|
| `otso_vision_v2.tflite` | ≤ 5 MB |
| ML Kit Chinese recognizer | ~2 MB (on-demand download) |
| ML Kit Japanese recognizer | ~2 MB (on-demand download) |
| ML Kit Korean recognizer | ~2 MB (on-demand download) |
| ML Kit Devanagari recognizer | ~2 MB (on-demand download) |
| `ocr_cleanup_rules.json` | < 10 KB |

**Catatan:** ML Kit script recognizers menggunakan on-demand model download, sehingga tidak menambah ukuran APK secara langsung.

### File yang Akan Dimodifikasi
| File | Perubahan |
|---|---|
| `core/NeuralVisionEngine.kt` | Integrasi TFLite model, replace heuristic |
| `core/OcrEngine.kt` | Multi-script routing, configurable cleanup |
| `app/build.gradle.kts` | Tambah ML Kit script dependencies |
| `assets/otso_vision_v2.tflite` | Model file baru |
| `assets/ocr_cleanup_rules.json` | Konfigurasi cleanup baru |

### File Baru (Training Pipeline — gitignored)
| File | Fungsi |
|---|---|
| `training/scripts/prepare_dataset.py` | Dataset preparation |
| `training/scripts/train_binarization.py` | Model training |
| `training/scripts/convert_to_tflite.py` | TFLite conversion |
| `training/scripts/evaluate.py` | Accuracy evaluation |

---

## Metrik Sukses

- **Akurasi Printed Text**: Word Error Rate (WER) ≤ 5% pada dokumen cetak standar
- **Akurasi Receipt**: WER ≤ 10% pada nota/receipt Indonesia (variasi pencahayaan)
- **Akurasi Handwriting**: Character Error Rate (CER) ≤ 15% pada tulisan tangan jelas
- **Multi-Script**: CJK recognition menghasilkan teks yang dapat dibaca (WER ≤ 20%)
- **Performa**: Total OCR time ≤ 3 detik pada Snapdragon 6-series
- **Ukuran**: Model TFLite ≤ 5 MB, total APK size increase ≤ 8 MB
- **Regresi**: Tidak ada penurunan kualitas untuk use case yang sudah ada (Latin printed text)

---

## Pertanyaan Terbuka

1. **Dataset tulisan tangan**: Apakah fokus pada tulisan tangan Latin saja, atau termasuk tulisan tangan aksara non-Latin?
2. **Model versioning**: Apakah perlu mekanisme untuk update model OTA (over-the-air) tanpa update APK?
3. **Benchmark device**: Selain Snapdragon 6-series, apakah ada target perangkat minimum lainnya?
4. **Arabic script**: ML Kit belum menyediakan recognizer untuk Arabic secara offline — apakah ini menjadi blocker atau bisa ditunda?

---

## Urutan Implementasi (Rekomendasi)

| Phase | User Story | Effort | Dependency |
|---|---|---|---|
| 1 | US-005: Configurable Cleanup Rules | 1 sesi | Tidak ada |
| 2 | US-004: Multi-Script Recognition | 1 sesi | Tidak ada |
| 3 | US-001: Dataset Collection | 2-3 sesi | Tidak ada |
| 4 | US-002: Training Pipeline | 2-3 sesi | US-001 |
| 5 | US-003: TFLite Integration | 1-2 sesi | US-002 |
| 6 | US-006: Handwriting Support | 2 sesi | US-002, US-003 |
