package com.quaterfoldvendorapp.data

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginModel(
    val status: String? = null,
    val data: VendorData? = null
)

@JsonClass(generateAdapter = true)
data class VendorData(
    val id: String? = null,
    val vendor_code: String? = null,
    val username: String? = null,
    val company_name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val user_type: String? = null,
    val contact: String? = null,
    val address_line_1: String? = null,
    val address_line_2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val working_cities: String? = null,
    val working_states: String? = null,
    val imei_no: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    var username: String? = null,
    var password: String? = null,
    var imei: String? = null
)

