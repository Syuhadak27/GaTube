package com.nontontv.indonesia.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Url

/** Data release GitHub yang dibutuhkan untuk cek pembaruan. */
data class GitHubReleaseDto(
    @SerializedName("tag_name") val tagName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("html_url") val htmlUrl: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("assets") val assets: List<GitHubAssetDto>? = null
)

data class GitHubAssetDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("browser_download_url") val browserDownloadUrl: String? = null
)

/**
 * API GitHub untuk release terbaru.
 * URL diambil dari Config.GITHUB_RELEASE_API supaya mudah diganti.
 */
interface GitHubApiService {

    @GET
    suspend fun releaseTerbaru(@Url url: String): GitHubReleaseDto
}
