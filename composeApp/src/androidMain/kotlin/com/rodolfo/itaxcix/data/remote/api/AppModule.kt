package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.ApiServiceImpl
import com.rodolfo.itaxcix.data.repository.UserRepositoryImpl
import com.rodolfo.itaxcix.domain.repository.UserRepository
import com.rodolfo.itaxcix.feature.auth.viewmodel.LoginViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RegisterViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.RegisterDriverViewModel

object AppModule {
    private val apiService: ApiService by lazy {
        ApiServiceImpl(ApiClient.create())
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(apiService)
    }

    fun provideRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel(userRepository)
    }

    fun provideRegisterDriverViewModel(): RegisterDriverViewModel {
        return RegisterDriverViewModel(userRepository)
    }

    fun provideLoginViewModel(): LoginViewModel {
        return LoginViewModel(userRepository)
    }
}