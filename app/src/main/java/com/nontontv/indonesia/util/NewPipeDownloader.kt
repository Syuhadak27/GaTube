package com.nontontv.indonesia.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Implementasi Downloader NewPipeExtractor di atas OkHttp.
 * Dipakai supaya NewPipeExtractor bisa mengambil halaman YouTube.
 */
object NewPipeDownloader : Downloader() {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun execute(request: Request): Response {
        val okhttpRequestBuilder = okhttp3.Request.Builder().url(request.url())

        // Salin semua header yang diminta NewPipeExtractor.
        request.headers().forEach { (nama, daftarNilai) ->
            daftarNilai.forEach { nilai -> okhttpRequestBuilder.addHeader(nama, nilai) }
        }

        val metode = request.httpMethod()?.uppercase(Locale.US) ?: "GET"
        val data = request.dataToSend()
        when (metode) {
            "HEAD" -> okhttpRequestBuilder.head()
            "POST" -> {
                val tipeKonten = request.headers()["Content-Type"]?.firstOrNull()
                    ?: "application/json; charset=utf-8"
                okhttpRequestBuilder.post(
                    (data ?: ByteArray(0)).toRequestBody(tipeKonten.toMediaType())
                )
            }
            "PUT" -> {
                val tipeKonten = request.headers()["Content-Type"]?.firstOrNull()
                    ?: "application/json; charset=utf-8"
                okhttpRequestBuilder.put(
                    (data ?: ByteArray(0)).toRequestBody(tipeKonten.toMediaType())
                )
            }
            else -> okhttpRequestBuilder.get()
        }

        val panggilan = okHttpClient.newCall(okhttpRequestBuilder.build())
        val respons = panggilan.execute()

        val isiRespons = respons.body?.string().orEmpty()
        val headerRespons = mutableMapOf<String, MutableList<String>>()
        respons.headers.forEach { (nama, nilai) ->
            headerRespons.getOrPut(nama) { mutableListOf() }.add(nilai)
        }

        return Response(
            respons.code,
            respons.message,
            headerRespons,
            isiRespons,
            respons.request.url.toString()
        )
    }
}
