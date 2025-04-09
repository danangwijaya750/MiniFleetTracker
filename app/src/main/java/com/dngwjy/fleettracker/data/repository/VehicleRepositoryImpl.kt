package com.dngwjy.fleettracker.data.repository

import android.content.Context
import com.dngwjy.fleettracker.data.model.Vehicle
import com.dngwjy.fleettracker.data.remote.MqttEvent
import com.dngwjy.fleettracker.data.remote.MqttManager
import com.dngwjy.fleettracker.utils.AppConstants
import com.dngwjy.fleettracker.utils.logD
import com.dngwjy.fleettracker.utils.logE
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.lang.Exception
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

class VehicleRepositoryImpl(private val mqttManager: MqttManager):VehicleRepository {
    override suspend fun vehicleStatus(index: Int): Flow<Vehicle> = flow {
        val data=simulateSensorData(index)
        emit(data)
    }

    override fun getVehicleUpdates(): Flow<Vehicle> = flow {
        mqttManager.events.collect { event ->
            when (event) {
                is MqttEvent.MessageReceived -> {
                    try {
                        val vehicle = Gson().fromJson(event.message, Vehicle::class.java)
                        logE(vehicle.toString())
                        emit(vehicle)
                    } catch (e: Exception) {
                        // Handle parsing error
                    }
                }
                else -> { /* Handle other events if needed */ }
            }
        }
    }

    override fun connectToBroker()  {
        CoroutineScope(Dispatchers.IO).launch {
            mqttManager.connectToBroker(AppConstants.MQTT_HOST,AppConstants.MQTT_TOPIC,
                AppConstants.MQTT_USERNAME,AppConstants.MQTT_PASSWORD)
        }
//        mqttManager.connect(
//            serverUri = "66fde72f2b504a62a2a89926aae05d9f.s1.eu.hivemq.cloud:8883"
//        )
//        mqttManager.subscribe("test/topic")
    }

    override fun disconnect() {
//        mqttManager.disconnect()
    }

    private val route = listOf(
        GeoPoint(-7.783037, 110.367043), // Tugu
        GeoPoint(-7.784334, 110.366861),
        GeoPoint(-7.786492, 110.366614),
        GeoPoint(-7.787885, 110.366464),
        GeoPoint(-7.789160, 110.366303),
        GeoPoint(-7.789596, 110.367140),
        GeoPoint(-7.790255, 110.368330),
        GeoPoint(-7.790149, 110.367365), // to malioboro
        GeoPoint(-7.790106, 110.366281),
        GeoPoint(-7.791276, 110.366035),
        GeoPoint(-7.793997, 110.365707),
        GeoPoint(-7.794847, 110.365595),
        GeoPoint(-7.795878, 110.365477),// to 0 km
        GeoPoint(-7.796569, 110.365391),
        GeoPoint(-7.797802, 110.365230),
        GeoPoint(-7.799556, 110.364929),
        GeoPoint(-7.801246, 110.364736), // 0 km
    )
    private fun simulateSensorData(index:Int):Vehicle {
        val engineOn = nextBoolean()
        val doorOpen = nextBoolean()
        val spd = if (engineOn) nextInt(100) else 0

//        if (spd > 80.0) logD("Speed > 80km/h")
//        if (spd > 0 && doorOpen) logD( "Door open while moving")
        val location = route[index % route.size]
        return Vehicle("1",location.latitude,location.longitude,engineOn,doorOpen,spd)
    }

}