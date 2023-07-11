package com.example.mystorysubmission.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch
import androidx.paging.*
import com.example.mystorysubmission.database.StoryDatabase
import com.example.mystorysubmission.model.AuthenticationResponse
import com.example.mystorysubmission.model.ListStoryItem
import com.example.mystorysubmission.network.ApiConfig
import com.example.mystorysubmission.network.ApiService
import com.example.mystorysubmission.storage.User
import com.example.mystorysubmission.storage.UserPreference
import kotlinx.coroutines.GlobalScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryRepository(
    private val preference: UserPreference,
    private val apiService: ApiService,
    private val database: StoryDatabase
) {

    private val _loginStatus = MutableLiveData<Boolean>()
    private val _loading = MutableLiveData<Boolean>()

    val errorMsg = MutableLiveData<String>()
    val loading: LiveData<Boolean> = _loading

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(preference, apiService, database),
            pagingSourceFactory = {
                    database.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getUser(): LiveData<User> {
        return preference.getUser().asLiveData()
    }

    suspend fun login(user: User) {
        preference.login(user)
    }

    fun setLogin(email: String, password: String) {
        _loading.value = true

        val client = ApiConfig.getApiService().login(email, password)
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
                            errorMsg.value = ""
                            _loginStatus.value = true

                            GlobalScope.launch {
                                preference.login(
                                    User(
                                        result.loginResult?.name.toString(),
                                        email,
                                        password,
                                        result.loginResult?.token.toString()
                                    )
                                )
                            }

                        }
                    }
                } else {
                    Log.e(TAG, response.message())
                    errorMsg.value = response.message()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                Log.e(TAG, "Error message: ${t.message}")
                _loading.value = false
                errorMsg.value = t.message
            }
        })
    }

    suspend fun logout() {
        preference.logout()
    }


    fun dataState(): LiveData<User> {
        return preference.getUser().asLiveData()
    }

    companion object {
        private const val TAG = "StoryRepository"
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            preferences: UserPreference,
            apiService: ApiService,
            database: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(preferences, apiService, database)
            }.also { instance = it }
    }
}
