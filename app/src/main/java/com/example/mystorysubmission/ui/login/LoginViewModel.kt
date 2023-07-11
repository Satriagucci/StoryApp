package com.example.mystorysubmission.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.storage.User
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _emailValid = MutableLiveData<Boolean>()
    private val _passwordValid = MutableLiveData<Boolean>()
    private val _loginStatus = MutableLiveData<Boolean>()
    private val _loading = MutableLiveData<Boolean>()

    val emailValid: LiveData<Boolean> = _emailValid
    val passwordValid: LiveData<Boolean> = _passwordValid
    val loginStatus: LiveData<Boolean> = _loginStatus
    val errorMsg = MutableLiveData<String>()
    val loading: LiveData<Boolean> = _loading

    init {
        _emailValid.value = false
        _passwordValid.value = false
        _loginStatus.value = false
        errorMsg.value = ""
        _loading.value = false
    }

    fun updateEmailStatus(status: Boolean) {
        _emailValid.value = status
    }

    fun updatePasswordStatus(status: Boolean) {
        _passwordValid.value = status
    }

    fun uploadLoginData(email: String, password: String) {
        viewModelScope.launch {
            repository.setLogin(email, password)
        }
    }

    fun login(user: User) {
        viewModelScope.launch {
            repository.login(user)
        }
    }

    fun getUser(): LiveData<User> {
        return repository.getUser()
    }

}