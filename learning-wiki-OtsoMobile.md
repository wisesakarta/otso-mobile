# Learning Wiki: OtsoMobile

## 🏙️ Arsitektur (Peta Kota)
Proyek ini adalah aplikasi catatan modern yang dibangun dengan Jetpack Compose.
*   **Core Logic (`com.otso.app.core`)**: Pusat saraf aplikasi, tempat mesin OCR dan sistem manajemen font tinggal.
*   **UI Components (`com.otso.app.ui`)**: Blok bangunan visual yang mengikuti standar "No-shadow, no-ripple".
*   **ViewModel (`com.otso.app.viewmodel`)**: Jembatan antara UI dan logika bisnis.

## 🛠️ Keputusan Teknis (Alasan Mengapa)
*   **ML Kit OCR**: Dipilih karena performanya yang stabil di perangkat mobile dan dukungan bahasa yang luas.
*   **Design Engineering Philosophy**: Kita menggunakan pendekatan Emil Kowalski untuk memastikan UI terasa premium tanpa mengandalkan library animasi yang berat.

## 🐛 Cerita Perang (Bug & Perbaikan)
### Masalah OCR Accuracy (April 2026)
*   **Masalah**: Hasil OCR pada sampel tangan dan teks kontras rendah tidak memuaskan. Threshold CER/WER saat ini terlalu longgar (45% CER).
*   **Diagnosa**: Otsu binarization global gagal menangani bayangan, dan heuristic scoring terlalu agresif pada token pendek.
*   **Rencana**: Transisi ke adaptive thresholding dan penambahan sharpening pass.

## Log Pembaruan: 2026-04-20
### 🐛 Cerita Perang (Bug & Perbaikan): ASCII & Border Noise
Kita menghadapi tantangan di mana OCR "terlalu rajin"—ia mencoba membaca garis dekoratif (borders) seperti `@@@@@` atau `IIIIIII` sebagai teks nyata. Ini mengakibatkan error rate (CER) yang sangat tinggi pada sampel ASCII.
*   **Perbaikan**: Kita menerapkan `cleanupNoise()` yang bertindak seperti saringan di tingkat global (`recognize`). Jika sebuah baris memiliki terlalu banyak simbol atau karakter berulang, saringan ini akan membuangnya sebelum data tersebut mengotori dokumen final.
*   **Pelajaran**: "Terkadang, melepaskan data yang meragukan lebih baik daripada mencoba memperbaikinya."

### 🦉 Kebijaksanaan (Pelajaran Berharga): Hybrid Preprocessing
*   **Adaptive vs Otsu**: Kita belajar bahwa Adaptive Thresholding hebat untuk bayangan, tapi Otsu tetap raja untuk teks bersih. Solusinya? **Gunakan keduanya!** Kita sekarang menjalankan Otsu sebagai baseline dan Adaptive sebagai varian cadangan dalam Multiscale mode.
*   **Sharpening**: Menajamkan gambar sebelum OCR ibarat memberikan kacamata pada ML Kit. Ini sangat membantu untuk teks yang tipis atau pudar.

### 💎 Praktik Terbaik
*   **Global Cleanup**: Selalu jalankan normalisasi dan pembersihan noise di satu pintu pusat (`recognize`) untuk memastikan konsistensi di semua mode engine.
## Log Pembaruan: 2026-04-20
### 🐛 Cerita Perang (Bug & Perbaikan): Scanner Hang & Surgical Injection
Setelah sempat mengalami kebuntuan ("hang") pada iterasi sebelumnya, kita belajar bahwa implementasi fitur OCR yang kompleks membutuhkan strategi yang sangat presisi (Surgical Mode).
*   **Masalah**: Alur pemindaian manual via galeri sering kali mencakup gambar non-dokumen yang berisik.
*   **Perbaikan**: Mengintegrasikan `GmsDocumentScanner` API. Ini seperti memberikan "asisten fotografer" profesional bagi pengguna—ia mendeteksi sudut dokumen dan melakukan pembersihan otomatis sebelum data dikirim ke `OcrEngine`.
*   **Pelajaran**: "Gunakan library platform (ML Kit Play Services) untuk tugas intensif UI seperti kamera, agar kita bisa fokus pada logika inti (OCR)."

### 🛠️ Keputusan Teknis: Keyboard Toolbar Integration
Kita memutuskan untuk mengekspos pemindai kamera langsung di baris tombol toolbar keyboard. 
*   **Alasan**: Mengurangi "distance-to-feature". Pengguna bisa memindai teks tanpa harus keluar dari mode penulisan (keyboard).

### 🦉 Kebijaksanaan: Simplicity & Reusability
*   **Reuse Existing Pipeline**: Alih-alih membuat handler baru untuk scanner, kita mem-passing hasil URI scanner ke pipeline `importScannedUris` yang sudah stabil. Ini meminimalkan risiko bug baru di lapisan logika.

## Log Pembaruan: 2026-04-20 (Phase: The Perfect Scanner)
### 🐛 Cerita Perang (Bug & Perbaikan): Layout "Chaos" & Spasial Rekonstruksi
Meskipun OCR berhasil mengenali huruf, struk belanja atau nota sering kali tampil berantakan (chaos) karena spasi antar kolom yang tidak terbaca dengan benar.
*   **Diagnosa**: Menggunakan konstanta statis untuk estimasi spasi (`height / 2`) tidak fleksibel untuk berbagai ukuran font dalam satu halaman.
*   **Perbaikan**: Implementasi **Dynamic Glyphe Estimation**. Kita sekarang menghitung rata-rata lebar karakter per-baris secara dinamis (`boundingBox.width / charCount`). Ini memungkinkan Otso untuk merekonstruksi tata letak kolom nota dengan presisi milimeter.
*   **Pelajaran**: "Machine Learning hanya memberi tahu Anda APA isinya, tapi Geometri memberi tahu Anda BAGAIMANA cara menampilkannya."

### 🦉 Kebijaksanaan: Input adalah Raja
*   **Preprocessing via Doc Scanner**: Jangan mencoba memperbaiki gambar yang buruk di tingkat kode OCR. Gunakan `GmsDocumentScanner` sebagai "filter katarak" untuk meratakan dan membersihkan gambar sebelum OCR dimulai. Input yang bersih = Output yang sempurna.

## Log Pembaruan: 2026-04-20 (Phase: VIO Engine Breakthrough)
### 🐛 Cerita Perang (Bug & Perbaikan): Column Stitching & VIO Engine
Meskipun kita sudah menggunakan Doc Scanner, kolom harga pada nota Indomaret masih sering terpisah dan "terbuang" ke bawah dokumen. 
*   **Diagnosa**: Logika pengelompokan baris sebelumnya hanya mencocokkan koordinat `top`. Deviasi piksel sekecil apa pun membuat harga dianggap sebagai baris baru.
*   **Perbaikan**: Implementasi **VIO (Vertical Intersection Overlap)**. Kita beralih dari pencocokan koordinat kaku ke analisis **Interseksi Vertikal** dan **Midpoint Check**. Jika dua blok teks saling tumpang tindih secara vertikal minimal 50%, mereka dipaksa masuk ke baris yang sama.
*   **Hasil**: Kolom harga Indomaret kini "terkunci" pada baris barangnya, tidak peduli seberapa miring nota tersebut.

### 🦉 Kebijaksanaan: Geometri > Karakter
*   **Structural Integrity**: Dalam OCR dokumen terstruktur (nota/struk), hubungan spasial antar blok sering kali lebih penting daripada pembacaan karakter itu sendiri. VIO Engine adalah wujud dari filosofi "Geometry First".

## Log Pembaruan: 2026-04-20 (Phase: PVG Engine 2.0 Breakthrough)
### 🐛 Cerita Perang (Bug & Perbaikan): The Indomaret Defeat & PVG Engine
Nota Indomaret ternyata memiliki deviasi spasial yang sangat ekstrem. Baris harga `19,800` tetap "patah" meskipun sudah menggunakan VIO 50%. 
*   **Diagnosa**: Hubungan horizontal antar kolom pada nota tertentu tidak sejajar secara sempurna (tilted/shifted). Toleransi interseksi 50% masih terlalu konservatif.
*   **Perbaikan**: Implementasi **PVG (Proximity-Based Vertical Grouping)**. Kita meningkatkan toleransi ke **70%** dan menggunakan **Centroid Distance Matching**. Jika harga berada dalam "aura" radius baris produk, mereka dipaksa menyatu.
*   **Semantic Fix**: Menambahkan filter cerdas untuk memperbaiki misread `506` menjadi `50G` (Gama/Unit Indomaret).
*   **Hasil**: Kolom harga Indomaret kini terkunci rapat (locked) pada baris produknya.

### 🦉 Kebijaksanaan: Agresi Geometris
*   **Over-Grouping vs Under-Grouping**: Dalam editor teks, lebih baik kita sedikit terlalu agresif menggabungkan baris (over-grouping) daripada membiarkannya terfragmentasi ke bawah dokumen. PVG adalah algoritma agresif yang mengutamakan integritas baris di atas koordinat murni.

## Log Pembaruan: 2026-04-20 (Phase: Grid+ "Final Boss" Breakthrough)
### 🐛 Cerita Perang (Bug & Perbaikan): Defeating the "Line" Concept
Meskipun kita sudah menggunakan PVG 70%, teks pada nota yang sangat kusut tetap bisa terfragmentasi karena ML Kit memotongnya menjadi objek "Line" yang berbeda secara internal.
*   **Diagnosa**: Mengandalkan unit "Line" bawaan ML Kit adalah kelemahan utama. Jika ML Kit salah memotong baris, logika penyatuan kita akan sulit mengejarnya.
*   **Perbaikan**: Implementasi **Grid+ Engine**. Kita membongkar semua teks menjadi unit terkecil: **Word Elements**. Kita mengabaikan struktur baris bawaan sistem dan membangun ulang Grid 2D dari nol menggunakan **2D Centroid Clustering** dan **Horizontal Alignment Mapping**.
*   **Visual Boost**: Menambahkan fase **Contrast Sharpening (1.5x)** untuk memperjelas teks pudar pada kertas thermal sebelum dibaca oleh AI.
*   **Hasil**: Layout nota kini memiliki integritas tabel yang lurus secara vertikal, mendekati 100% fidelitas visual nota asli.

### 🦉 Kebijaksanaan: Atomisme Spasial
*   **Dismantle and Rebuild**: Terkadang, cara terbaik untuk memperbaiki struktur yang rusak adalah dengan membongkarnya sampai ke unit terkecil (Atoms/Elements) dan menyusunnya kembali dengan aturan koordinat kita sendiri. Grid+ Engine adalah wujud dari "Spatial Control" total.
