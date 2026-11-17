# Changelog GaTube

Semua perubahan penting pada GaTube dicatat di file ini.
Format mengikuti [Keep a Changelog](https://keepachangelog.com/id/).

## [1.0.0] - 2026-11-17

### Ditambahkan
- Pemutar YouTube tanpa iklan (pre-roll, mid-roll, banner, overlay) memakai
  Media3/ExoPlayer + NewPipeExtractor.
- Picture-in-Picture (PiP) otomatis saat aplikasi di-minimize dan tetap jalan
  saat layar mati.
- Background play dengan notifikasi media (Putar/Jeda, Berikutnya, Sebelumnya,
  Tutup).
- Beranda ala YouTube: Trending, kategori, pencarian, detail video, komentar,
  kanal, playlist, riwayat, dan favorit.
- Pilihan kualitas video 144p hingga 4K (bila tersedia) dan kecepatan putar
  0.25x - 2x.
- Login ala Google TV dengan kode 8 karakter (format ABCD-1234) yang
  disimpan sepenuhnya lokal, plus mode tamu.
- Sesi login tersimpan di DataStore terenkripsi (AES-256 GCM, AndroidKeyStore).
- Pengecekan pembaruan otomatis dari GitHub Release dengan dialog versi baru,
  unduh APK, dan pembukaan installer.
- Build release otomatis memakai GitHub Actions saat push tag `v*`.
