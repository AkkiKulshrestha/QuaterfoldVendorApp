package com.quaterfoldvendorapp.application

import android.app.Application
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.quaterfoldvendorapp.utils.ConnectivityReceiver
import com.quaterfoldvendorapp.utils.ConnectivityReceiver.ConnectivityReceiverListener
import com.quaterfoldvendorapp.utils.SharedPrefsHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin


class QuaterfoldApplication : Application() {
    private var realmConfig: RealmConfiguration? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
//            FirebaseApp.initializeApp(this)
            FirebaseApp.initializeApp(applicationContext)
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        } catch (e: Exception) {
        }
        MultiDex.install(this)
        SharedPrefsHelper.init(applicationContext)

        startKoin {
            androidLogger()
            androidContext(this@QuaterfoldApplication)
            modules(
                listOf(
                    AppModule,
                    NetworkModule
                )
            )
        }

        Realm.init(this)
        realmConfig = RealmConfiguration.Builder()
            .name("quaterfoldvendorapp.db")
            .deleteRealmIfMigrationNeeded()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .schemaVersion(0)
            .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    fun setConnectivityListener(listener: ConnectivityReceiverListener?) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    companion object {
        @get:Synchronized
        var instance: QuaterfoldApplication? = null
            private set
    }
}