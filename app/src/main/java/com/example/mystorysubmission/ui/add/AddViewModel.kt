package com.example.mystorysubmission.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.storage.User

class AddViewModel(private val repository: StoryRepository) : ViewModel() {
    var token = ""
    val loading = MutableLiveData<Boolean>()

    init {
        loading.value = false
    }

    fun getUser(): LiveData<User> {
        return repository.getUser()
    }
}