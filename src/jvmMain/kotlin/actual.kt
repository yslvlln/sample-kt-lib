import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val mainDispatcher: CoroutineDispatcher = Dispatchers.Main as CoroutineDispatcher
actual val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default