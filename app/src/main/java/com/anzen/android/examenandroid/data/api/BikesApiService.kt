package com.anzen.android.examenandroid.data.api

import com.anzen.android.examenandroid.domain.PickUpBike
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class BikesApiService {
    private val BASE_URL = "https://raw.githubusercontent.com"

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BikesApi::class.java)

    fun getBikes(): Single<List<PickUpBike>> {
        return api.getDogs()
    }

}