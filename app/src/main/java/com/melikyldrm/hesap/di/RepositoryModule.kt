package com.melikyldrm.hesap.di

import com.melikyldrm.hesap.data.repository.ExchangeRepositoryImpl
import com.melikyldrm.hesap.data.repository.HistoryRepositoryImpl
import com.melikyldrm.hesap.domain.repository.ExchangeRepository
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRepository(
        exchangeRepositoryImpl: ExchangeRepositoryImpl
    ): ExchangeRepository
}

