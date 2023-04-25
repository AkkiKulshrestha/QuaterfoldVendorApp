package com.quaterfoldvendorapp.interfaces

import com.quaterfoldvendorapp.data.*
import com.quaterfoldvendorapp.utils.AppConstant
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiRepository {

    @POST(AppConstant.LOGIN_URL)
    suspend fun userLogin(@Body body: LoginRequest): Response<LoginModel>

    @POST(AppConstant.ASSIGNMENT_LIST_URL)
    suspend fun getAssignmentList(@Body body: AssignmentRequest): Response<AssignmentModel>

    @POST(AppConstant.UPLOAD_IMAGE_URL)
    suspend fun saveAssignmentImages(@Body body: AssignmentSaveRequest): Response<AssignmentModel>
}