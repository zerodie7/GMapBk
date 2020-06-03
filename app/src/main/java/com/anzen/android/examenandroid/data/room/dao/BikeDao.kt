package com.anzen.android.examenandroid.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.anzen.android.examenandroid.domain.PickUpBike
import java.util.*

@Dao
interface BikeDao {

    @Transaction
   suspend fun insertAll(list: List<PickUpBike>?) = list?.forEach(){ bike ->
        insertBikeList(bike)
    }

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBikeList( bikes: PickUpBike)

    @Query("SELECT * FROM PickUpBike p WHERE p.bikes != 0 and p.status != 'CLS' ")
     fun getAllBikes(): LiveData<List<PickUpBike>>

    @Query("SELECT * FROM PickUpBike p WHERE p.bikes != 0 and p.status != 'CLS' ORDER BY p.bikes ASC")
    fun getBikesAsc(): LiveData<List<PickUpBike>>

    @Query("SELECT * FROM PickUpBike p WHERE p.bikes != 0 and p.status != 'CLS' ORDER BY p.slots DESC")
    fun getSlotsDesc(): LiveData<List<PickUpBike>>

    @Query("SELECT * FROM PickUpBike p WHERE p.ubication_id = :uid  AND  p.bikes != 0 AND  p.status != 'CLS' LIMIT 1")
    fun getByUID(uid: Int): LiveData<PickUpBike>

}
