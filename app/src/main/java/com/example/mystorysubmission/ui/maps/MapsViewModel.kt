package com.example.mystorysubmission.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.model.ListStoryItem
import com.example.mystorysubmission.model.StoryResponse
import com.example.mystorysubmission.network.ApiConfig
import com.example.mystorysubmission.storage.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val repository : StoryRepository) : ViewModel() {

    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    private val _messageError = MutableLiveData<String>()
    val messageError: LiveData<String> = _messageError

    private val client = ApiConfig.getApiService()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getAllStoryLocation(userToken: String) {
        _isLoading.value = true
        client.getLocation("Bearer $userToken", 1).enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                _isLoading.value = false
                _messageError.value = response.body()?.message
                if (response.isSuccessful){
                    _stories.value = response.body()?.listStory
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _isLoading.value =false
                _messageError.value = t.message
            }
        })
    }

    fun dataState(): LiveData<User> {
        return repository.dataState()
    }
}