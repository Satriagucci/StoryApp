package com.example.mystorysubmission.model

import com.google.gson.annotations.SerializedName

data class StoryUploadResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,
)