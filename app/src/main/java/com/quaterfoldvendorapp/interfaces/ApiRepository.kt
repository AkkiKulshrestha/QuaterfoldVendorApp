package com.quaterfoldvendorapp.interfaces

import com.quaterfoldvendorapp.data.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiRepository {

    @POST("appLogin.php")
    suspend fun userLogin(@Body body: LoginRequest): Response<LoginModel>

    @POST("appGetListOfAssignment.php")
    suspend fun getAssignmentList(@Body body: AssignmentRequest): Response<AssignmentModel>

    @POST("appUploadAssignmentImages.php")
    suspend fun saveAssignmentImages(@Body body: AssignmentSaveRequest): Response<AssignmentModel>
}