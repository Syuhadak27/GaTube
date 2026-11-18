---
description: Update semua ikon dari icon.png via update-icon.sh lalu build APK di Termux
---

# Eksekusi Build APK + Ganti Ikon (Termux)

1. Jalankan `./update-icon.sh` untuk menimpa seluruh `ic_launcher`, *adaptive foreground*, dan ikon UI dari `icon.png`.
2. Periksa `AndroidManifest.xml` dan `mipmap-anydpi-v26/ic_launcher.xml` — jika proyek menggunakan vector XML (`@drawable/ic_launcher_foreground`) yang belum tertimpa gambar raster, perbarui referensi foreground-nya agar menampilkan `icon.png` secara utuh.
3. Pastikan `gradle.properties` sudah menyertakan override `android.aapt2FromMavenOverride` yang mengarah ke binary `aapt2` ARM64 Termux (cek via `which aapt2` jika belum ada).
4. Jalankan `chmod +x ./gradlew && ./gradlew clean assembleDebug`.
5. Setelah sukses, salin APK hasil build (`app/build/outputs/apk/debug/app-debug.apk`) ke `/sdcard/Download/` dengan nama sesuai nama proyek dan tampilkan ringkasan hasilnya.
