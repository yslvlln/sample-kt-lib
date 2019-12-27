package com.yslvlln.gssi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
actual val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default