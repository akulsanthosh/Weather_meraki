package com.example.akulsanthosh.weather.Common

import android.app.Activity
import com.example.akulsanthosh.weather.R
import java.util.*

fun setWeatherIcon(actualId:Int,sunrise:Double,sunset:Double):String{
    var id:Int = actualId/100
    var icon:String =  ""
    if(actualId==800){
        var time:Long = Date().time
        if(time>=sunrise && time<sunset){
            icon = "&#xf00d;"
        }
        else{
            icon = "&#xf02e;"
        }
    }
    else{
        when(id){
            2 -> icon = "&#xf01e;"
            3 -> icon = "&#xf01c;"
            7 -> icon = "&#xf014;"
            8 -> icon = "&#xf013;"
            6 -> icon = "&#xf01b;"
            5 -> icon = "&#xf019;"
        }
    }
    return icon
}
