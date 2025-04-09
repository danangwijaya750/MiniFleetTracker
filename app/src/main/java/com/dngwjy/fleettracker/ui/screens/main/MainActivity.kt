package com.dngwjy.fleettracker.ui.screens.main

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dngwjy.fleettracker.R
import com.dngwjy.fleettracker.data.remote.MqttManager
import com.dngwjy.fleettracker.data.repository.VehicleRepositoryImpl
import com.dngwjy.fleettracker.ui.theme.FleetTrackerTheme
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import com.dngwjy.fleettracker.utils.logE
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            FleetTrackerTheme {
                val mqttManager = remember { MqttManager() }
                val viewModel = MainViewModel(VehicleRepositoryImpl(mqttManager))
                MainScreen(viewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    LaunchedEffect(Unit) {
        //viewModel.loadVehicleAndSimulate()
        viewModel.connectToBroker()
    }
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fleet Tracker") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            VehicleMap(
                viewModel = viewModel,
                modifier = Modifier.matchParentSize()
            )
            AlertList(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                alerts = uiState.alerts
            )
        }
    }
}




@Composable
fun DashboardScreen(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier // apply the passed-in modifier properly
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.vehicle?.speed?.toInt()} km/h",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Current Speed",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Engine: ${if (uiState.vehicle?.engineOn == true) "ON" else "OFF"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Door: ${if (uiState.vehicle?.doorOpen == true) "OPEN" else "CLOSED"}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Composable
fun VehicleMap(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Map view
        AndroidView(
            factory = {
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    minZoomLevel = 4.0
                    maxZoomLevel = 19.0
                    controller.setZoom(16.0)

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay DashboardScreen over the map
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (uiState.vehicle!=null) {
                DashboardScreen(viewModel)
            }
        }
    }

    // Update markers based on vehicle position
    mapView?.let { mv ->
        val location = GeoPoint(uiState.vehicle?.lat ?: -7.783037,
            uiState.vehicle?.lng ?: 110.367043) // default tugu
        mv.overlays.removeIf { true } // Clear existing markers
        if(uiState.vehicle!=null) {
            val marker = Marker(mv).apply {
                position = location
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setIcon(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.baseline_directions_car_filled_24
                    )
                )
            }
            mv.overlays.add(marker)
        }
        mv.controller.animateTo(location)
    }
}