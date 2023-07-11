package com.example.mystorysubmission.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.mystorysubmission.model.ListStoryItem
import com.example.mystorysubmission.storage.User
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.example.mystorysubmission.data.StoryRepository

class MainViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val loading: LiveData<Boolean> = _loading

    init {
        errorMessage.value = ""
        _loading.value = false
    }

    fun storyPaging(): LiveData<PagingData<ListStoryItem>> {
        return repository.getStories().cachedIn(viewModelScope)
    }

    fun getUser(): LiveData<User> {
        return repository.getUser()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}