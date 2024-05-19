package com.ifs21004.lostandfound.data.remote.response

import com.google.gson.annotations.SerializedName

data class DelcomLostandFoundResponse(

    @field:SerializedName("data")
    val data: DataLostandFoundResponse,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String
)

data class DataLostandFoundResponse(

    @field:SerializedName("lost_found")
    val lostFound: LostandFoundResponse
)

data class AuthorLostandFoundResponse(

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("photo")
    val photo: String?,
)

data class LostandFoundResponse(

    @field:SerializedName("cover")
    val cover: String?,

    @field:SerializedName("updated_at")
    val updatedAt: String,

    @field:SerializedName("user_id")
    val userId: Int,

    @field:SerializedName("author")
    val author: AuthorLostandFoundResponse,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("created_at")
    val createdAt: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("is_completed")
    val isCompleted: Int,

    @field:SerializedName("status")
    val status: String
)
