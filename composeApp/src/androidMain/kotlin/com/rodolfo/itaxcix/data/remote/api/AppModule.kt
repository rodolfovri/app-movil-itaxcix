package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.ApiServiceImpl
import com.rodolfo.itaxcix.data.repository.UserRepositoryImpl
import com.rodolfo.itaxcix.domain.repository.UserRepository
import com.rodolfo.itaxcix.feature.auth.viewmodel.LoginViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RecoveryViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RegisterViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.ResetPasswordViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.RegisterDriverViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return ApiServiceImpl(ApiClient.create())
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepositoryImpl(apiService)
    }
}