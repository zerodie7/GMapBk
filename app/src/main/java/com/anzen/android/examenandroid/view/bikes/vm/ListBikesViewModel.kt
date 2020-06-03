package com.anzen.android.examenandroid.view.bikes.vm

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anzen.android.examenandroid.data.api.BikesApiService
import com.anzen.android.examenandroid.data.room.BikesDatabase
import com.anzen.android.examenandroid.domain.PickUpBike
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ListBikesViewModel(application: Application) : BaseViewModel(application) {

    val empty: MutableLiveData<Boolean> = MutableLiveData(false)

    private val bikesService = BikesApiService()
    private val disposable = CompositeDisposable()
    private val bikeDao = BikesDatabase(getApplication()).bikeDao()

    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromRemote() {
        disposable.add(
            bikesService.getBikes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<PickUpBike>>() {

                    override fun onSuccess(dogList: List<PickUpBike>) {
                        storeBikesLocally(dogList)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                })
        )
    }

    private fun storeBikesLocally(list: List<PickUpBike>) {
        launch {
            bikeDao.insertAll(list)
        }
    }

    fun getAllBikes(): LiveData<List<PickUpBike>> = bikeDao.getAllBikes()
    fun getBikesAsc(): LiveData<List<PickUpBike>> = bikeDao.getBikesAsc()
    fun getSlotsDesc(): LiveData<List<PickUpBike>> = bikeDao.getSlotsDesc()

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}
