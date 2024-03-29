package com.koinwarga.client.datasources.local_database

import android.content.Context
import androidx.room.Room

object LocalDatabase {
    fun connect(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "db"
        ).build()
    }
}