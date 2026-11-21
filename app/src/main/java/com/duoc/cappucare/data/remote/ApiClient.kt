package com.duoc.cappucare.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /**
     * Emulador Android -> PC anfitrión: 10.0.2.2
     * Dispositivo físico: IP local del PC (ej. http://192.168.1.50:8080/), en la misma red WiFi.
     */
    const val BASE_URL = "http://localhost:8080/"

    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: CappucareApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CappucareApi::class.java)
    }
}
