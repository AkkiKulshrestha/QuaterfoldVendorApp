package com.quaterfoldvendorapp.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quaterfoldvendorapp.data.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class ApiViewModel constructor(
    private val apiUseCase: ApiUseCase
) : ViewModel() {

    private val _loginResponse = MutableLiveData<Resource<LoginModel>>()
    val loginResponse: LiveData<Resource<LoginModel>> get() = _loginResponse

    private val _assignmentResponse = MutableLiveData<Resource<AssignmentModel>>()
    val assignmentResponse: LiveData<Resource<AssignmentModel>> get() = _assignmentResponse

    private val _assignmentSaveResponse = MutableLiveData<Resource<AssignmentModel>>()
    val assignmentSaveResponse: LiveData<Resource<AssignmentModel>> get() = _assignmentSaveResponse

    val showProgressbar = MutableLiveData<Boolean>()
    val messageData = MutableLiveData<String>()

    fun userLogin(partMap: LoginRequest) {
        showProgressbar.value = true
        _loginResponse.postValue(Resource.loading())
        viewModelScope.launch {
            apiUseCase.userLogin(partMap).let {
                if (it.isSuccessful) {
                    _loginResponse.postValue(Resource.success(it.body()))
                } else {
                    val jsonObj = it.errorBody()?.charStream()?.readText()
                        ?.let { it1 -> JSONObject(it1) }
                    val message = "Login Failed"
                    _loginResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
                }
            }
        }
    }

    fun getAssignmentList(partMap: AssignmentRequest) {
        showProgressbar.value = true
        _assignmentResponse.postValue(Resource.loading())
        viewModelScope.launch {
            apiUseCase.getAssignmentList(partMap).let {
                if (it.isSuccessful) {
                    _assignmentResponse.postValue(Resource.success(it.body()))
                } else {
                    val jsonObj = it.errorBody()?.charStream()?.readText()
                        ?.let { it1 -> JSONObject(it1) }
                    val message = "Failed to get data. Please try again later"
                    _assignmentResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
                }
            }
        }
    }

    fun saveAssignmentImages(partMap: AssignmentSaveRequest) {
        showProgressbar.value = true
        _assignmentSaveResponse.postValue(Resource.loading())
        viewModelScope.launch {
            apiUseCase.saveAssignmentImages(partMap).let {
                if (it.isSuccessful) {
                    _assignmentSaveResponse.postValue(Resource.success(it.body()))
                } else {
                    val jsonObj = it.errorBody()?.charStream()?.readText()
                        ?.let { it1 -> JSONObject(it1) }
                    val message = "Failed to get data. Please try again later"
                    _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
                }
            }
        }
    }

}