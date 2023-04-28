package com.quaterfoldvendorapp.domain

import com.quaterfoldvendorapp.data.AssignmentRequest
import com.quaterfoldvendorapp.data.AssignmentSaveRequest
import com.quaterfoldvendorapp.data.LoginRequest
import com.quaterfoldvendorapp.interfaces.ApiRepository
import okhttp3.MultipartBody
import org.json.JSONArray
import retrofit2.http.Part

class ApiUseCase constructor(
    private val apiRepository: ApiRepository
) {

    suspend fun userLogin(loginBody: LoginRequest) = apiRepository.userLogin(loginBody)

    suspend fun getAssignmentList(assignmentRequest: AssignmentRequest) =
        apiRepository.getAssignmentList(assignmentRequest)

    suspend fun saveAssignmentImages(assignmentSaveRequest: AssignmentSaveRequest) =
        apiRepository.saveAssignmentImages(assignmentSaveRequest)

    suspend fun uploadAssignmentImages(
        @Part("images") images: String?,
        @Part file1: MultipartBody.Part?,
        @Part file2: MultipartBody.Part?,
        @Part file3: MultipartBody.Part?,
        @Part file4: MultipartBody.Part?,
        @Part file5: MultipartBody.Part?,
        @Part file6: MultipartBody.Part?
    ) = apiRepository.uploadAssignmentImages(images, file1, file2, file3, file4, file5, file6)

}