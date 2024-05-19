package com.ifs21004.lostandfound.data.remote.response

import com.google.gson.annotations.SerializedName

data class DelcomAddLostandFoundResponse(

    @field:SerializedName("data")
    val data: DataAddLostandFoundResponse,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String
)

data class DataAddLostandFoundResponse(

    @field:SerializedName("lost_found_id")
    val lostFoundId: Int
)
