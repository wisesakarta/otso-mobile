## STANDARD OPERATING PROCEDURE (SOP): ANTIGRAVITY AGENT

### I. SKILLSET & KNOWLEDGE RETRIEVAL
Sebelum mengeksekusi instruksi apa pun, Agent **wajib** melakukan prosedur berikut:

1.  [cite_start]**Directory Inspection**: Selalu periksa direktori `.agent/` di root repository untuk memetakan *skillset* yang tersedia[cite: 1].
2.  [cite_start]**Skillset Declaration**: Setiap respons awal harus menjabarkan secara eksplisit *skillset* mana dari direktori `.agent` yang akan digunakan untuk menyelesaikan tugas tersebut[cite: 1].
    * *Analyzing [.agent/X] → therefore using [Skill Y] karena [alasan].*
3.  [cite_start]**Missing Data**: Jika direktori `.agent` tidak memberikan konteks teknis yang cukup, Agent wajib memberikan *caveat* dan meminta informasi spesifik sebelum melanjutkan[cite: 1].

---

### II. CORE RULES & LOGIC GATE
1.  [cite_start]**No Assumption Policy**: Jika struktur kode, aliran data, dependensi, atau logika bisnis tidak jelas, dilarang menebak[cite: 1].
2.  [cite_start]**Read Before Write**: Selalu periksa file, log, konfigurasi, dan *stack traces* yang relevan sebelum memodifikasi kode[cite: 2].
3.  [cite_start]**Root Cause First**: Jangan memperbaiki gejala sebelum mengidentifikasi penyebab sebenarnya[cite: 3].
4.  [cite_start]**Minimal Change Policy**: Terapkan perubahan terkecil yang aman untuk menyelesaikan masalah[cite: 4].
5.  [cite_start]**Preserve Stability**: Hindari refaktor yang tidak perlu selama perbaikan bug kecuali jika diperlukan secara langsung[cite: 5].
6.  [cite_start]**Verify Before Handover**: Semua output harus lolos build, lint, tes, dan validasi runtime di lingkungan sandbox[cite: 6].

---

### III. EXECUTION FLOW
Untuk setiap tugas, Agent harus mengikuti alur berikut:

1.  [cite_start]**Skillset Mapping**: Scan direktori `.agent/` dan deklarasikan skillset yang akan digunakan[cite: 1].
2.  [cite_start]**Restate Target**: Tuliskan kembali target tugas secara singkat[cite: 15].
3.  [cite_start]**Inspect Relevant Code**: Lakukan investigasi pada kode yang bersangkutan[cite: 15].
4.  [cite_start]**Identify Root Cause**: Temukan akar permasalahan[cite: 15].
5.  [cite_start]**Propose Safest Fix**: Usulkan perbaikan paling aman[cite: 15].
6.  [cite_start]**Wait if Risk is High**: Berhenti sejenak jika risiko tinggi atau lingkup berubah[cite: 15].
7.  [cite_start]**Apply Patch**: Terapkan perbaikan pada kode[cite: 15].
8.  [cite_start]**Run Validation**: Jalankan pengujian di lingkungan sandbox[cite: 15, 18].
9.  [cite_start]**Summarize Changes**: Ringkas perubahan yang dilakukan[cite: 15].
10. [cite_start]**State Risks & Rollback**: Sebutkan risiko yang ada dan berikan langkah-langkah untuk kembali ke versi sebelumnya[cite: 15].

---

### IV. READ-ONLY INVESTIGATION MODE
[cite_start]Saat terjadi ketidakpastian, jalankan investigasi terlebih dahulu[cite: 16].

[cite_start]**Diperbolehkan[cite: 16]:**
* Membaca file dan mencari simbol.
* Menelusuri *imports* dan meninjau log.
* Memeriksa konfigurasi dan menganalisis tes.
* Memeriksa riwayat commit.

[cite_start]**Tidak Diperbolehkan[cite: 16]:**
* Mengedit atau menghapus file.
* Menginstal paket secara acak.
* Melakukan push commit.

---

### V. IMPLEMENTATION & TESTING RULES
[cite_start]**Sebelum mengubah kode[cite: 16]:**
* Periksa pola yang ada di repositori dan ikuti gaya arsitektur saat ini.
* Gunakan kembali utilitas yang tersedia dan jaga konsistensi penamaan serta pemformatan.

* **MANDATORY FULL TESTING**: Setiap kali selesai melakukan implementasi kode, Agent WAJIB melakukan full testing secara langsung pada aplikasi di device. Ini berarti melakukan kompilasi, instalasi (deploy), dan meluncurkan aplikasi (run) melalui command Gradle & ADB. Dilarang menyatakan tugas selesai sebelum memvalidasi bahwa aplikasi benar-benar dapat di-*build* dan berjalan (tidak *crash*) pada environment pengguna.
* Lakukan verifikasi apakah masalah sudah teratasi dengan menjalankan *test suite* yang relevan.
* Periksa apakah ada regresi atau efek samping yang muncul akibat perubahan tersebut.

[cite_start]**Testing Priority[cite: 17]:**
1.  Unit tests untuk logika yang berubah.
2.  Integration tests untuk modul yang terhubung.
3.  UI tests jika ada perubahan pada sisi pengguna.
4.  Smoke test untuk startup aplikasi.
5.  Minimal targeted tests jika belum ada tes sebelumnya.

---

### VI. RISK CONTROL & ESCALATION
[cite_start]Berhenti dan lakukan eskalasi jika tugas mencakup hal-hal berikut[cite: 18]:
* Logika autentikasi atau sistem pembayaran.
* Migrasi database atau skrip destruktif.
* Perubahan konfigurasi produksi atau izin keamanan.
* Refaktor massal atau efek samping yang tidak diketahui.

---

### VII. OUTPUT FORMAT
[cite_start]Setiap respons akhir harus menyertakan[cite: 19]:

* **A. Objective**
* **B. Skillset Used** (Berdasarkan poin I)
* **C. Findings**
* **D. Root Cause**
* **E. Fix Plan**
* **F. Files Changed**
* **G. Validation Result**
* **H. Risks & Rollback**
* **I. Next Recommendation**

---

[cite_start]**DEFAULT PRIORITY**: Correctness > Safety > Stability > Speed > Elegance[cite: 20].

[cite_start]**FINAL RULE**: Jangan melakukan optimasi kode yang bukan bagian dari masalah yang diminta, kecuali jika kode tersebut menghalangi solusi yang diperlukan[cite: 20].

---

### VIII. OTSO PRODUCT GOALS (MANDATORY — NON-NEGOTIABLE)

Setiap keputusan engineering, desain, dan arsitektur **WAJIB** mengacu pada empat tujuan berikut. Ini adalah north star yang tidak bisa dikompromikan:

1.  **SMOOTHLESS** — Otso/Kontio harus terasa mulus tanpa jitter, lag, atau frame drop. Target: 60/120 FPS konsisten.
2.  **LIGHTWEIGHT** — Tidak boleh ada bloat, slop, atau fitur yang menambah bobot tanpa nilai. Setiap baris kode harus membenarkan keberadaannya.
3.  **RED-DOT PRODUCT DESIGN AWARD STANDARD** — Setiap komponen UI harus memenuhi standar desain produk internasional: aksesibilitas, visual hierarchy, progressive disclosure, micro-animations, dan premium material treatment.
4.  **USEABLE** — Fitur harus berfungsi dengan benar dan dapat digunakan oleh pengguna nyata dalam kondisi nyata. Tidak ada dead-end, tidak ada fitur setengah jadi.

---

### IX. BRANCH RULES (MANDATORY)

Agent **DILARANG KERAS** bekerja langsung pada branch `main`.

**Konvensi branch yang wajib digunakan:**
- `fix/<issue-name>` — Untuk perbaikan bug
- `hotfix/<issue-name>` — Untuk perbaikan kritis yang mendesak
- `feature/<feature-name>` — Untuk fitur baru
- `refactor/<scope-name>` — Untuk refaktor kode

Jika branch belum ada, **buat terlebih dahulu** sebelum melakukan perubahan apa pun.

---

### X. REPOSITORY IDENTITY RULES (MANDATORY)

Saat Agent melakukan `git add` / `commit` / `push`:

1.  **Gunakan kredensial pemilik repositori** yang sudah ter-autentikasi di environment.
2.  **Gunakan konteks identitas repositori** yang sudah ada.
3.  **DILARANG** menggunakan identitas buatan agent.
4.  **DILARANG** mengatur `git user.name` atau `user.email` baru kecuali diminta secara eksplisit.
5.  **DILARANG** mengganti SSH keys, tokens, signing keys, atau remote URLs.
6.  **DILARANG** mengubah credential helpers.
7.  **DILARANG** mengekspos secrets di logs atau output.
8.  Agent harus **selektif** dan hanya memilih file esensial saat melakukan `git add` / `commit` / `push`.

**Jika konteks pemilik yang ter-autentikasi tidak tersedia:**
Hentikan operasi tulis dan minta akses repositori yang tepat.

---

### XI. MERGE-TO-MAIN RULE (MANDATORY)

Setelah semua pekerjaan selesai dan tervalidasi, Agent **WAJIB** melakukan merge branch kerja ke `main` dan kembali ke branch `main`. Tidak ada pekerjaan yang boleh ditinggalkan di branch terpisah tanpa di-merge.

**Alur wajib:**
1.  Buat branch kerja sesuai konvensi (Section IX).
2.  Lakukan semua perubahan dan commit di branch kerja.
3.  Validasi melalui full testing pada device (Section V).
4.  Merge branch kerja ke `main`.
5.  Kembali ke branch `main` sebagai posisi akhir.