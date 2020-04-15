package com.kush.zomatoaggregator.network

import com.kush.zomatoaggregator.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkService {
    private const val NETWORK_REQUEST_TIMEOUT = 60L

    val instance by lazy {
        this.create()
    }

    private fun create(): NetworkApis {
        val retrofitClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            //  Logging interceptor
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            retrofitClientBuilder.addInterceptor(httpLoggingInterceptor)
        }

        retrofitClientBuilder.addInterceptor(RequestInterceptor())
            .callTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
            .client(retrofitClientBuilder.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.base_url)
            .build()
        return retrofit.create(NetworkApis::class.java)
    }

}

class RequestInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Request customization: add request headers
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("user-key", "cfcc581d0ea10817ba96ef17ae9928b1")

        val request = requestBuilder.build()

        return chain.proceed(request)
    }

}