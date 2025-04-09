package com.dngwjy.fleettracker.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngwjy.fleettracker.data.model.Alert
import com.dngwjy.fleettracker.data.model.AlertType
import com.dngwjy.fleettracker.data.model.Vehicle
import com.dngwjy.fleettracker.data.repository.VehicleRepositoryImpl
import com.dngwjy.fleettracker.utils.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
//import javax.inject.Inject


class MainViewModel (private val repository: VehicleRepositoryImpl):ViewModel() {
    private var currentIndex=0
    private val _uiState= MutableStateFlow(MainUiState())
    val uiState:StateFlow<MainUiState> get() = _uiState.asStateFlow()
    private var previousVehicle: Vehicle? =Vehicle("1",0.0,0.0)
    fun loadVehicleAndSimulate(){
        viewModelScope.launch {
            while (true){
                repository.vehicleStatus(currentIndex).collect{v->
                    _uiState.value=_uiState.value.copy(vehicle = v)
                }
                currentIndex++
                delay(3000)
            }
        }
    }
    fun connectToBroker(){
        repository.connectToBroker()
        viewModelScope.launch {
            repository.getVehicleUpdates().collect { vehicle ->
//                logE(vehicle.toString())
//                _uiState.value = _uiState.value.copy(vehicle = vehicle)
                handleNotification(vehicle)
            }
       }
    }

    private fun handleSpeedAlert(vehicle: Vehicle, alerts: MutableList<Alert>) {
        val exists = alerts.any { it.type == AlertType.SPEED }

        if (vehicle.speed > 80) {
            if (!exists) {
                alerts.add(Alert("High Speed: ${vehicle.speed.toInt()} km/h", AlertType.SPEED))
            }
        } else {
            alerts.removeAll { it.type == AlertType.SPEED }
        }
    }

    private fun handleDoorAlert(vehicle: Vehicle, alerts: MutableList<Alert>) {
        val exists = alerts.any { it.type == AlertType.DOOR }
        val conditionMet = vehicle.doorOpen && vehicle.speed > 0

        if (conditionMet) {
            if (!exists) {
                alerts.add(Alert("Door open while moving", AlertType.DOOR))
            }
        } else {
            alerts.removeAll { it.type == AlertType.DOOR }
        }
    }

    private fun handleEngineAlert(vehicle: Vehicle, alerts: MutableList<Alert>) {
        previousVehicle?.let { prev ->
            if (prev.engineOn != vehicle.engineOn) {
                val message = if (vehicle.engineOn) "Engine turned on" else "Engine turned off"
                val alert = Alert(message, AlertType.ENGINE)
                alerts.add(alert)
                viewModelScope.launch {
                    delay(2000)
                    _uiState.value = _uiState.value.copy(
                        alerts = _uiState.value.alerts - alert
                    )
                }
            }
        }
    }
    private fun handleNotification(vehicle: Vehicle){
        val currentAlerts = _uiState.value.alerts.toMutableList()
        handleSpeedAlert(vehicle, currentAlerts)
        handleDoorAlert(vehicle, currentAlerts)
        handleEngineAlert(vehicle, currentAlerts)

        previousVehicle = vehicle
        logE(vehicle.toString())
        _uiState.value = _uiState.value.copy(
            vehicle = vehicle,
            alerts = currentAlerts.distinctBy { it.id }
        )
    }
    fun disconnectBroker(){
        repository.disconnect()
    }
}
data class MainUiState(
    val vehicle: Vehicle?=null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val alerts:List<Alert> = emptyList()
)