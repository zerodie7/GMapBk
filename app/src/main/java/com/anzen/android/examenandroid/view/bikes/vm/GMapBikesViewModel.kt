package com.anzen.android.examenandroid.view.bikes.vm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anzen.android.examenandroid.data.room.BikesDatabase
import com.anzen.android.examenandroid.domain.PickUpBike
import com.google.android.gms.maps.model.Marker

class GMapBikesViewModel (application: Application) : BaseViewModel(application) {

    private val bikeDao = BikesDatabase(getApplication()).bikeDao()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val oldSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val showService: MutableLiveData<Boolean> = MutableLiveData(false)
    val bikeUID: MutableLiveData<Int> = MutableLiveData()



    val markersResult: MutableList<Marker?> = ArrayList()

    val catBikes: MutableLiveData<String> = MutableLiveData(ALL)


    fun getAllBikes(): LiveData<List<PickUpBike>> = bikeDao.getAllBikes()
    fun getBikesByUID(): LiveData<PickUpBike> = bikeDao.getByUID(bikeUID.value ?: 1)

    fun getAllBikesFilter(): LiveData<List<PickUpBike>> {
        return when (catBikes.value){
            "DESC" -> bikeDao.getSlotsDesc()
            "ASC" -> bikeDao.getBikesAsc()
            else -> bikeDao.getAllBikes()
        }
    }

    fun getBikesAsc(): LiveData<List<PickUpBike>> = bikeDao.getBikesAsc()
    fun getSlotsDesc(): LiveData<List<PickUpBike>> = bikeDao.getSlotsDesc()

    companion object{
        const val ALL = "ALL"
    }

}
