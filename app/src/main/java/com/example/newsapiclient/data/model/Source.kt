package com.example.newsapiclient.data.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class Source(
    @PrimaryKey(autoGenerate = true)
    val id: String? = null,
    val name: String
): Parcelable