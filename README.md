# GaTube

Aplikasi Android pemutar YouTube **tanpa iklan** (pre-roll, mid-roll, banner, dan
overlay), dengan **Picture-in-Picture (PiP)** otomatis, **background play**,
serta **login ala Google TV** memakai kode perangkat.

- Bahasa: **Kotlin 100% Jetpack Compose + Material 3**
- Arsitektur: **MVVM + Clean Architecture**
- Dependency Injection: **Hilt**
- Jaringan: **Retrofit + OkHttp**
- Database lokal: **Room**
- Pemutar: **Media3 / ExoPlayer + MediaSession**
- Extractor: **NewPipeExtractor** (dari JitPack, legal, tanpa iklan)
- Min SDK 24 • Target SDK 34
- Package: `com.nontontv.indonesia`

---

## 1. Fitur Utama

| Fitur | Keterangan |
|---|---|
| Tanpa iklan | Hanya memutar URL stream langsung, iklan YouTube tidak pernah dimuat. |
| PiP otomatis | Masuk Picture-in-Picture saat tombol Home ditekan; tetap jalan saat layar mati. |
| Background play | Audio lanjut saat aplikasi di-minimize / layar terkunci, dengan notifikasi media (Putar/Jeda, Berikutnya, Sebelumnya, Tutup). |
| Beranda | Trending, kategori (Musik, Game, Film, Podcast, Live), pencarian + saran, detail video, komentar, kanal, playlist. |
| Pustaka | Riwayat tontonan dan favorit (lokal, tetap ada saat mode tamu). |
| Kualitas | 144p, 240p, 360p, 480p, 720p, 1080p, 1440p, 4K (bila tersedia). |
| Kecepatan | 0.25x, 0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x, 2x. |
| Login Google TV | Kode 8 karakter format `ABCD-1234` (disimpan lokal), atau Mode Tamu. |
| Sesi aman | Token disimpan di DataStore dengan enkripsi AES-256 GCM (AndroidKeyStore). |
| Cek pembaruan | Otomatis cek GitHub Release saat aplikasi dibuka, dialog versi baru, unduh APK, buka installer. |
| Rilis otomatis | GitHub Actions membuild APK saja dan membuat Release saat push tag `v*`. |

---

## 2. Struktur Proyek

```
GaTube/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nontontv/indonesia/
│       │   ├── GaTubeApp.kt
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── local/        (Room, DataStore, SessionManager)
│       │   │   ├── model/        (VideoItem, VideoDetail, dst.)
│       │   │   ├── remote/       (YouTubeRepository, API GitHub)
│       │   │   └── repository/   (Library, Account, Update)
│       │   ├── di/               (Hilt: NetworkModule, DatabaseModule)
│       │   ├── playback/         (PlaybackController)
│       │   ├── service/          (PlaybackService: MediaSessionService)
│       │   ├── ui/
│       │   │   ├── components/   (KartuVideo, dialog, status)
│       │   │   ├── detail/ home/ search/ player/ channel/ playlist/
│       │   │   ├── library/ settings/ update/ login/ navigasi/ theme/
│       │   └── util/             (Config.kt, FormatUtils, UpdateDownloader, NewPipeDownloader)
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/…
├── gradlew  •  gradlew.bat
├── keystore/
│   └── release.keystore
├── keystore.properties
├── generate-keystore.sh
├── .github/workflows/
│   ├── build-apk.yml       (build APK only -> artifact)
│   └── build-release.yml   (build APK only + GitHub Release)
├── .gitignore
├── CHANGELOG.md
└── README.md
```

---

## 3. Persyaratan

1. **Android Studio** Foxfire/Barbet atau lebih baru (disarankan Hedgehog+).
2. **JDK 17**.
3. **Android SDK**: API 34 (compileSdk), build-tools 34+.
4. Akun **GitHub** (untuk fitur cek pembaruan & rilis otomatis).
5. Tidak perlu server/backend: kode pairing dan sesi login disimpan lokal.

Buka project di Android Studio, biarkan Gradle sync selesai, lalu jalankan.

---

## 4. Langkah 1 — Ganti USERNAME/REPO GitHub di `Config.kt`

File: `app/src/main/java/com/nontontv/indonesia/util/Config.kt`

```kotlin
object Config {
    const val GITHUB_USERNAME = "Syuhadak27"   // <-- GANTI bila repo berubah
    const val GITHUB_REPO = "GaTube"           // <-- SESUAIKAN
}
```

- `GITHUB_USERNAME` dan `GITHUB_REPO` dipakai untuk:
  `https://api.github.com/repos/Syuhadak27/GaTube/releases/latest`
- Selalu buat **GitHub Release publik** agar API bisa diakses tanpa token.
- Tidak ada konfigurasi Supabase: semua kode pairing dan sesi tersimpan lokal.

---

## 5. Langkah 2 — Login dengan Kode (Lokal)

Tidak ada Supabase. Kode dan sesi login hanya disimpan di perangkat ini
(memakai DataStore terenkripsi).

Alur:

1. Di layar pertama, pilih **Masuk dengan Kode**. Muncul kode 8 karakter,
   contoh `ABCD-1234`, yang berlaku 10 menit.
2. Buka **Pengaturan > Tautkan Perangkat Baru**, masukkan kode tersebut, lalu
   tekan **Tautkan Sekarang**.
3. Layar pairing otomatis mendeteksi persetujuan dan aplikasi masuk.
4. Token sesi disimpan secara lokal (enkripsi AES-256 GCM).
5. Tombol **Keluar** di Pengaturan menghapus sesi lokal tersebut.

Mode tamu juga tersedia: tanpa login tetap bisa menonton, dan riwayat/favorit
tersimpan lokal.

---

## 6. Langkah 3 — Push Pertama Termasuk Keystore

Keystore dan `keystore.properties` **sengaja di-push** karena ini proyek pribadi.

```bash
cd GaTube

git init
git add .
git commit -m "Rilis awal GaTube v1.0.0"
git branch -M main
git remote add origin git@github.com:Syuhadak27/GaTube.git
git push -u origin main
```

Pastikan file ini ikut ter-push:

```bash
git ls-files | grep -E "keystore"
# harus memunculkan:
# keystore.properties
# keystore/release.keystore
```

Bila `gradlew` belum boleh eksekusi:

```bash
chmod +x gradlew
git update-index --chmod=+x gradlew
git commit -am "Perbaiki permission gradlew"
git push
```

### Keystore

| Item | Nilai |
|---|---|
| File | `keystore/release.keystore` |
| Store password | `12345678` |
| Key alias | `appkey` |
| Key password | `12345678` |

Jika keystore hilang, buat ulang:

```bash
./generate-keystore.sh
```

> ⚠️ **PERINGATAN** — Jangan meneladani untuk proyek publik/open source.
> Password hardcoded di repo hanya untuk kemudahan proyek pribadi.
> Untuk proyek publik, gunakan GitHub Secrets / environment variable dan
> jangan pernah mempush keystore.

---

## 7. Langkah 4 — Rilis Versi Baru Cukup dengan Push Tag

Pastikan `versionCode` dan `versionName` di `app/build.gradle.kts` sudah
dinaikkan, lalu:

```bash
# 1) Naikkan versionCode & versionName di app/build.gradle.kts
#    contoh: versionCode = 13, versionName = "1.0.1"

# 2) Commit perubahan
git add app/build.gradle.kts
git commit -m "Naikkan versi ke 1.0.1 (13)"

# 3) Buat tag dan push
git tag v1.0.1
git push origin main
git push origin v1.0.1
```

GitHub Actions (`.github/workflows/build-release.yml`) akan:

1. Membuild APK release saja (sudah ditandatangani memakai keystore di repo,
   tanpa AAB).
2. Membuat GitHub Release bernama `GaTube v1.0.1`.
3. Mengisi changelog dari daftar commit antara tag sebelumnya dan tag ini.
4. Melampirkan satu asset APK, misalnya `GaTube-v1.0.1.apk`.

Cek hasilnya di tab **Actions** lalu tab **Releases**. Workflow ini juga bisa
dijalankan manual lewat **Actions > Build APK & Release GaTube > Run workflow**
dengan mengisi field `tag`.

Untuk hanya membuild APK tanpa membuat Release, jalankan
**Actions > Build APK Only > Run workflow**, atau biarkan workflow
`.github/workflows/build-apk.yml` berjalan otomatis saat push/PR ke `main`.
APK bisa diunduh dari artifact pada halaman run tersebut.

### Aturan penentuan “versi baru”

- `versionCode` diambil dari **body release** bila memuat pola
  `versionCode: 13`. Jika tidak ada, dihitung dari tag `vX.Y.Z`:
  `X*10000 + Y*100 + Z`.
- Bila `versionName` sama dengan yang terpasang, aplikasi menganggap sudah
  terbaru (menghindari popup berulang).
- Contoh: terpasang `v1.0.0 (12)`, rilis baru `v1.0.1` → muncul popup.

---

## 8. Langkah 5 — Test Fitur Cek Update & Pop-up Versi Baru

### Uji otomatis saat aplikasi dibuka

1. Pastikan `GITHUB_USERNAME`/`GITHUB_REPO` di `Config.kt` sudah benar.
2. Buka halaman **Releases** di repo GitHub, buat release baru (misal `v1.0.1`)
   dengan melampirkan file APK apa pun (bisa `app-release.apk`).
3. Jalankan aplikasi (versi lama). Saat aplikasi terbuka, aplikasi otomatis
   memanggil `releases/latest`.
4. Muncul dialog:

   - Judul: **Versi Baru Tersedia!**
   - Isi: “Versi v1.0.1 sudah tersedia. Silakan perbarui untuk fitur terbaru
     dan perbaikan bug.”
   - Changelog dari body release.
   - Tombol: **Perbarui Sekarang** dan **Nanti**.

5. Klik **Perbarui Sekarang**:
   - Aplikasi mengecek izin *Install unknown apps*.
   - Jika belum ada, tombil berubah jadi **Buka Pengaturan Izin** dan membuka
     pengaturan Android. Aktifkan izin, kembali, lalu klik lagi.
   - APK diunduh dengan progress, lalu installer Android terbuka otomatis
     (`ACTION_VIEW` + FileProvider).

### Uji manual

- Buka **Pengaturan > Tentang Aplikasi > Cek Pembaruan**.
- Bila ada update: dialog seperti di atas.
- Bila tidak ada: muncul pesan **“Kamu sudah menggunakan versi terbaru.”**
- Di layar yang sama tampil **Versi Terpasang**, contoh: `v1.0.0 (12)`.

### Catatan izin

`REQUEST_INSTALL_PACKAGES` sudah terpasang di `AndroidManifest.xml`. Di
Android 8+, pengguna harus mengizinkan “Instal aplikasi tak dikenal” sekali
untuk GaTube.

---

## 9. Build Lokal

```bash
# Debug
./gradlew assembleDebug

# Release (dipakai keystore di repo) — APK only
./gradlew assembleRelease
```

Output:

- APK debug: `app/build/outputs/apk/debug/app-debug.apk`
- APK release: `app/build/outputs/apk/release/app-release.apk`

CI tidak membangun AAB. Bila ingin AAB untuk Play Store, jalankan manual
`./gradlew bundleRelease` di lokal.

---

## 10. Troubleshooting

| Masalah | Solusi |
|---|---|
| `NewPipeExtractor` gagal dimuat / video error | YouTube sering berubah; naikkan versi di `app/build.gradle.kts` ke rilis terbaru di JitPack. |
| PiP tidak muncul | Pastikan `android:supportsPictureInPicture="true"` dan `configChanges` sudah ada (sudah diset). |
| Notifikasi media tidak muncul | Izinkan notifikasi (Android 13+). |
| Login kode gagal | Pastikan kode masih berlaku (maksimal 10 menit) dan sudah dibuat di layar *Masuk dengan Kode*. |
| Cek pembaruan 404 | Pastikan repo **publik** dan sudah punya release. |
| Build release gagal karena keystore | Jalankan `./generate-keystore.sh`, lalu build ulang. |
| Gradle wrapper permission denied | `chmod +x gradlew` atau jalankan `bash gradlew ...`. |

---

## 11. Lisensi & Disclaimer

GaTube dibuat untuk keperluan belajar dan penggunaan pribadi. Project ini
**bukan** afiliasi YouTube/Google. Konten YouTube tetap tunduk pada ketentuan
YouTube. NewPipeExtractor bersifat open source (GPL-3.0).
