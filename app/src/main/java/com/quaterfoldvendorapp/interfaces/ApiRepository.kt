package com.quaterfoldvendorapp.interfaces

import com.quaterfoldvendorapp.data.*
import com.quaterfoldvendorapp.utils.AppConstant
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiRepository {

    @POST(AppConstant.LOGIN_URL)
    suspend fun userLogin(@Body body: LoginRequest): Response<LoginModel>

    @POST(AppConstant.ASSIGNMENT_LIST_URL)
    suspend fun getAssignmentList(@Body body: AssignmentRequest): Response<AssignmentModel>

    @POST(AppConstant.UPLOAD_IMAGE_URL)
    suspend fun saveAssignmentImages(@Body body: AssignmentSaveRequest): Response<AssignmentModel>

    @Multipart
    @POST(AppConstant.UPLOAD_IMAGE_NEW_URL)
    suspend fun uploadAssignmentImages(
        @Part("images") images: String?,
        @Part file1: MultipartBody.Part?,
        @Part file2: MultipartBody.Part?,
        @Part file3: MultipartBody.Part?,
        @Part file4: MultipartBody.Part?,
        @Part file5: MultipartBody.Part?,
        @Part file6: MultipartBody.Part?,
    ): Response<ImageUploadResponseBody?>
}