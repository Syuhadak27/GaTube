import os


def find_files_with_extensions(root_folder, extensions):
    """Mencari file dengan beberapa ekstensi di seluruh folder"""
    files_with_extension = []

    for root, dirs, files in os.walk(root_folder):
        for file in files:
            if file.lower().endswith(tuple(extensions)):
                files_with_extension.append(
                    os.path.join(root, file)
                )

    return files_with_extension


def search_words_in_files(files, keywords):
    """Mencari kata dalam daftar file dan mencetak hasil"""

    line_color = '\033[92m'   # Hijau untuk nomor baris
    file_color = '\033[94m'   # Biru untuk nama file
    reset_color = '\033[0m'   # Reset warna

    # Pisahkan kata berdasarkan *
    keywords = [
        keyword.strip()
        for keyword in keywords.split('*')
        if keyword.strip()
    ]

    found = False

    for name in files:

        try:
            with open(
                name,
                mode='r',
                encoding='utf-8',
                errors='ignore'
            ) as file:
                data = file.readlines()

        except Exception as e:
            print(f"Gagal membaca {name}: {e}")
            continue

        for i, line in enumerate(data):

            # Semua keyword harus ada dalam satu baris
            if all(keyword in line for keyword in keywords):

                print(
                    f'{line_color}Line {i + 1}{reset_color} '
                    f'in {file_color}{name}{reset_color}: '
                    f'{line.strip()}'
                )

                found = True

    if not found:
        print("Tidak ditemukan.")


# ==================================================
# FOLDER ROOT
# ==================================================

# Direktori tempat script dijalankan
root_folder = os.getcwd()


# ==================================================
# EXTENSION FILE YANG AKAN DICARI
# ==================================================

extensions = [
    '.py',
    '.js',
    '.html',
    '.css',
    '.json',
    '.kt'
]


# ==================================================
# CARI SEMUA FILE
# ==================================================

marge_file_name = find_files_with_extensions(
    root_folder,
    extensions
)


print("=" * 50)
print("FILE YANG AKAN DICARI:")
print("=" * 50)

for file in marge_file_name:
    print(file)

print("=" * 50)
print(f"Total file: {len(marge_file_name)}")
print("=" * 50)


# ==================================================
# SEARCH
# ==================================================

while True:

    user_input = input(
        "\nMasukkan kata kunci "
        "(Gunakan '*' untuk memisahkan kata "
        "atau 'q' untuk keluar): "
    )

    if user_input.lower() == 'q':
        print("Keluar.")
        break

    search_words_in_files(
        marge_file_name,
        user_input
    )