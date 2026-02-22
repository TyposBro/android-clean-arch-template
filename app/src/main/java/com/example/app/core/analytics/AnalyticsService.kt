package com.example.app.core.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(key: String, value: String)
    fun setUserId(userId: String?)
}

@Singleton
class NoOpAnalyticsService @Inject constructor() : AnalyticsService {
    override fun logEvent(name: String, params: Map<String, Any>) {
        // No-op
    }

    override fun setUserProperty(key: String, value: String) {
        // No-op
    }

    override fun setUserId(userId: String?) {
        // No-op
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: NoOpAnalyticsService): AnalyticsService
}
