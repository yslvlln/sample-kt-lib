@file:JvmName("KtRepository")

package com.yslvlln.gssi

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName

class Repository {

    val client by lazy {
        HttpClient() {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json.nonstrict)
            }
        }
    }

    val baseUrl = "https://heartbeat-5284a.firebaseio.com/"

    @Serializable
    data class Heartbeat(
        val date: String,
        val lat: Double,
        val lng: Double,
        val time: String,
        val user: String)

    //Use the callback to get the the userids under heartbeat.json
    fun observeUsers(onUserFetchedCallback: (String) -> Unit): Job {
        val endpoint = baseUrl + "heartbeat.json"
        return GlobalScope.launch(backgroundDispatcher) {
            val result = client.get<JsonObject> { url(endpoint) }.keys.toList()
            result.forEach { user ->
                onUserFetchedCallback(user)
            }

        }
    }

    //Use the callback to get the userids under heartbeat/userid.json
    fun observeLogs(userid: String, onLogFetchedCallback: (String) -> Unit): Job {
        val endpoint = baseUrl + "/heartbeat/$userid.json"
        return GlobalScope.launch(backgroundDispatcher) {
            val result = client.get<JsonObject> { url(endpoint) }.keys.toList()
            result.forEach {
                onLogFetchedCallback(it)
            }
        }
    }

    //Use the callbac to get the latitude and longitude using the heartbeat object
    //heartbeat.getLat() & heartbeat.getLng()
    fun observeHeartbeat(userid: String, logid: String, onHeartbeatFetchedCallback: (Heartbeat) -> Unit): Job {
        val endpoint = baseUrl + "/heartbeat/$userid/$logid.json"
        return GlobalScope.launch(backgroundDispatcher) {
            val result = client.get<JsonObject>{ url(endpoint) }
            val pushids = result.keys.toList()
            val ignore = listOf("isActive", "startLocation", "endLocation", "startTimestamp", "endTimestamp")
            pushids.forEach {
                if (it !in ignore) {
                    val locationRef = result.getObject(it)
                    val heartbeat = Json.parse(Heartbeat.serializer(), locationRef.toString())
                    onHeartbeatFetchedCallback(
                        Heartbeat(
                            heartbeat.date,
                            heartbeat.lat,
                            heartbeat.lng,
                            heartbeat.time,
                            heartbeat.user
                        )
                    )
                }
            }

        }
    }

}