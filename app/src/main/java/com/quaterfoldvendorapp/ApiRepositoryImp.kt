package com.quaterfoldvendorapp

import com.quaterfoldvendorapp.data.*
import com.quaterfoldvendorapp.interfaces.ApiRepository
import okhttp3.MultipartBody
import org.json.JSONArray
import retrofit2.Response
import retrofit2.http.Part

class ApiRepositoryImp(private val apiRepository: ApiRepository) : ApiRepository {

    override suspend fun userLogin(body: LoginRequest): Response<LoginModel> {
        return apiRepository.userLogin(body)
    }

    override suspend fun getAssignmentList(assignmentRequest: AssignmentRequest): Response<AssignmentModel> {
        return apiRepository.getAssignmentList(assignmentRequest)
    }

    override suspend fun saveAssignmentImages(assignmentSaveRequest: AssignmentSaveRequest): Response<AssignmentModel> {
        return apiRepository.saveAssignmentImages(assignmentSaveRequest)
    }

    override suspend fun uploadAssignmentImages(
        @Part("images") images: String?,
        @Part file1: MultipartBody.Part?,
        @Part file2: MultipartBody.Part?,
        @Part file3: MultipartBody.Part?,
        @Part file4: MultipartBody.Part?,
        @Part file5: MultipartBody.Part?,
        @Part file6: MultipartBody.Part?
    ): Response<ImageUploadResponseBody?> {
        return apiRepository.uploadAssignmentImages(
            images,
            file1,
            file2,
            file3,
            file4,
            file5,
            file6
        )
    }
}