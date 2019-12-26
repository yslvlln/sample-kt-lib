import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
}