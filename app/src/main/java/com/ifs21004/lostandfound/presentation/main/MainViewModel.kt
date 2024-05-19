package com.ifs21004.lostandfound.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ifs21004.lostandfound.data.pref.UserModel
import com.ifs21004.lostandfound.data.remote.MyResult
import com.ifs21004.lostandfound.data.remote.response.DelcomLostandFoundsResponse
import com.ifs21004.lostandfound.data.remote.response.DelcomResponse
import com.ifs21004.lostandfound.presentation.ViewModelFactory
import com.ifs21004.lostandfound.data.repository.AuthRepository
import com.ifs21004.lostandfound.data.repository.LostandFoundRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val lostandfoundRepository: LostandFoundRepository
) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return authRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun getLostandFounds(
        isCompleted: Int?,
        isMe: Int?,
        status: String?,
    ): LiveData<MyResult<DelcomLostandFoundsResponse>> {
        return lostandfoundRepository.getLostandFounds(isCompleted,
            isMe,
            status,).asLiveData()
    }

    fun getLostandFound(): LiveData<MyResult<DelcomLostandFoundsResponse>> {
        return lostandfoundRepository.getLostandFounds(null, 1, null).asLiveData()
    }

    fun putLostandFound(
        lostandfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostandfoundRepository.putLostandFound(
            lostandfoundId,
            title,
            description,
            status,
            isCompleted,
        ).asLiveData()
    }

    companion object {
        @Volatile
        private var INSTANCE: MainViewModel? = null

        fun getInstance(
            authRepository: AuthRepository,
            todoRepository: LostandFoundRepository
        ): MainViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = MainViewModel(
                    authRepository,
                    todoRepository
                )
            }
            return INSTANCE as MainViewModel
        }
    }
}

