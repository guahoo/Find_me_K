package com.find_me_kotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.number_holder.*
import java.util.*

class NumberHolderActivity : AppCompatActivity() {
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val NUMBER_ADD = "Добавлен номер: "
    private val PREFERENCES = "PREFERENCES"



    private lateinit var sharedPreferences:SharedPreferences

    private lateinit var numberList: ArrayList<Char>

    private var isPhoneDeal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.number_holder)
        init()
    }

    private fun init() {

        sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val name :String? = sharedPreferences.getString("name",null);
        val phoneNo :String? = sharedPreferences.getString("phone",null);

        showPhoneNumber(name,phoneNo)




        val imageButtons = arrayOf(
            number_button_11,
            number_button_1,
            number_button_2,
            number_button_3,
            number_button_4,
            number_button_5,
            number_button_6,
            number_button_7,
            number_button_8,
            number_button_9
        )

        numberList = ArrayList()

        for (i in imageButtons.indices) {
            imageButtons[i].setOnClickListener { buttonClick(i) }
        }

        button_phone_book.setOnClickListener {

            if(!isPhoneDeal) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startActivityForResult(intent, REQUEST_CODE_ASK_PERMISSIONS)
                val permissions = arrayOf(Manifest.permission.READ_CONTACTS)
                ActivityCompat.requestPermissions(this, permissions, 0)
            }else{
                savePhoneNumber("Нет в списке",dialing_a_number.text.toString())
                dialing_a_number.visibility = View.INVISIBLE
                showPhoneNumber(sharedPreferences.getString("name",null),
                    sharedPreferences.getString("phone",null))
                isPhoneDeal=false
            }
        }

        button_exit.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }



        button_backspace.setOnClickListener {
            if (numberList.size > 0) {
                numberList.removeAt(numberList.size - 1)

                showText()
            }
        }


    }

    public override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        try {
            val contactData = data!!.data
            val c = with(contentResolver) { query(contactData!!, null, null, null, null) }
            if (c!!.moveToFirst()) {
                val contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                val hasNumber =
                    c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                var num: String
                var name: String
                if (Integer.valueOf(hasNumber) == 1) {
                    val numbers = with(contentResolver) {
                        query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                        null,
                                        null
                                    )
                    }
                    while (numbers != null && numbers.moveToNext()) {




                        num =
                            numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        name =
                            numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

                        if ((num!=null) and (name!=null)){
                            savePhoneNumber(name,num )
                            showPhoneNumber(name,num )
                            Toast.makeText(this, NUMBER_ADD + num, Toast.LENGTH_LONG)
                                .show()
                            }


                    }
                }
            }
        } catch (npe: NullPointerException) {
           nocontact()
        }

    }

    private fun showPhoneNumber(name: String?,num: String?) {
        try {
            name_view.visibility = View.VISIBLE
            numbertextview.visibility = View.VISIBLE



            if ((name==null) and (num==null)){
                name_view.text = "Номер не выбран"
                numbertextview.text = "Контакт не выбран"
            }else{
                name_view.text = name
                numbertextview.text = num
            }


        } catch (npe: NullPointerException){
            nocontact()
        }

    }

    private fun nocontact() {


        numberList.clear()

        val handler = Handler()
        var i=0
        val lampLiting = object : Runnable {
            override fun run() {
                if (i == 3) { // just remove call backs
                    handler.removeCallbacks(this)
                    showPhoneNumber(sharedPreferences.getString("name",null),
                        sharedPreferences.getString("phone",null))
                    if (numberList.size>0){
                        name_view.visibility = View.INVISIBLE
                        numbertextview.visibility = View.INVISIBLE
                    }

                } else { // post again
                    i++
                    name_view.text = ("Контакт не выбран")
                    numbertextview.text = ("Номер не выбран")
                }
                handler.postDelayed(this, 1000)
            }
        }
        lampLiting.run()

    }


    private fun buttonClick(position: Int) {

        if (numberList.size < 12) {
            dialing_a_number.visibility = View.VISIBLE

            when {
                (numberList.size == 0) and (position == 8) -> {
                    numberList.add('+')
                    numberList.add('7')

                }
                (numberList.size == 0) and (position != 8) -> {
                    numberList.add('+')
                    numberList.add(position.toString()[0])

                }
                else -> numberList.add(position.toString()[0])
            }


            showText()
        }
    }

    private fun showText() {
        val stringBuilder = StringBuilder(numberList.size)
        for (ch in numberList) {
            stringBuilder.append(ch)
        }

        if (numberList.size>0){
            name_view.visibility = View.INVISIBLE
            numbertextview.visibility = View.INVISIBLE
            dialing_a_number.visibility = View.VISIBLE
        } else{
            name_view.visibility = View.VISIBLE
            numbertextview.visibility = View.VISIBLE
        }
        dialing_a_number.text = stringBuilder.toString()

       if (numberList.size == 12){
            isPhoneDeal=true

           Toast.makeText(this,isPhoneDeal.toString(),Toast.LENGTH_LONG).show()
        }

    }

    private fun savePhoneNumber(name: String,number: String) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("phone", number)

        editor.apply()

    }
}




