package com.dngwjy.fleettracker.data.remote

import android.content.Context
import com.dngwjy.fleettracker.data.model.Vehicle
import com.dngwjy.fleettracker.utils.logE
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttTopic
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

sealed class MqttEvent {
    data class MessageReceived(val message: String) : MqttEvent()
    object Connected : MqttEvent()
    data class Error(val error: String) : MqttEvent()
    object Disconnected : MqttEvent()
}
class MqttManager {
    private val _events = Channel<MqttEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    fun connectToBroker(url:String,mqttTopic:String,mqttUsername:String,mqttPassword:String) {
        try {
            val mqttClient = MqttClient(url, MqttClient.generateClientId(), MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = false
                userName=mqttUsername
                password=mqttPassword.toCharArray()
            }
            mqttClient.connect(options)
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    CoroutineScope(Dispatchers.IO).launch{
                        _events.send(MqttEvent.Disconnected)
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val receivedMessage = message?.toString()
                    if (!receivedMessage.isNullOrEmpty()) {
                        val utf8String = receivedMessage.toByteArray(Charset.forName("UTF-8"))
                        val convertedString = String(utf8String, Charset.forName("UTF-8"))
//                        val vehicle = Gson().fromJson(convertedString,Vehicle::class.java)
                        CoroutineScope(Dispatchers.IO).launch {
                            _events.send(MqttEvent.MessageReceived(convertedString))
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {

                }
            })

            mqttClient.subscribe(mqttTopic, 0)
            CoroutineScope(Dispatchers.IO).launch {
                _events.send(MqttEvent.Connected)
            }
        }catch (e:MqttException){
            logE(e.message.toString())
            logE(e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch {
                _events.send(MqttEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

}