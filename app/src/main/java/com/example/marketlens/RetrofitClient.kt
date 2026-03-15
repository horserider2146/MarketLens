package com.example.marketlens

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private val API_KEY get() = BuildConfig.COINGECKO_API_KEY

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("x-cg-demo-api-key", API_KEY)
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }
}