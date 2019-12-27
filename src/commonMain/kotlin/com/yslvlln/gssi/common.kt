package com.yslvlln.gssi

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
expect val mainDispatcher: CoroutineDispatcher

@ThreadLocal
expect val backgroundDispatcher: CoroutineDispatcher