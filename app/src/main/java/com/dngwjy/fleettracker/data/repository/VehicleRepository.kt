package com.dngwjy.fleettracker.data.repository

import android.content.Context
import com.dngwjy.fleettracker.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    suspend fun vehicleStatus(index: Int): Flow<Vehicle>
    fun connectToBroker()
    fun disconnect()
    fun getVehicleUpdates(): Flow<Vehicle>
}