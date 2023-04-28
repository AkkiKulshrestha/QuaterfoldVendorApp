package com.quaterfoldvendorapp.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quaterfoldvendorapp.data.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.Part
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiViewModel constructor(
    private val apiUseCase: ApiUseCase
) : ViewModel() {

    private val _loginResponse = MutableLiveData<Resource<LoginModel>>()
    val loginResponse: LiveData<Resource<LoginModel>> get() = _loginResponse

    private val _assignmentResponse = MutableLiveData<Resource<AssignmentModel>>()
    val assignmentResponse: LiveData<Resource<AssignmentModel>> get() = _assignmentResponse

    private val _assignmentSaveResponse = MutableLiveData<Resource<AssignmentModel>>()
    val assignmentSaveResponse: LiveData<Resource<AssignmentModel>> get() = _assignmentSaveResponse

    private val _uploadImageResponse = MutableLiveData<Resource<ImageUploadResponseBody>>()
    val uploadImageResponse: LiveData<Resource<ImageUploadResponseBody>> get() = _uploadImageResponse

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
            try {
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
            }catch (se: SocketTimeoutException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to get data. Please try again later"
                _assignmentResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: UnknownHostException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to get data. Please try again later"
                _assignmentResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: ConnectException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to get data. Please try again later"
                _assignmentResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            } catch (ex: Throwable) {
                Log.e("TAG", "Error ${ex.message}")
                val message = "Failed to get data. Please try again later"
                _assignmentResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }
        }
    }

    fun saveAssignmentImages(partMap: AssignmentSaveRequest) {
        showProgressbar.value = true
        _assignmentSaveResponse.postValue(Resource.loading())
        viewModelScope.launch {
            try {
                apiUseCase.saveAssignmentImages(partMap).let {
                    if (it.isSuccessful) {
                        _assignmentSaveResponse.postValue(Resource.success(it.body()))
                    } else {
                        val jsonObj = it.errorBody()?.charStream()?.readText()
                            ?.let { it1 -> JSONObject(it1) }
                        val message = "Failed to upload data. Please try again later"
                        _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
                    }
                }
            } catch (se: SocketTimeoutException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload data. Please try again later"
                _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: UnknownHostException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload data. Please try again later"
                _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: ConnectException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload data. Please try again later"
                _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            } catch (ex: Throwable) {
                Log.e("TAG", "Error ${ex.message}")
                 val message = "Failed to upload data. Please try again later"
                _assignmentSaveResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }
        }
    }

    fun uploadAssignmentImages(@Part("images") images: String?,
                               @Part file1: MultipartBody.Part?,
                               @Part file2: MultipartBody.Part?,
                               @Part file3: MultipartBody.Part?,
                               @Part file4: MultipartBody.Part?,
                               @Part file5: MultipartBody.Part?,
                               @Part file6: MultipartBody.Part?) {
        showProgressbar.value = true
        _uploadImageResponse.postValue(Resource.loading())
        viewModelScope.launch {
            try {
                apiUseCase.uploadAssignmentImages(images, file1, file2, file3, file4, file5, file6).let {
                    if (it.isSuccessful) {
                        _uploadImageResponse.postValue(Resource.success(it.body()))
                    } else {
                        val jsonObj = it.errorBody()?.charStream()?.readText()
                            ?.let { it1 -> JSONObject(it1) }
                        val message = "Failed to upload images. Please try again later"
                        _uploadImageResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
                    }
                }
            } catch (se: SocketTimeoutException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload images. Please try again later"
                _uploadImageResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: UnknownHostException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload images. Please try again later"
                _uploadImageResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }  catch (se: ConnectException) {
                Log.e("TAG", "Error: ${se.message}")
                val message = "Failed to upload images. Please try again later"
                _uploadImageResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            } catch (ex: Throwable) {
                Log.e("TAG", "Error ${ex.message}")
                val message = "Failed to upload images. Please try again later"
                _uploadImageResponse.postValue(message.let { it1 -> Resource.error(it1, null) })
            }
        }
    }
}