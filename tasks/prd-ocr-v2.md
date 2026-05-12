# PRD: Otso Vision v2 — Model-First OCR Enhancement Engine

## Pengantar

Otso Vision v2 adalah evolusi major dari pipeline OCR Otso Note, dari wrapper ML Kit pasif menjadi **model-first OCR enhancement engine** dengan signature Otso. V2 tidak mengganti recognizer ML Kit sebagai backend pengenalan teks; V2 membangun intelligence layer milik Otso di sekitar recognition: model preprocessing on-device, routing engine, candidate ranking, layout reconstruction, semantic cleanup, dan degraded-mode observability.

Artifact model utama V2 adalah `otso_docclean_v1.tflite`, yaitu custom TFLite document-cleanup/binarization model untuk meningkatkan kualitas gambar sebelum OCR. Cakupan V2 meliputi peningkatan akurasi untuk printed text, receipt/nota Indonesia, dukungan awal handwriting melalui preprocessing khusus, dan pengenalan multi-script yang tetap menggunakan ML Kit recognizer.

Target rilis: **v3.0.0** (Major Release)

---

## Tujuan

- Melatih dan mengintegrasikan custom TFLite document-cleanup model milik Otso (`otso_docclean_v1.tflite`)
- Meningkatkan akurasi OCR untuk dokumen cetak, nota belanja Indonesia, dan tulisan tangan
- Menambahkan dukungan pengenalan multi-script via ML Kit backend (Latin + CJK + Devanagari)
- Mengganti hardcoded semantic cleanup dengan sistem rule berbasis konfigurasi eksternal
- Tidak ada perubahan UI — seluruh improvement terjadi di belakang layar (engine-only)
- Menjadikan Otso model sebagai primary preprocessing path, dengan heuristic lama hanya sebagai degraded mode yang observable

---

## User Stories

### US-001: Dataset Collection Pipeline
**Deskripsi:** Sebagai engineer, saya perlu mengumpulkan dan menyusun dataset training agar model Otso Vision dapat dilatih dengan data yang representatif. (Status: SELESAI di Home Server)

**Kriteria Penerimaan:**
- [x] Membuat direktori `training/` di root repository (di-gitignore) dengan struktur: `training/images/`, `training/labels/`, `training/scripts/`
- [x] Membuat script Python (`training/scripts/prepare_dataset.py` dkk) untuk mengonversi pasangan gambar-teks menjadi format yang kompatibel
- [x] Mendokumentasikan kategori dataset minimum: (a) dokumen cetak Latin (SROIE), (b) nota/receipt Indonesia (CORD), (c) tulisan tangan (IAM), (d) dokumen sulit (DIBCO)
- [x] Minimum 200 pasangan gambar-teks per kategori untuk training awal (Realita: 12,482 total data terkumpul)
- [x] Dataset harus mencakup variasi: pencahayaan buruk, rotasi, blur, low-DPI
- [x] Typecheck/lint lolos

### US-002: Custom Model Training Pipeline
**Deskripsi:** Sebagai engineer, saya perlu membangun pipeline training yang reproducible agar model Otso Vision dapat dilatih ulang kapan saja dengan dataset yang diperbarui.

**Kriteria Penerimaan:**
- [ ] Membuat script Python (`training/scripts/train_binarization.py`) yang melatih model binarization/preprocessing menggunakan TensorFlow
- [x] Model arsitektur: MobileNetV3-Small segmentation untuk document binarization/doc-cleanup (Colab prototype)
- [ ] Model harus mampu: (a) adaptive thresholding yang lebih baik dari Otsu, (b) noise removal, (c) shadow/lighting normalization
- [x] Output model berupa file `.tflite` dengan ukuran ≤ 5 MB (Realita: `otso_docclean_v1.tflite` = 1.41 MB / 1,473,720 bytes)
- [ ] Membuat script konversi (`training/scripts/convert_to_tflite.py`) dari SavedModel → TFLite dengan quantization
- [ ] Membuat script evaluasi (`training/scripts/evaluate.py`) yang membandingkan akurasi model Otso vs heuristic saat ini vs raw ML Kit
- [ ] Mendokumentasikan metrik evaluasi: Character Error Rate (CER), Word Error Rate (WER)
- [ ] Typecheck/lint lolos

### US-003: Integrasi Model TFLite ke NeuralVisionEngine
**Deskripsi:** Sebagai pengguna, saya ingin OCR menghasilkan teks yang lebih akurat secara otomatis tanpa harus mengubah pengaturan apa pun.

**Kriteria Penerimaan:**
- [ ] File `otso_docclean_v1.tflite` ditempatkan di `app/src/main/assets/`
- [ ] `NeuralVisionEngine.kt` memuat model saat aplikasi dimulai (`loadModel()` dipanggil dari `OtsoApplication`) dan dapat melaporkan status model
- [ ] Fungsi `neuralBoost()` menggunakan TFLite Interpreter sebagai primary preprocessing path, bukan heuristic thresholding
- [ ] Heuristic lama hanya boleh berjalan sebagai degraded mode jika model missing/failed, dan status kegagalan harus observable (`MODEL_MISSING`, `MODEL_FAILED`, bukan silent success)
- [ ] Waktu inference model ≤ 200ms pada perangkat mid-range (Snapdragon 6-series)
- [ ] Memory footprint tambahan model ≤ 10 MB saat loaded
- [ ] Build berhasil (assembleDebug) dan OCR berfungsi pada device
- [ ] Typecheck/lint lolos

### US-004: Dukungan Multi-Script Recognition
**Deskripsi:** Sebagai pengguna yang menulis dalam bahasa non-Latin, saya ingin OCR dapat mengenali teks dalam bahasa Mandarin, Jepang, Korea, dan Hindi/Devanagari.

**Kriteria Penerimaan:**
- [x] Menambahkan ML Kit recognizer untuk Chinese/Japanese/Korean (`com.google.mlkit:text-recognition-chinese`, `com.google.mlkit:text-recognition-japanese`, `com.google.mlkit:text-recognition-korean`)
- [x] Menambahkan ML Kit recognizer untuk Devanagari (`com.google.mlkit:text-recognition-devanagari`)
- [ ] `OcrEngine.kt` secara otomatis mendeteksi script dominan menggunakan `IntelligenceEngine.identifyLanguage()` atau script detector khusus lalu memilih recognizer yang sesuai
- [ ] Jika deteksi gagal, gunakan Latin recognizer sebagai degraded recognition path yang tercatat
- [ ] Hybrid mode menjalankan kedua recognizer (Latin + detected script) dan memilih hasil terbaik berdasarkan `qualityScore()`
- [ ] Build berhasil dan OCR berfungsi untuk teks Latin DAN non-Latin pada device
- [ ] Typecheck/lint lolos

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

- FR-1: Sistem harus memuat custom TFLite model (`otso_docclean_v1.tflite`) dari assets saat aplikasi dimulai atau saat pertama kali OCR digunakan (lazy loading)
- FR-2: Sistem harus menjadikan model TFLite sebagai primary preprocessing path; heuristic binarization hanya digunakan sebagai degraded mode yang observable jika model tidak tersedia atau inference gagal
- FR-3: Sistem harus secara otomatis mendeteksi script bahasa dan memilih ML Kit recognizer yang sesuai (Latin, Chinese, Japanese, Korean, Devanagari)
- FR-4: Sistem harus memuat aturan semantic cleanup dari file JSON (`ocr_cleanup_rules.json`) di assets
- FR-5: Sistem harus mengevaluasi kualitas hasil OCR menggunakan `qualityScore()` yang sudah ada, tetapi hasil model-first tidak boleh diam-diam dianggap berhasil jika runtime model sedang degraded
- FR-6: Waktu total OCR (preprocessing + recognition + cleanup) tidak boleh melebihi 3 detik untuk gambar beresolusi standar (12 MP) pada perangkat mid-range
- FR-7: Ukuran total model TFLite tidak boleh melebihi 5 MB untuk menjaga ukuran APK tetap ringan (SOP Goal: LIGHTWEIGHT)
- FR-8: Sistem harus menyertakan mode preprocessing khusus untuk handwriting yang menggunakan dilasi dan contrast boost agresif
- FR-9: Sistem harus menyediakan status runtime Otso Vision (`MODEL_READY`, `MODEL_MISSING`, `MODEL_FAILED`, `DEGRADED_HEURISTIC`) untuk debugging dan audit kualitas

---

## Bukan-Tujuan (Non-Goals)

- **Tidak ada perubahan UI** — Tidak ada tombol baru, dialog baru, atau perubahan visual apa pun
- **Tidak ada real-time camera OCR** — Hanya pemrosesan gambar statis (dari galeri atau scan)
- **Tidak ada cloud API** — Semua pemrosesan sepenuhnya on-device
- **Tidak ada OCR untuk video** — Hanya frame gambar tunggal
- **Tidak ada auto-correction grammar** — Engine hanya mengekstrak dan membersihkan noise, tidak memperbaiki tata bahasa
- **Tidak ada model training di dalam aplikasi** — Training dilakukan offline di environment pengembangan
- **Tidak ada klaim full in-house OCR recognizer di V2** — ML Kit tetap menjadi text recognition backend; Otso-owned model berfokus pada document cleanup/preprocessing
- **Tidak ada Arabic offline recognition di V2** — Arabic ditunda sampai backend recognition yang stabil tersedia

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
- **Input**: 256×256 RGB image (`float32`, normalized 0..1)
- **Output**: 256×256 binary mask (text vs background)
- **Quantization**: TFLite optimization untuk ukuran ≤ 5MB; INT8 full quantization menjadi target lanjutan jika latency/size membutuhkan
- **Framework**: TensorFlow 2.x → TFLite converter

### Training Environment
- Python 3.10+, TensorFlow 2.14+
- Dataset disimpan lokal di `training/` (gitignored)
- Output model disimpan di `app/src/main/assets/`

### APK Size Impact
| Komponen | Estimasi Ukuran |
|---|---|
| `otso_docclean_v1.tflite` | 1.41 MB |
| ML Kit Chinese recognizer | ~2 MB (on-demand download) |
| ML Kit Japanese recognizer | ~2 MB (on-demand download) |
| ML Kit Korean recognizer | ~2 MB (on-demand download) |
| ML Kit Devanagari recognizer | ~2 MB (on-demand download) |
| `ocr_cleanup_rules.json` | < 10 KB |

**Catatan:** ML Kit script recognizers menggunakan on-demand model download, sehingga tidak menambah ukuran APK secara langsung.

### File yang Akan Dimodifikasi
| File | Perubahan |
|---|---|
| `core/NeuralVisionEngine.kt` | Integrasi TFLite model-first path dan observable degraded mode |
| `core/OcrEngine.kt` | Multi-script routing, configurable cleanup |
| `app/build.gradle.kts` | Tambah ML Kit script dependencies |
| `assets/otso_docclean_v1.tflite` | Model file baru |
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
- **Runtime State**: ≥ 95% OCR invocation pada build release harus melewati `MODEL_READY` path ketika asset model tersedia; degraded mode harus tercatat dan tidak boleh silent

---

## Pertanyaan Terbuka

1. **Dataset tulisan tangan**: Apakah fokus pada tulisan tangan Latin saja, atau termasuk tulisan tangan aksara non-Latin?
2. **Model versioning**: Apakah perlu mekanisme untuk update model OTA (over-the-air) tanpa update APK?
3. **Benchmark device**: Selain Snapdragon 6-series, apakah ada target perangkat minimum lainnya?
4. **Arabic script**: ditunda dari V2 karena ML Kit offline recognizer belum tersedia untuk Arabic.

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
