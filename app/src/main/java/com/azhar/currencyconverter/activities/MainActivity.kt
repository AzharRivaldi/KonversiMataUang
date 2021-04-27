package com.azhar.currencyconverter.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.azhar.currencyconverter.R
import com.azhar.currencyconverter.databinding.ActivityMainBinding
import com.azhar.currencyconverter.networking.ApiEndpoint
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var strInputNominal: String = ""
    var strCountrySelected1: String = ""
    var strCountryCodeSelected1: String = ""
    var strCountrySelected2: String = ""
    var strCountryCodeSelected2: String = ""
    var strTitleResult: String = ""
    var strFromResult: String = ""
    var strToResult: String = ""
    var strDateNow: String = ""
    lateinit var strCountry: Array<String>
    lateinit var strCountryCode: Array<String>
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Mohon Tunggu...")
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Sedang menampilkan data")

        //set spinner data negara
        strCountry = resources.getStringArray(R.array.countryName)

        //set spinner data kode mata uang
        strCountryCode = resources.getStringArray(R.array.countryCode)

        //hide item layout
        binding.imageClear.visibility = View.GONE
        binding.linearHasil.visibility = View.GONE

        //set array spinner 1
        val arrayCountry1 = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, strCountry)
        arrayCountry1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //set array spinner 2
        val arrayCountry2 = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, strCountry)
        arrayCountry2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //set spinner 1
        binding.spCountry1.adapter = arrayCountry1
        binding.spCountry1.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                strCountrySelected1 = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        //set spinner 2
        binding.spCountry2.adapter = arrayCountry2
        binding.spCountry2.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                strCountrySelected2 = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        //clear data with image delete
        binding.imageClear.setOnClickListener {
            binding.etInputNominal.text.clear()
            binding.linearHasil.visibility = View.GONE
            binding.imageClear.visibility = View.GONE
        }

        //action mendapatkan hasil konversi mata uang
        binding.btnHitung.setOnClickListener {
            strInputNominal = binding.etInputNominal.text.toString()
            if (strInputNominal.isEmpty()) {
                Toast.makeText(this@MainActivity, "Form tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                getCurrency(strCountryCodeSelected1, strCountryCodeSelected2, strInputNominal)
                val inputManager = application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(window.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    //method get data konversi mata uang
    private fun getCurrency(strCountryCodeSelected1: String, strCountryCodeSelected2: String, strInputNominal: String) {
        progressDialog?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL)
                .addPathParameter("currCodefrom", strCountryCodeSelected1)
                .addPathParameter("currCodeto", strCountryCodeSelected2)
                .addPathParameter("amount", strInputNominal)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog?.dismiss()
                        try {
                            val jsonObject = response.getJSONObject("data")
                            strTitleResult = jsonObject.getString("title")
                            strFromResult = jsonObject.getString("fromResult")
                            strToResult = jsonObject.getString("toResult")
                            strDateNow = jsonObject.getString("updatedAt")

                            //set text
                            binding.tvTitleHasil.text = "Result : $strTitleResult"
                            binding.tvCurrencyFrom.text = "Nominal Input : $strFromResult"
                            binding.tvCurrencyTo.text = "Hasil Konversi : $strToResult"
                            binding.tvDateNow.text = "Update Terakhir : $strDateNow"

                            //show item layout
                            binding.linearHasil.visibility = View.VISIBLE
                            binding.imageClear.visibility = View.VISIBLE
                        } catch (e: JSONException) {
                            Toast.makeText(this@MainActivity,
                                    "Oops, gagal menampilkan hasil konversi!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        progressDialog?.dismiss()
                        Toast.makeText(this@MainActivity,
                                "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                    }
                })
    }
}