package com.quaterfoldvendorapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quaterfoldvendorapp.MainActivity
import com.quaterfoldvendorapp.data.Agentinfo
import com.quaterfoldvendorapp.data.LoginRequest
import com.quaterfoldvendorapp.data.Resource
import com.quaterfoldvendorapp.databinding.LoginActivityBinding
import com.quaterfoldvendorapp.domain.ApiViewModel
import com.quaterfoldvendorapp.sharedpreference.SharedPrefManager
import com.quaterfoldvendorapp.sharedpreference.UtilitySharedPreferences
import com.quaterfoldvendorapp.utils.DeviceInfoUtils
import com.quaterfoldvendorapp.utils.getStringFromEditText
import com.quaterfoldvendorapp.utils.isInteger
import com.quaterfoldvendorapp.utils.md5
import es.dmoral.toasty.Toasty
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var progressDialog: ProgressDialog
    private var IMEI: String? = null
    private val KEY_MOBILE_NO = "username"
    private val KEY_PASSWORD = "password"
    private val viewModel: ApiViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)

        IMEI = DeviceInfoUtils.getIMEI(this)

        attachObserver()
        initViews()
    }

    private fun attachObserver() {
        viewModel.loginResponse.observe(this) { response ->
            if (response != null) {
                when (response.status) {
                    Resource.Status.SUCCESS -> {
                        progressDialog.dismiss()
                        //Toasty.info(this, "--->>>>> "+ response.data?.status).show()
                        if (response.data?.status == "valid") {
                            val responseData = response.data.data

                            val agentInfo = Agentinfo()
                            agentInfo.setId(responseData?.id)
                            agentInfo.setVendor_code(responseData?.vendor_code)
                            agentInfo.setUsername(responseData?.username)
                            agentInfo.setCompany_name(responseData?.company_name)
                            agentInfo.setEmail(responseData?.email)
                            agentInfo.setMobile(responseData?.mobile)
                            agentInfo.setContact(responseData?.contact)
                            agentInfo.setAddress_line_1(responseData?.address_line_1)
                            agentInfo.setAddress_line_2(responseData?.address_line_2)
                            agentInfo.setCity(responseData?.city)
                            agentInfo.setState(responseData?.state)
                            agentInfo.setPincode(responseData?.pincode)
                            agentInfo.setWorking_cities(responseData?.working_cities)
                            agentInfo.setImei_no(responseData?.imei_no)

                            SharedPrefManager.getInstance(this).agentLogin(agentInfo)
                            UtilitySharedPreferences.setPrefs(this, "agent_id", responseData?.id)
                            Toasty.success(this, "Login Successful", Toast.LENGTH_SHORT, true)
                                .show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.apply {
                                startActivity(this)
                                finish()
                            }
                        } else {
                            progressDialog.dismiss()
                            Toasty.error(
                                this,
                                "You have entered an invalid username or password",
                                Toast.LENGTH_SHORT,
                                false
                            ).show()
                        }
                    }
                    Resource.Status.LOADING -> {
                        progressDialog.show()
                    }
                    Resource.Status.ERROR -> {
                        progressDialog.dismiss()
                        response.message?.let { it1 -> Toasty.error(this, it1) }
                    }
                }
            }
        }

    }

    private fun initViews() {
        //binding.editPhone.setText("8299842470")
        //binding.editPassword.setText("Pawa@2470")

        binding.btnLogin.setOnClickListener {
            if (isValid()) {
                signIn()
            }
        }
    }

    private fun signIn() {

        //dialog.show();
        val mobileNo = getStringFromEditText(binding.editPhone)
        val password = getStringFromEditText(binding.editPassword)

        val userData = LoginRequest()
        userData.username = mobileNo
        userData.password = md5(password)
        //userData.imei = IMEI
        viewModel.userLogin(userData)
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