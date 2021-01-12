package com.find_me_kotlin

import android.Manifest.permission.SEND_SMS
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 0
    private val TURONLOCATION = "Включите доступ к местоположению"
    private val SEND = "Сообщение отправлено"
    private val SMS_FAILED = "Сообщение не отправлено, попробуйте позже"
    private val COPYCOORDS = "Координаты скопированы"
    private val FINDME = "Найди меня! Широта: N %s, Долгота: E %s"
    private val PREFERENCES = "PREFERENCES"
    private val PHONE_NUMBER = "phone"


    private val CHOOSEPHONENUMBER = "Пожалуйста, выберите номер получателя"
    private lateinit var sharedPreferences: SharedPreferences

    private val LOCATION_SETTINGS_REQUEST = 1

    internal var name: String?=null

    private lateinit var lampLiting: Runnable
    internal var i = 0

    private var latitude: String?=null
    private var longitude: String?=null

    private var switchGPS = false


    fun setLatitude(latitude: String) {
        this.latitude = latitude
    }

    fun setLongitude(longitude: String) {
        this.longitude = longitude
    }

    private var phoneNo: String? = null
    private var message: String? = null
    private lateinit var geoLocationFinder: GeoLocationFinder
    internal var isPressed = false

    val handler = Handler()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        button_sos.setOnClickListener {
            lampLiting = object : Runnable {
                override fun run() {
                    if (i == 20) { // just remove call backs
                        handler.removeCallbacks(this)

                    } else { // post again
                        isPressed = if (isPressed) {
                            i++
                            lightLamp(true)
                            false
                        } else {
                            lightLamp(false)
                            true
                        }
                        handler.postDelayed(this, 1000)
                    }
                }

            }

            lampLiting.run()

            getGeoPosition()
        }

        button_phone_book.setOnClickListener {
            val intent = Intent(applicationContext, NumberHolderActivity::class.java)
            startActivity(intent)
        }
        button_copy.setOnClickListener {
            copy(latitude_view.text.toString() + ", " + longitude_view.text.toString()) }


    }

    private fun init() {
        sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        phoneNo = sharedPreferences.getString(PHONE_NUMBER, null)
        val contactName = sharedPreferences.getString("name", null)

        longitude_view.text = "Долгота не определена"
        latitude_view.text = "Широта не определена"


        showContact(contactName,phoneNo)
    }

    private fun showContact(contactName:String?, phoneNo: String?) {
        if ((phoneNo==null) and (contactName==null)){
            dialing_a_number.text = "Номер не выбран"
            name_view.text = "Контакт не выбран"
        }else{
            dialing_a_number.text = phoneNo
            name_view.text = contactName
        }
    }

    private fun copy(coord: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(COPYCOORDS, coord)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(applicationContext, COPYCOORDS, Toast.LENGTH_SHORT).show()
    }

    fun sendSms(x: String, y: String) {

        message = String.format(FINDME, x, y)
                phoneNo = sharedPreferences.getString("phone",null)


        try {

            if (ContextCompat.checkSelfPermission(
                    this,
                    SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        SEND_SMS
                    )
                ) {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNo, null, message, null, null)
                    Toast.makeText(
                        applicationContext, SEND,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        SMS_FAILED, Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS
                )


            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Неверный номер!", Toast.LENGTH_LONG).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val smsManager = SmsManager.getDefault()
                try {
                    smsManager.sendTextMessage(phoneNo, null, message, null, null)
                } catch (npe: Exception) {
                    Toast.makeText(
                        applicationContext,
                        CHOOSEPHONENUMBER, Toast.LENGTH_LONG
                    ).show()
                }

                Toast.makeText(applicationContext, SEND, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    SMS_FAILED, Toast.LENGTH_LONG
                ).show()
                return
            }
        }


    }
    private fun getGeoPosition() {
        geoLocationFinder = GeoLocationFinder(this@MainActivity)
        geoLocationFinder.getLastLocation()
    }

    fun startActivityTurnOnLocation() {

        Toast.makeText(this, TURONLOCATION, Toast.LENGTH_LONG).show()
        switchGPS = true
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST)
    }

    fun setTextToTextView(latitude:String, longitude: String){
        latitude_view.text = latitude
        longitude_view.text = longitude
    }

    private fun lightLamp(buttonsosIsPressed: Boolean) {
        if (buttonsosIsPressed) {
            lampView.setImageResource(R.drawable.lamp_light)
        } else {
            lampView.setImageResource(R.drawable.lamp)
        }
    }

    override fun onResume() {
        super.onResume()
        when(switchGPS){
            true-> {
                getGeoPosition()
                switchGPS=false
            }

        }
    }

}
