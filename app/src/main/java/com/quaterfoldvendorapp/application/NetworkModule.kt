package com.quaterfoldvendorapp.application

import com.quaterfoldvendorapp.interfaces.ApiRepository
import com.quaterfoldvendorapp.interfaces.ApiService
import com.quaterfoldvendorapp.domain.ApiUseCase
import com.quaterfoldvendorapp.utils.AppConstant
import com.squareup.moshi.Moshi
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val NetworkModule = module {

    single { createService(get()) }

    single { createRetrofit(get(), AppConstant.BASE_URL) }

    single { createOkHttpClient() }

    single { createWebService<ApiService>(get(), AppConstant.BASE_URL) }

    single { MoshiConverterFactory.create() }

    single { Moshi.Builder().build() }
}

fun createOkHttpClient(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient.Builder()
    client.connectTimeout(5, TimeUnit.MINUTES)
    client.readTimeout(5, TimeUnit.MINUTES)
    client.writeTimeout(5, TimeUnit.MINUTES)
    client.connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
    //client.pingInterval(20,  TimeUnit.SECONDS)
    client.protocols(listOf(Protocol.HTTP_1_1))
    return client.addInterceptor {
        val original = it.request()
        val requestBuilder = original.newBuilder()
        requestBuilder.header("Content-Type", "application/json")
        val request = requestBuilder.method(original.method, original.body).build()
        return@addInterceptor it.proceed(request)
    }.build()
}

fun createRetrofit(okHttpClient: OkHttpClient, url: String): Retrofit {
    return Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create()).build()
}

fun createService(retrofit: Retrofit): ApiRepository {
    return retrofit.create(ApiRepository::class.java)
}

fun createLoginUseCase(apiRepository: ApiRepository): ApiUseCase {
    return ApiUseCase(apiRepository)
}

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit.create(T::class.java)
}