package com.example.hostelmanager

import android.app.Application
import com.example.hostelmanager.di.AppContainer
import com.example.hostelmanager.di.AppContainerImpl

class HostelManagerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
