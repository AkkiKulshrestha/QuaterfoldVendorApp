package com.quaterfoldvendorapp.interfaces

import com.quaterfoldvendorapp.data.LoginModel
import com.quaterfoldvendorapp.data.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("appLogin.php")
    suspend fun userLogin(@Body body: LoginRequest): Response<LoginModel>
}