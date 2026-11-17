package com.nontontv.indonesia.util

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mengunduh APK pembaruan dan membuka installer Android.
 * Unduhan disimpan di folder eksternal aplikasi lalu dibagikan lewat FileProvider.
 */
@Singleton
class UpdateDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {

    /** Mengunduh APK dengan melaporkan progres 0..100. */
    suspend fun unduhApk(
        url: String,
        onProgress: suspend (Int) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val folder = File(context.getExternalFilesDir("updates"), "").apply { mkdirs() }
        val file = File(folder, "GaTube-update.apk")
        if (file.exists()) file.delete()

        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Gagal mengunduh pembaruan (kode ${response.code})")
            }
            val body = response.body ?: throw IOException("File pembaruan kosong")
            val total = body.contentLength()
            var terunduh = 0L

            file.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val dibaca = input.read(buffer)
                        if (dibaca == -1) break
                        output.write(buffer, 0, dibaca)
                        terunduh += dibaca
                        if (total > 0) {
                            onProgress(((terunduh * 100) / total).toInt().coerceIn(0, 100))
                        }
                    }
                }
            }
        }
        file
    }

    /** Membuka installer Android untuk APK yang sudah diunduh. */
    fun bukaInstaller(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** Mengecek izin "Install unknown apps". */
    fun bisaInstallAplikasiTakDikenal(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /** Membuka halaman pengaturan izin install untuk aplikasi ini. */
    fun bukaPengaturanInstall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }
}
