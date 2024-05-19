package com.ifs21004.lostandfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DelcomLostandFound(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    var isCompleted: Boolean,
    val cover: String?
) : Parcelable
