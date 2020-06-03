package com.anzen.android.examenandroid.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anzen.android.examenandroid.utils.ListItemViewModel
import com.google.gson.annotations.SerializedName

@Entity
data class PickUpBike(
    @ColumnInfo(name = "ubication_id")
    @SerializedName("id")
    @PrimaryKey var id: Int?,

    @SerializedName("district")
    var district: String?,

    @SerializedName("lon")
    var lon: Double?,

    @SerializedName("lat")
    var lat: Double?,

    @SerializedName("bikes")
    var bikes: Int?,

    @SerializedName("slots")
    var slots: Int?,

    @SerializedName("zip")
    var zip: Int?,

    @SerializedName("address")
    var address: String?,

    @ColumnInfo(name = "address_number")
    @SerializedName("addressNumber")
    var address_number: String?,

    @ColumnInfo(name = "nearby_stations")
    @SerializedName("nearbyStations")
    var nearby_stations: String?,

    @SerializedName("status")
    var status: String?,

    @SerializedName("name")
    var name: String?,

    @ColumnInfo(name = "station_type")
    @SerializedName("stationType")
    var station_type: String?
): ListItemViewModel()