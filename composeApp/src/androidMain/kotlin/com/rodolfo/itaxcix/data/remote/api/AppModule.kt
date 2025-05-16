package com.rodolfo.itaxcix.data.remote.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.ApiServiceImpl
import com.rodolfo.itaxcix.data.repository.DriverRepositoryImpl
import com.rodolfo.itaxcix.data.repository.UserRepositoryImpl
import com.rodolfo.itaxcix.domain.repository.DriverRepository
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
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return ApiClient.create()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("user_preferences") }
        )
    }

    @Provides
    @Singleton
    fun providePreferencesManager(dataStore: DataStore<Preferences>): PreferencesManager {
        return PreferencesManager(dataStore)
    }

    @Provides
    @Singleton
    fun provideApiService(
        client: HttpClient,
        preferencesManager: PreferencesManager
    ): ApiService {
        return ApiServiceImpl(client, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideDriverRepository(apiService: ApiService): DriverRepository {
        return DriverRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        driverRepository: DriverRepository,
        preferencesManager: PreferencesManager
    ): UserRepository {
        return UserRepositoryImpl(apiService, driverRepository, preferencesManager)
    }
}