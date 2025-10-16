package com.example.weatherapp.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.weatherapp.R
import com.example.weatherapp.adapter.MainAdapter
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.ModelMain
import com.example.weatherapp.networking.ApiEndpoint
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), LocationListener {
    private var lat: Double? = null
    private var lng: Double? = null
    private var hariIni: String? = null
    private var progressBar: ProgressBar? = null
    private var mainAdapter: MainAdapter? = null
    private val modelMain: MutableList<ModelMain> = ArrayList()
    private lateinit var binding: ActivityMainBinding
    var permissionArrays = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //set Transparent Statusbar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        //set Permission
        val myVersion = Build.VERSION.SDK_INT
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEE", dateNow) as String

        progressBar = ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleLarge)
        progressBar?.findViewById<ProgressBar>(R.id.progressbar)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        view.addView(progressBar, params)


        mainAdapter = MainAdapter(modelMain)

        binding.rvListWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvListWeather.setHasFixedSize(true)
        binding.rvListWeather.adapter = mainAdapter

        //method get LatLong & get Date
        getToday()
        getLatlong()
    }

    private fun getToday() {
        val date = Calendar.getInstance().time
        val tanggal = DateFormat.format("d MMM yyyy", date) as String
        val formatDate = "$hariIni, $tanggal"
        binding.tvDate.text = formatDate
    }

    private fun getLatlong() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 115)
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider!!)
        if (location != null) {
            onLocationChanged(location)
        } else {
            locationManager.requestLocationUpdates(provider!!, 20000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude

        //method get Data Weather
        getCurrentWeather()
        getListWeather()
    }

    private fun getCurrentWeather() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.CurrentWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArrayOne = response.getJSONArray("weather")
                        val jsonObjectOne = jsonArrayOne.getJSONObject(0)
                        val jsonObjectTwo = response.getJSONObject("main")
                        val jsonObjectThree = response.getJSONObject("wind")
                        val strWeather = jsonObjectOne.getString("main")
                        val strDescWeather = jsonObjectOne.getString("description")
                        val strKecepatanAngin = jsonObjectThree.getString("speed")
                        val strKelembaban = jsonObjectTwo.getString("humidity")
                        val strNamaKota = response.getString("name")
                        val dblTemperatur = jsonObjectTwo.getDouble("temp")

                        if (strDescWeather == "broken clouds") {
                            binding.iconTemp.setAnimation(R.raw.broken_clouds)
                            binding.tvWeather.text = "Awan Tersebar"
                        } else if (strDescWeather == "light rain") {
                            binding.iconTemp.setAnimation(R.raw.light_rain)
                            binding.tvWeather.text = "Gerimis"
                        } else if (strDescWeather == "haze") {
                            binding.iconTemp.setAnimation(R.raw.broken_clouds)
                            binding.tvWeather.text = "Berkabut"
                        } else if (strDescWeather == "overcast clouds") {
                            binding.iconTemp.setAnimation(R.raw.overcast_clouds)
                            binding.tvWeather.text = "Awan Mendung"
                        } else if (strDescWeather == "moderate rain") {
                            binding.iconTemp.setAnimation(R.raw.moderate_rain)
                            binding.tvWeather.text = "Hujan Ringan"
                        } else if (strDescWeather == "few clouds") {
                            binding.iconTemp.setAnimation(R.raw.few_clouds)
                            binding.tvWeather.text = "Berawan"
                        } else if (strDescWeather == "heavy intensity rain") {
                            binding.iconTemp.setAnimation(R.raw.heavy_intentsity)
                            binding.tvWeather.text = "Hujan Lebat"
                        } else if (strDescWeather == "clear sky") {
                            binding.iconTemp.setAnimation(R.raw.clear_sky)
                            binding.tvWeather.text = "Cerah"
                        } else if (strDescWeather == "scattered clouds") {
                            binding.iconTemp.setAnimation(R.raw.scattered_clouds)
                            binding.tvWeather.text = "Awan Tersebar"
                        } else {
                            binding.iconTemp.setAnimation(R.raw.unknown)
                            binding.tvWeather.text = strWeather
                        }

                        binding.toolbarLayout.tvNamaKota.text = strNamaKota
                        binding.tvTempeatur.text = String.format(Locale.getDefault(), "%.0f°C", dblTemperatur)
                        binding.tvKecepatanAngin.text = "Kecepatan Angin $strKecepatanAngin km/j"
                        binding.tvKelembaban.text = "Kelembaban $strKelembaban %"
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Gagal menampilkan data header!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getListWeather() {
        progressBar?.visibility = View.VISIBLE
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.ListWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        progressBar?.visibility = View.GONE
                        val jsonArray = response.getJSONArray("list")
                        for (i in 0..6) {
                            val dataApi = ModelMain()
                            val objectList = jsonArray.getJSONObject(i)
                            val jsonObjectOne = objectList.getJSONObject("main")
                            val jsonArrayOne = objectList.getJSONArray("weather")
                            val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                            var timeNow = objectList.getString("dt_txt")
                            val formatDefault = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val formatTimeCustom = SimpleDateFormat("kk:mm")

                            try {
                                val timesFormat = formatDefault.parse(timeNow)
                                timeNow = formatTimeCustom.format(timesFormat)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                            dataApi.timeNow = timeNow
                            dataApi.currentTemp = jsonObjectOne.getDouble("temp")
                            dataApi.descWeather = jsonObjectTwo.getString("description")
                            dataApi.tempMin = jsonObjectOne.getDouble("temp_min")
                            dataApi.tempMax = jsonObjectOne.getDouble("temp_max")
                            modelMain.add(dataApi)
                        }
                        mainAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    progressBar?.visibility = View.GONE;
                    Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatlong()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}