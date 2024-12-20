package com.example.newsapiclient.presentation.di

import com.example.newsapiclient.data.api.NewsAPIService
import com.example.newsapiclient.data.repository.dataSource.NewsRemoteDataSource
import com.example.newsapiclient.data.repository.dataSourceImpl.NewsRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteDataModule {

    @Singleton
    @Provides
    fun provideNewsRemoteDataSource(newsApiService: NewsAPIService): NewsRemoteDataSource {
        return NewsRemoteDataSourceImpl(newsApiService)
    }
}