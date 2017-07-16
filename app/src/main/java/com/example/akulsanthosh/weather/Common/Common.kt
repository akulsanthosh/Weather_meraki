package com.example.akulsanthosh.weather.Common

import java.text.SimpleDateFormat
import java.util.*



object  Common{
    val API_KEY = "a69a3f8f7cfeef1b425ffd199e45394b"
    val API_LINK = "http://api.openweathermap.org/data/2.5/weather?"

    fun apiRequest(lat:String,lng:String):String{
        var sb = StringBuilder(API_LINK)
        sb.append("lat=$lat&lon=$lng&appid=$API_KEY&units=metric")
        return sb.toString()
    }

    fun unixTimeStampToDateTime(unixTimeStamp: Double):String{
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong()*1000
        return dateFormat.format(date)
    }

    val dateNow:String
        get(){
                val dateFormat = SimpleDateFormat("dd MM yyyy HH:mm")
                val date = Date()
                return dateFormat.format(date)

        }
}