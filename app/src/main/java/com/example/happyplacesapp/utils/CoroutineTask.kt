package com.example.happyplacesapp.utils

import kotlinx.coroutines.*

abstract class CoroutineTask<Params, Result> {


    protected open fun onPreExecute() {}

    protected abstract fun doInBackground(vararg params: Params): Result?

    protected open fun onPostExecute(result: Result?) {}


    private val mUiScope by lazy { CoroutineScope(Dispatchers.Main) }

    private lateinit var mJob: Job

    fun execute(vararg params: Params) {
        mJob = mUiScope.launch {
            // run on UI/Main thread
            onPreExecute()

            // execute on worker thread
            val result = async(Dispatchers.IO) { doInBackground(*params) }

            // return result on UI/Main thread
            onPostExecute(result.await())
        }
    }

    fun cancel() {
        if (::mJob.isInitialized && mJob.isActive) mJob.cancel()
    }

    fun cancelAll() {
        if (mUiScope.isActive) mUiScope.cancel()
    }
}

