#!/usr/bin/env bash
#
# generate-keystore.sh
# Membuat ulang keystore release GaTube jika file-nya hilang.
# Password sengaja sama dengan yang ada di keystore.properties (proyek pribadi).
#
set -euo pipefail

cd "$(dirname "$0")"

mkdir -p keystore

echo "Membuat keystore/release.keystore ..."
keytool -genkeypair -v \
  -keystore keystore/release.keystore \
  -storetype PKCS12 \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias appkey \
  -storepass 12345678 \
  -keypass 12345678 \
  -dname "CN=GaTube, OU=Pribadi, O=GaTube, L=Jakarta, ST=DKI Jakarta, C=ID"

echo "Selesai. Keystore siap dipakai untuk build release."
