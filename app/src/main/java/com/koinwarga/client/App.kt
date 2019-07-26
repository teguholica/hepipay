package com.koinwarga.client

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.koinwarga.client.services.ReceiverWorker
import java.util.concurrent.TimeUnit


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val receiverWorkRequest = PeriodicWorkRequestBuilder<ReceiverWorker>(1, TimeUnit.MINUTES)
            .build()
//        WorkManager.getInstance(this).enqueue(receiverWorkRequest)
        WorkManager.getInstance(this).cancelAllWork()
    }

}