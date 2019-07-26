package com.teguholica.hepipay

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.teguholica.hepipay.services.ReceiverWorker
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