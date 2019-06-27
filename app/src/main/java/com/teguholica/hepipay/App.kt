package com.teguholica.hepipay

import android.app.Application
import com.parse.Parse
import com.parse.ParseInstallation


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )

        val installation = ParseInstallation.getCurrentInstallation()
        installation.put("GCMSenderId", getString(R.string.back4app_push_sender_id))
        installation.saveInBackground()
    }

}