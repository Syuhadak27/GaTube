package com.nontontv.indonesia.data.repository

import com.nontontv.indonesia.BuildConfig
import com.nontontv.indonesia.data.model.UpdateInfo
import com.nontontv.indonesia.data.remote.api.GitHubAssetDto
import com.nontontv.indonesia.data.remote.api.GitHubApiService
import com.nontontv.indonesia.util.Config
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mengecek pembaruan dari GitHub Release.
 *
 * Aturan penentuan versi:
 *  - versionCode dari release diambil dari body release bila memuat pola
 *    "versionCode: 123". Jika tidak ada, dihitung dari tag vX.Y.Z dengan
 *    rumus X*10000 + Y*100 + Z.
 *  - Bila nama versi (versionName) sama dengan yang terpasang, anggap sudah
 *    terbaru. Ini menghindari popup update berulang untuk versi yang sama.
 */
@Singleton
class UpdateRepository @Inject constructor(
    private val gitHubApi: GitHubApiService
) {

    /** Mengembalikan UpdateInfo bila ada versi baru, atau null bila sudah terbaru. */
    suspend fun periksaPembaruan(): UpdateInfo? {
        val release = gitHubApi.releaseTerbaru(Config.GITHUB_RELEASE_API)

        val tag = release.tagName?.trim().orEmpty()
        if (tag.isEmpty()) return null

        val versiBaru = tag.removePrefix("v").removePrefix("V")
        val versiTerpasang = BuildConfig.VERSION_NAME.trim().removePrefix("v")

        // Versi sama -> tidak ada pembaruan.
        if (versiBaru == versiTerpasang) return null

        val kodeBaru = parseVersionCode(release.body)
            ?: hitungVersionCode(versiBaru)

        if (kodeBaru <= BuildConfig.VERSION_CODE) return null

        val apk = release.assets
            ?.firstOrNull { aset -> aset.name?.endsWith(".apk", ignoreCase = true) == true }
            ?: GitHubAssetDto(name = null, browserDownloadUrl = null)

        val urlApk = apk.browserDownloadUrl.orEmpty()
        if (urlApk.isEmpty()) return null

        return UpdateInfo(
            versionName = versiBaru,
            versionCode = kodeBaru,
            changelog = release.body.orEmpty(),
            apkUrl = urlApk
        )
    }

    /** Mengambil versionCode eksplisit dari body release. */
    private fun parseVersionCode(body: String?): Int? {
        if (body.isNullOrBlank()) return null
        val pola = Regex("versionCode\\s*[:=]\\s*(\\d+)", RegexOption.IGNORE_CASE)
        return pola.find(body)?.groupValues?.get(1)?.toIntOrNull()
    }

    /** v1.2.3 -> 10203. */
    internal fun hitungVersionCode(versi: String): Int {
        val bagian = versi.split(".")
        val major = bagian.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        val minor = bagian.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        val patch = bagian.getOrNull(2)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        return major * 10_000 + minor * 100 + patch
    }
}
