/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * BaseTask.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

abstract class BaseTask<T, R> {
    private var _future: FutureTask<Void>? = null
    private var _latch: CountDownLatch? = null
    var isCancelled = false
        private set

    // R is defined in BaseTask implementation
    private val _result: ArrayList<R?> = ArrayList<R?>()

    fun cancel(mayInterruptIfRunning: Boolean?) {
        _future!!.cancel(mayInterruptIfRunning!!)
        isCancelled = true
    }

    // T is defined in BaseTask implementation
    fun execute(vararg params: T) {
        _future = FutureTask {
            _latch = CountDownLatch(1)
            Thread { preExecute() }.start()
            _latch!!.await()
            _latch = CountDownLatch(1)
            Thread { executeInBackground(*params) }.start()
            _latch!!.await()
            _latch = null
            Thread { onPostExecute(_result[0]) }.start()
            null
        }
        executor.execute(_future)
    }

    private fun preExecute() {
        onPreExecute()
        _latch!!.countDown()
    }

    // T is defined in BaseTask implementation
    private fun executeInBackground(vararg params: T) {
        val result = doInBackground(*params)
        _result.add(result)
        _latch!!.countDown()
    }

    protected open fun onPreExecute() {}

    // T is defined in BaseTask implementation
    protected abstract fun doInBackground(vararg params: T): R?
    protected open fun onPostExecute(result: R?) {}

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }
}