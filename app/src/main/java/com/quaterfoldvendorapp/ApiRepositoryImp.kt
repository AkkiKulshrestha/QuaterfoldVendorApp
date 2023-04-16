package com.quaterfoldvendorapp

import com.quaterfoldvendorapp.data.*
import com.quaterfoldvendorapp.interfaces.ApiRepository
import com.quaterfoldvendorapp.interfaces.ApiService
import retrofit2.Response

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
}