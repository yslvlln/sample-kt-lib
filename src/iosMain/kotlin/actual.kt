import kotlinx.coroutines.CoroutineDispatcher

/*actual val mainDispatcher = object : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatch_get_main_queue()) {
            block.run()
        }
    }
}

actual val backgroundDispatcher = mainDispatcher*/

actual val mainDispatcher: CoroutineDispatcher
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual val backgroundDispatcher: CoroutineDispatcher
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.