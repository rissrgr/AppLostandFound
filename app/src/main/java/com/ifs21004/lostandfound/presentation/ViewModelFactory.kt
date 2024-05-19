package com.ifs21004.lostandfound.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ifs21004.lostandfound.di.Injection
import com.ifs21004.lostandfound.presentation.login.LoginViewModel
import com.ifs21004.lostandfound.presentation.lostandfound.LostandFoundViewModel
import com.ifs21004.lostandfound.presentation.main.MainViewModel
import com.ifs21004.lostandfound.presentation.profile.ProfileViewModel
import com.ifs21004.lostandfound.presentation.register.RegisterViewModel
import com.ifs21004.lostandfound.data.repository.AuthRepository
import com.ifs21004.lostandfound.data.repository.LocalLostandFoundRepository
import com.ifs21004.lostandfound.data.repository.LostandFoundRepository
import com.ifs21004.lostandfound.data.repository.UserRepository

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lostandfoundRepository: LostandFoundRepository,
    private val localLostandFoundRepository: LocalLostandFoundRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel.getInstance(authRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel.getInstance(authRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel.getInstance(authRepository, lostandfoundRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel.getInstance(authRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(LostandFoundViewModel::class.java) -> {
                LostandFoundViewModel
                    .getInstance(lostandfoundRepository, localLostandFoundRepository) as T
            }
            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.name
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = ViewModelFactory(
                    Injection.provideAuthRepository(context),
                    Injection.provideUserRepository(context),
                    Injection.provideLostandFoundRepository(context),
                    Injection.provideLocalLostandFoundRepository(context),
                )
            }
            return INSTANCE as ViewModelFactory
        }
    }
}