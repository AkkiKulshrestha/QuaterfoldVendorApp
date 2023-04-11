package com.quaterfoldvendorapp.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.quaterfoldvendorapp.MainActivity
import com.quaterfoldvendorapp.R
import com.quaterfoldvendorapp.databinding.ActivitySplashBinding
import com.quaterfoldvendorapp.sharedpreference.UtilitySharedPreferences
import com.quaterfoldvendorapp.utils.RuntimePermissions
import com.quaterfoldvendorapp.utils.SharedPrefsHelper

class SplashActivity : RuntimePermissions() {

    private lateinit var binding: ActivitySplashBinding
    private val REQUEST_PERMISSIONS = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkInternetConnection(null)) {
            checkRunTimePermission()
        }
        init()
    }

    override fun onPermissionsGranted(requestCode: Int) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = resources.getColor(R.color.black_light)
        navigateToNextScreen()
    }

    private fun checkInternetConnection(dialog: DialogInterface?): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            promptInternetConnect()
            return false
        }
        dialog?.dismiss()
        return true
    }

    private fun promptInternetConnect() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_alert_no_intenet)
        builder.setMessage(R.string.msg_alert_no_internet)
        builder.setCancelable(false)
        val positiveText = getString(R.string.btn_label_refresh)
        builder.setPositiveButton(
            positiveText
        ) { dialog, _ -> //Block the Application Execution until user grants the permissions
            if (checkInternetConnection(dialog)) {
                checkRunTimePermission()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkRunTimePermission() {
        requestAppPermissions(
            arrayOf(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            ),
            R.string.runtime_permissions_txt,
            REQUEST_PERMISSIONS
        )
    }

    private fun init() {
        SharedPrefsHelper.init(this)
    }

    private fun navigateToNextScreen() {
        setContentView(R.layout.activity_splash)

        val agentId = UtilitySharedPreferences.getPrefs(
            applicationContext,
            "agent_id"
        )

        Handler().postDelayed({
            if (!agentId.isNullOrEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.apply {
                    startActivity(this)
                    finish()
                }
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.apply {
                    startActivity(this)
                    finish()
                }
            }
        }, 2000)
    }
}