package com.example.mystorysubmission.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.database.StoryDatabase
import com.example.mystorysubmission.network.ApiConfig
import com.example.mystorysubmission.storage.UserPreference

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val preferences = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        val database = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(preferences, apiService, database)
    }
}