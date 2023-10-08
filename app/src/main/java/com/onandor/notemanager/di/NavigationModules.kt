package com.onandor.notemanager.di

import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationManagerModule {

    @Singleton
    @Provides
    fun provideNavigationManager(): INavigationManager = NavigationManager()
}