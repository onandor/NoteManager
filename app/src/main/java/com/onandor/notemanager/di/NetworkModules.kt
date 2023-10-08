package com.onandor.notemanager.di

import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.remote.models.InvalidRefreshTokenException
import com.onandor.notemanager.data.remote.services.AuthApiService
import com.onandor.notemanager.data.remote.services.IAuthApiService
import com.onandor.notemanager.data.remote.sources.AuthDataSource
import com.onandor.notemanager.data.remote.sources.IAuthDataSource
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {

    @Singleton
    @Provides
    fun provideHttpClient(settings: ISettings, navManager: INavigationManager): HttpClient {
        return HttpClient(OkHttp) {
            expectSuccess = true
            defaultRequest {
                host = "10.0.2.2"
                port = 8443
                url { protocol = URLProtocol.HTTPS }
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                gson()
            }
            Auth {
                bearer {
                    loadTokens {
                        val accessToken = settings.getString(SettingsKeys.ACCESS_TOKEN)
                        val refreshToken = settings.getString(SettingsKeys.REFRESH_TOKEN)
                        BearerTokens(accessToken, refreshToken)
                    }
                    refreshTokens {
                        val refreshToken = settings.getString(SettingsKeys.REFRESH_TOKEN)
                        lateinit var tokenPair: TokenPair
                        try {
                            client.get {
                                markAsRefreshTokenRequest()
                                url("auth/refresh")
                                parameter("refreshToken", refreshToken)
                            }.body<TokenPair>()
                        } catch (e: ClientRequestException) {
                            settings.remove(SettingsKeys.USER_ID)
                            settings.remove(SettingsKeys.USER_EMAIL)
                            settings.remove(SettingsKeys.ACCESS_TOKEN)
                            settings.remove(SettingsKeys.REFRESH_TOKEN)
                            navManager.navigateTo(NavActions.signedOut())
                            throw InvalidRefreshTokenException()
                        }

                        settings.save(SettingsKeys.ACCESS_TOKEN, tokenPair.accessToken)
                        settings.save(SettingsKeys.REFRESH_TOKEN, tokenPair.refreshToken)
                        BearerTokens(tokenPair.accessToken, tokenPair.refreshToken)
                    }
                    sendWithoutRequest { request ->
                        !request.url.toString().contains("auth/login")
                                && !request.url.toString().contains("auth/register")
                    }
                }
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ApiServiceModule {

    @Binds
    abstract fun bindAuthApiService(authApiService: AuthApiService): IAuthApiService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindAuthDataSource(authDataSource: AuthDataSource): IAuthDataSource
}
