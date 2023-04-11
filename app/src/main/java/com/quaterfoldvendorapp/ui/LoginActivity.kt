package com.quaterfoldvendorapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.quaterfoldvendorapp.MainActivity
import com.quaterfoldvendorapp.data.Agentinfo
import com.quaterfoldvendorapp.databinding.LoginActivityBinding
import com.quaterfoldvendorapp.sharedpreference.SharedPrefManager
import com.quaterfoldvendorapp.sharedpreference.UtilitySharedPreferences
import com.quaterfoldvendorapp.utils.SharedPrefConstant
import com.quaterfoldvendorapp.utils.getStringFromEditText
import com.quaterfoldvendorapp.utils.isInteger
import com.quaterfoldvendorapp.utils.md5
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    var progressDialog: ProgressDialog? = null
    var StrIMEI1 = ""
    var StrIMEI2: String? = ""
    var StrIMEI: String? = ""
    var StrCellInfo: String? = ""

    var IMEI: String? = null
    var mTelephonyManager: TelephonyManager? = null

    private val KEY_MOBILE_NO = "username"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please Wait")
        progressDialog!!.setCancelable(false)

        mTelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (mTelephonyManager != null) {
            IMEI = getDeviceID()
        }

        initViews()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(): String? {
        var m_szUniqueID = ""
        /*String Return_DeviceID = USERNAME_and_PASSWORD.getString(DeviceID_key,"Guest");
        return Return_DeviceID;*/
        val telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val simState = telMgr.simState
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val simCount = telMgr.phoneCount
            if (simCount == 2) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return null
                }
                val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            StrIMEI1 = telephonyManager.getImei(0)
                            StrIMEI2 = telephonyManager.getImei(1)
                        }
                    } else {
                        StrIMEI = Settings.Secure.getString(
                            contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                    }
                }
            } else {
                StrIMEI = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            }
            // 2 compute DEVICE ID
            if (StrIMEI != null && !StrIMEI.equals("", ignoreCase = true) && !StrIMEI.equals(
                    "null",
                    ignoreCase = true
                )
            ) {
                m_szUniqueID = StrIMEI as String
            } else if (!StrIMEI1.equals(
                    "",
                    ignoreCase = true
                ) && !StrIMEI1.equals("null", ignoreCase = true)
            ) {
                m_szUniqueID = StrIMEI1
            } else if (StrIMEI2 != null && !StrIMEI2.equals(
                    "",
                    ignoreCase = true
                ) && !StrIMEI2.equals("null", ignoreCase = true)
            ) {
                m_szUniqueID = StrIMEI2 as String
            } else {
                val m_szDevIDShort = "35" + // we make this look like a valid IMEI
                        Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10 // 13 digits
                val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                @SuppressLint("HardwareIds") val m_szWLANMAC =
                    if (wm != null) wm.connectionInfo.macAddress else ""
                // 5 Bluetooth MAC address android.permission.BLUETOOTH required
                var m_BluetoothAdapter: BluetoothAdapter? = null // Local Bluetooth adapter
                m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                @SuppressLint("HardwareIds") val m_szBTMAC = m_BluetoothAdapter.address
                println("m_szBTMAC $m_szBTMAC")
                if (m_szWLANMAC != null && !m_szWLANMAC.equals(
                        "",
                        ignoreCase = true
                    ) && !m_szWLANMAC.equals("null", ignoreCase = true)
                ) {
                    m_szUniqueID = m_szWLANMAC
                } else if (m_szBTMAC != null && !m_szBTMAC.equals(
                        "",
                        ignoreCase = true
                    ) && !m_szBTMAC.equals("null", ignoreCase = true)
                ) {
                    m_szUniqueID = m_szBTMAC
                }
            }
        } else {
            StrIMEI = telMgr.deviceId
            m_szUniqueID = StrIMEI.toString()
        }
        Log.i("--DeviceID--", m_szUniqueID)
        Log.d("DeviceIdCheck", "DeviceId that generated MPreferenceActivity:$m_szUniqueID")
        return m_szUniqueID
    }

    private fun initViews() {
        binding.btnLogin.setOnClickListener {
            if (isValid()) {
                signIn()
            }
        }
    }

    private fun signIn() {

        //dialog.show();
        progressDialog?.show()
        val mobileNo = getStringFromEditText(binding.editPhone)
        val password = getStringFromEditText(binding.editPassword)
        Log.d("LOGIN_URL", "" + SharedPrefConstant.LOGIN_URL)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, SharedPrefConstant.LOGIN_URL,
            Response.Listener { response ->
                progressDialog?.dismiss()
                try {
                    Log.d("Response", "" + response)
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getString("status")
                    if ("valid".equals(status, ignoreCase = true)) {
                        val pos_info_obj = jsonObject.getJSONObject("data")

                        val agentInfo = Agentinfo()
                        agentInfo.setId(pos_info_obj.getString("id"))
                        agentInfo.setVendor_code(pos_info_obj.getString("vendor_code"))
                        agentInfo.setUsername(pos_info_obj.getString("username"))
                        agentInfo.setCompany_name(pos_info_obj.getString("company_name"))
                        agentInfo.setEmail(pos_info_obj.getString("email"))
                        agentInfo.setMobile(pos_info_obj.getString("mobile"))
                        agentInfo.setContact(pos_info_obj.getString("contact"))
                        agentInfo.setAddress_line_1(pos_info_obj.getString("address_line_1"))
                        agentInfo.setAddress_line_2(pos_info_obj.getString("address_line_2"))
                        agentInfo.setCity(pos_info_obj.getString("city"))
                        agentInfo.setState(pos_info_obj.getString("state"))
                        agentInfo.setPincode(pos_info_obj.getString("pincode"))
                        agentInfo.setWorking_cities(pos_info_obj.getString("working_cities"))
                        agentInfo.setImei_no(pos_info_obj.getString("imei_no"))

                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(applicationContext).agentLogin(agentInfo)
                        UtilitySharedPreferences.setPrefs(
                            applicationContext,
                            "agent_id",
                            pos_info_obj.getString("id")
                        )
                        Toasty.success(this, "Login Successful", Toast.LENGTH_SHORT, true)
                            .show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.apply {
                            startActivity(this)
                            finish()
                        }
                    } else if ("invalid".equals(status, ignoreCase = true)) {
                        Toasty.error(
                            this,
                            "You have entered an invalid username or password",
                            Toast.LENGTH_SHORT,
                            false
                        ).show()
                        return@Listener
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (progressDialog?.isShowing!!) {
                        progressDialog?.dismiss()
                    }
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                if (progressDialog?.isShowing!!) {
                    progressDialog?.dismiss()
                }
                //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }) {
            override fun getParams(): Map<String, String>? {
                val map: MutableMap<String, String> = HashMap()
                map[KEY_MOBILE_NO] = mobileNo
                val md5Password = md5(password)
                if (!md5Password.isNullOrEmpty()) {
                    map[KEY_PASSWORD] = md5Password
                }

                if (!IMEI.isNullOrEmpty()) {
                    map["imei"] = IMEI.toString()
                }
                Log.d("LoginAPI", "posting params: $map")
                return map
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    private fun isValid(): Boolean {
        val mobileNo = getStringFromEditText(binding.editPhone)
        if (mobileNo.isEmpty() || isInteger(mobileNo)) {
            return false
        }

        if (getStringFromEditText(binding.editPassword).isEmpty()) {
            return false
        }
        return true
    }
}