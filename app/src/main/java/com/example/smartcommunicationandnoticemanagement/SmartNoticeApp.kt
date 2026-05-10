package com.example.smartcommunicationandnoticemanagement

import android.app.Application
import com.example.smartcommunicationandnoticemanagement.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartNoticeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
