package com.anzen.android.examenandroid.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.anzen.android.examenandroid.data.room.dao.BikeDao
import com.anzen.android.examenandroid.domain.PickUpBike

@Database(entities = arrayOf(
    PickUpBike::class),
    version = 1, exportSchema = false)

abstract class BikesDatabase: RoomDatabase() {
    abstract fun bikeDao(): BikeDao

    companion object {
        private const val DB_NAME = "bikes_database"

        @Volatile
        private var instance: BikesDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
            instance
                ?: buildDatabase(
                    context
                ).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            BikesDatabase::class.java,
            DB_NAME
        ).build()
    }
}