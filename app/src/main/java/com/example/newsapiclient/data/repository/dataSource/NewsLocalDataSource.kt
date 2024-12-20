package com.example.newsapiclient.data.repository.dataSource

import com.example.newsapiclient.data.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsLocalDataSource {

    suspend fun saveArticleToDB(article: Article)
    fun getSavedArticles():Flow<List<Article>>
    suspend fun deleteArticleFromDB(article: Article)
}