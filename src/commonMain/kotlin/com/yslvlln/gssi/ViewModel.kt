@file:JvmName("KtViewModel")

package com.yslvlln.gssi

import com.yslvlln.gssi.Repository
import com.yslvlln.gssi.mainDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

class ViewModel(private val repo: Repository = Repository()) {

    fun observeUsers(onChangeCallback: (String) -> Unit) {
        repo.observeUsers {
            GlobalScope.launch(mainDispatcher){
                onChangeCallback(it)
            }
        }
    }

    fun observeLogs(userid: String, onChangeCallback: (String) -> Unit) {
        repo.observeLogs(userid) {
            GlobalScope.launch(mainDispatcher){
                onChangeCallback(it)
            }
        }
    }

    fun observeHeartbeat(userid: String, logid: String, onHeartbeatFetchedCallback: (Repository.Heartbeat) -> Unit) {
        repo.observeHeartbeat(userid, logid) {
            GlobalScope.launch(mainDispatcher) {
                onHeartbeatFetchedCallback(it)
            }
        }
    }

    fun startListener(userid: String) {
        repo.startListener(userid)
    }

    fun stopListener(userid: String) {
        repo.stopListener(userid)
    }

    fun observeLocation(userid: String, lat: Double, lng: Double) {
        repo.observeLocation(userid, lat, lng)
    }
}