package com.anzen.android.examenandroid.data.api

import com.anzen.android.examenandroid.domain.PickUpBike
import io.reactivex.Single
import retrofit2.http.GET

interface BikesApi {
    @GET("zerodie7/GMapBk/master/bikes.json")
    fun getDogs(): Single<List<PickUpBike>>
}