package com.quaterfoldvendorapp.domain

import com.quaterfoldvendorapp.data.AssignmentRequest
import com.quaterfoldvendorapp.data.AssignmentSaveRequest
import com.quaterfoldvendorapp.interfaces.ApiRepository
import com.quaterfoldvendorapp.data.LoginRequest
import com.quaterfoldvendorapp.interfaces.ApiService

class ApiUseCase constructor(
    private val apiRepository: ApiRepository
) {

    suspend fun userLogin(loginBody: LoginRequest) = apiRepository.userLogin(loginBody)

    suspend fun getAssignmentList(assignmentRequest: AssignmentRequest) = apiRepository.getAssignmentList(assignmentRequest)

    suspend fun saveAssignmentImages(assignmentSaveRequest: AssignmentSaveRequest) = apiRepository.saveAssignmentImages(assignmentSaveRequest)
}