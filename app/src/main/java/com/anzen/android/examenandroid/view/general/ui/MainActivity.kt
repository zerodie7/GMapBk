package com.anzen.android.examenandroid.view.general.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.anzen.android.examenandroid.R
import com.anzen.android.examenandroid.utils.GPSTracker

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    private val gpsTracker: GPSTracker by lazy {
        GPSTracker(this)
    }

    fun startGPSTrack(locationChangeListener: GPSTracker.LocationChangeListener){
        gpsTracker.startTrack().addListener(locationChangeListener)
    }

    fun navigate(des: Int, args: Bundle = Bundle.EMPTY){
        Log.e("currentDestination", navController.currentDestination?.label.toString())
        navController.navigate(des, args)
    }

}
