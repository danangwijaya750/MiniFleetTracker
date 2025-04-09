package com.dngwjy.fleettracker.data.model

import org.osmdroid.util.GeoPoint

data class Vehicle (
    var vehicleId:String,
    var lat:Double,
    var lng:Double,
    var engineOn:Boolean=false,
    var doorOpen:Boolean=true,
    var speed:Int=0,
)