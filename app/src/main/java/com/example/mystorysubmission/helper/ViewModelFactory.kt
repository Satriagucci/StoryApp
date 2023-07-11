package com.example.mystorysubmission.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.di.Injection
import com.example.mystorysubmission.ui.add.AddViewModel
import com.example.mystorysubmission.ui.login.LoginViewModel
import com.example.mystorysubmission.ui.main.MainViewModel
import com.example.mystorysubmission.ui.maps.MapsViewModel

class ViewModelFactory(private val repo: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repo) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repo) as T
            modelClass.isAssignableFrom(AddViewModel::class.java) -> AddViewModel(repo) as T
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> MapsViewModel(repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(Injection.provideRepository(context))
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}