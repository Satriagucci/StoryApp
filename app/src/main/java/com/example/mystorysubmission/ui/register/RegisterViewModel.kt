package com.example.mystorysubmission.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystorysubmission.model.AuthenticationResponse
import com.example.mystorysubmission.network.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _emailValid = MutableLiveData<Boolean>()
    private val _passwordValid = MutableLiveData<Boolean>()
    private val _loading = MutableLiveData<Boolean>()
    private val _statusMessage = MutableLiveData<String>()

    val emailValid: LiveData<Boolean> = _emailValid
    val passwordValid: LiveData<Boolean> = _passwordValid
    val statusMessage: LiveData<String> = _statusMessage
    val loading: LiveData<Boolean> = _loading

    init {
        _emailValid.value = false
        _passwordValid.value = false
        _statusMessage.value = ""
        _loading.value = false
    }

    fun updateEmailStatus(status: Boolean) {
        _emailValid.value = status
    }

    fun updatePasswordStatus(status: Boolean) {
        _passwordValid.value = status
    }

    fun register(name: String, email: String, password: String) {
        _loading.value = true

        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                _loading.value = false

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null) {
                        if (!result.error) {
                            _statusMessage.value = "success"
                        } else {
                            _statusMessage.value = result.message
                        }
                    }
                } else {
                    Log.e(TAG, response.message())
                    _statusMessage.value = response.message()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                Log.e(TAG, "Error message: ${t.message}")
                _loading.value = false
                _statusMessage.value = t.message
            }
        })
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}