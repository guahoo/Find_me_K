package com.find_me_kotlin

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class GeoLocationFinder(private val context: Context) : Activity(), LocationListener {

    private var mFusedLocationClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(context)
    lateinit var mLastLocation:Location
    private val LOCATION_SETTINGS_REQUEST = 1
    val PERMISSION_ID = 44

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {


             mLastLocation  = locationResult.lastLocation

            try{
                val latitude = mLastLocation.latitude.toString()
                val longitude = mLastLocation.longitude.toString()
                (context as MainActivity).setLatitude(latitude)
                context.setLongitude(longitude)
                context.setTextToTetxView("N $latitude","E $longitude")
                context.sendSms(latitude, longitude)
            }catch (npe:NullPointerException){
                Toast.makeText(context,"Геопозиция не определена",Toast.LENGTH_LONG).show()
            }

        }
    }




    private val isLocationEnabled: Boolean
        get() {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }


    init {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        if (!requestLastLocation()) {
            (context as MainActivity).startActivityTurnOnLocation()
        }


    }

    private fun requestLastLocation(): Boolean {
        if (!isLocationEnabled) {
            return false
        }

        mFusedLocationClient!!.lastLocation.addOnCompleteListener {

            this@GeoLocationFinder.requestNewLocationData()

            try {

                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {

                       // context.sendSms(latitude, longitude)
                    }
                }


            } catch (npe: NullPointerException) {
                this@GeoLocationFinder.requestLastLocation()
            }
        }
        return true

    }

    private fun startActivityTurnOnLocation() {
        Toast.makeText(context, TURONLOCATION, Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        (context as MainActivity).startActivityForResult(intent, LOCATION_SETTINGS_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //        if (resultCode == 1) {
        when (requestCode) {
            LOCATION_SETTINGS_REQUEST -> Toast.makeText(
                applicationContext,
                "!!!",
                Toast.LENGTH_LONG
            ).show()
        }
        //        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(0)
        mLocationRequest.setFastestInterval(0)
        mLocationRequest.setNumUpdates(1)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {

        ActivityCompat.requestPermissions(
            context as MainActivity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID

        )
    }


    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    companion object {

        protected val TURONLOCATION = "Включите доступ к местоположению"
    }
}