package com.anzen.android.examenandroid.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

/**
 * Created by Diego Martinez on 02/07/20
 */

class GPSTracker(val context: Context): LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var locationChangeListener: LocationChangeListener? = null

    fun startTrack(): GPSTracker{
        turnOnGPS()
        return this
    }

    fun addListener(locationChangeListener: LocationChangeListener){
        //this.locationChageListener = null
        this.locationChangeListener = locationChangeListener
    }

    fun stopTrack(){
        this.locationChangeListener = null
        turnOffGPS()
    }

    //gps permission
    @SuppressLint("MissingPermission")
    private fun turnOnGPS() {
        try {
            if (isGPSActivated()) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    0f,
                    this
                )
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE
                // BestProvider
                val provider = locationManager.getBestProvider(criteria, true) ?: ""
                // Getting last location available
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOffGPS(){
        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Error desactivando GPS $e",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun isGPSActivated(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation
                return if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(context,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE_GPS)
                    false
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE_GPS)
                    false
                }
            }else {
                return true
            }
        }else {
            return true
        }
    }

    override fun onLocationChanged(p0: Location?) {
        p0?.let {
            latitude = it.latitude
            longitude = it.longitude
            locationChangeListener?.onChange(latitude, longitude)
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    interface LocationChangeListener{
        fun onChange(latitude: Double, longitude: Double)
    }

    companion object{
        const val REQUEST_CODE_GPS = 1001
    }
}