package com.find_me_kotlin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class GeoLocationFinder(private val context: Context) : Activity(), LocationListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_SETTINGS_REQUEST = 1
    private val PERMISSION_ID = 44
    private val TURONLOCATION = "Включите доступ к местоположению"
    private val geolocationNotDefined = "Геопозиция не определена"




    fun getLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestLocationPermissions()
            return
        }



        fusedLocationClient.lastLocation.addOnSuccessListener { location ->


            if (location != null) {
                try {
                    printLocationOnGuiAndSendSms(location)
                } catch (npe: NullPointerException) {

                    Toast.makeText(context, geolocationNotDefined, Toast.LENGTH_LONG).show()
                }
            } else {

                if (!requestLastLocation()) {
                   startActivityTurnOnLocation()
                    this@GeoLocationFinder.requestNewLocationData()
                } else{
                    this@GeoLocationFinder.requestNewLocationData()
                }


            }

            return@addOnSuccessListener
        }
    }


    private fun printLocationOnGuiAndSendSms(location: Location) {
        val latitude = location.latitude.toString()
        val longitude = location.longitude.toString()
        (context as MainActivity).setLatitude(latitude)
        context.setLongitude(longitude)
        context.setTextToTextView("N $latitude", "E $longitude")
        context.sendSms(latitude, longitude)
    }


    override fun onLocationChanged(p0: Location?) {}

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    private val isLocationEnabled: Boolean
        get() { val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    private fun requestLastLocation(): Boolean {
        return isLocationEnabled
    }

    private fun startActivityTurnOnLocation() {
        Toast.makeText(context, TURONLOCATION, Toast.LENGTH_LONG).show()
        (context as MainActivity).startActivityTurnOnLocation()
        finish()
    }


    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }

        val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
            }

        }
        fusedLocationClient .requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            context as MainActivity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCATION_SETTINGS_REQUEST -> Toast.makeText(
                applicationContext,
                "!!!",
                Toast.LENGTH_LONG
            ).show()
        }

    }
}