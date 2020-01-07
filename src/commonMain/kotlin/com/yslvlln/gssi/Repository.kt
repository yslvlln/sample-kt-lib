@file:JvmName("KtRepository")

package com.yslvlln.gssi

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
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
        val time: String)

    //
    suspend fun generatePushId(): String {
        val pushId = client.post<JsonObject>{
            url ("$baseUrl/test.json")
            body = "{ }"
        }
        return pushId.get("name").toString().replace("\"", "")
    }

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

    //Use the callback to get the latitude and longitude using the heartbeat object
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
                            heartbeat.time
                        )
                    )
                }
            }

        }
    }

    fun startListener(userid: String): Job {
        return GlobalScope.launch(backgroundDispatcher) {
            val endpoint = baseUrl + "/heartbeat/$userid/${generatePushId()}.json"
            client.patch<JsonObject> {
                url(endpoint)
                body = "{\"isActive\": true}"
            }
        }
    }

    fun stopListener(userid: String): Job {
        return GlobalScope.launch(backgroundDispatcher) {
            val endpoint = baseUrl + "/heartbeat/$userid.json"
            val allLogs = client.get<JsonObject> { url(endpoint) }
            if (allLogs != null) {
                val lastLog = allLogs.keys.last().toString()
                client.patch<JsonObject> {
                    url(baseUrl + "/heartbeat/$userid/$lastLog.json")
                    body = "{\"isActive\": false}"
                }
            }
        }
    }

    fun observeLocation(userid: String, lat: Double, lng: Double): Job {
        val dateFormat = DateFormat("MMM-dd-yyyy")
        val timeFormat = DateFormat("HH:mm:ss")
        return GlobalScope.launch(backgroundDispatcher) {
            val userLogs = client.get<JsonObject>("$baseUrl/heartbeat/${userid}.json")
            val lastLog = userLogs.keys.last().toString()
            val lastLogObj = client.get<JsonObject>("$baseUrl/heartbeat/${userid}/$lastLog.json")
            if (lastLogObj.containsKey("isActive").equals(false)) {
                client.patch<JsonObject> {
                    url("${baseUrl}/heartbeat/${userid}/${generatePushId()}.json")
                    body = "{\"isActive\": true }"
                }
            }
            client.patch<JsonObject> {
                url("${baseUrl}/heartbeat/${userid}/$lastLog/${generatePushId()}.json")
                body = "{ \"lat\": \"${lat}\"," +
                        " \"lng\": \"${lng}\"," +
                        " \"time\": \"${DateTime.nowLocal().format(timeFormat)}\"," +
                        " \"date\": \"${DateTime.nowLocal().format(dateFormat)}\"}"
            }
        }
    }
}