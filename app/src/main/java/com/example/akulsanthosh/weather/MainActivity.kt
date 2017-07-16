package com.example.akulsanthosh.weather

import android.annotation.TargetApi
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.AsyncTask
import com.google.android.gms.location.LocationListener
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.Html
import android.util.Log
import android.widget.Toast
import com.example.akulsanthosh.weather.Common.Common
import com.example.akulsanthosh.weather.Common.Helper
import com.example.akulsanthosh.weather.Common.setWeatherIcon
import com.example.akulsanthosh.weather.Model.OpenWeatherMap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    val PERMISSION_REQUEST_CODE = 1001
    val PLAY_SERVICE_RESOLUTION_REQUEST = 1000


    var mGoogleApiClient:GoogleApiClient? = null
    var mLocationRequest:LocationRequest? = null
    internal var openWeatherMap = OpenWeatherMap()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        requestPermission()
        if(checkPlayService())
            buildGoogleApiClient()


    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayService())
                    {
                        buildGoogleApiClient()
                    }
                }
            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()

    }

    private fun  checkPlayService(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RESOLUTION_REQUEST).show()
            }
            else
            {
                Toast.makeText(applicationContext,"This device is not supported",Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000 //10sec
        mLocationRequest!!.fastestInterval = 5000 //5sec
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("ERROR","Connection Failed: " + p0.errorCode)

    }

    override fun onLocationChanged(location: Location?) {
        GetWeather().execute(Common.apiRequest(location!!.latitude.toString(),location!!.longitude.toString()))
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
    }


    override fun onStart() {
        super.onStart()
        if(mGoogleApiClient != null)
            mGoogleApiClient!!.connect()
    }

    override fun onDestroy() {
        mGoogleApiClient!!.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkPlayService()
    }

    private inner class GetWeather: AsyncTask<String, Void, String>()
     {
        internal var pd = ProgressDialog(this@MainActivity)

        override fun onPreExecute() {
            super.onPreExecute()
            pd.setTitle("Please wait...")
            //pd.show()
        }
        override fun doInBackground(vararg params: String?): String {
            var stream:String
            var urlString=params[0]

            val http = Helper()
            stream = http.getHTTPData(urlString)
            return stream

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result!!.contains("Error: Not found city"))
            {
                pd.dismiss()
                return
            }
            val gson = Gson()
            val mType = object:TypeToken<OpenWeatherMap>(){}.type
            openWeatherMap = gson.fromJson<OpenWeatherMap>(result,mType)
            pd.dismiss()

            //changing strings
            val res:Resources = resources
            val sunRise:String = res.getString(R.string.sunRise,Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise))
            val sunSet:String = res.getString(R.string.sunSet,Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset))
            val humidity:String = res.getString(R.string.humidity,openWeatherMap.main!!.humidity)
            val pressure:String = res.getString(R.string.pressure,openWeatherMap.main!!.pressure)
            val high:String = res.getString(R.string.high,openWeatherMap.main!!.temp_max)
            val low:String = res.getString(R.string.low,openWeatherMap.main!!.temp_min)
            val icon:String = res.getString(R.string.icon,Html.fromHtml(setWeatherIcon(openWeatherMap.weather!![0].id,openWeatherMap.sys!!.sunrise,openWeatherMap.sys!!.sunset)))
            val temp:String = res.getString(R.string.temp,openWeatherMap.main!!.temp)

            //Set info into layout
            cityName.text = "${openWeatherMap.name}"
            description.text = "${openWeatherMap.weather!![0].description}"
            dayName.text = "Last Updated: ${Common.dateNow}"
            tempValue.text = temp
            //with icons
            sunrise.text = sunRise
            sunset.text = sunSet
            humidityValue.text = humidity
            precipitationValue.text = pressure
            maxValue.text = high
            minValue.text = low
            weatherIcon.text = icon
        }

    }


}




