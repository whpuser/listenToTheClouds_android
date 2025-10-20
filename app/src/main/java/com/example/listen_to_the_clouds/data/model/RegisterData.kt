package com.example.listen_to_the_clouds.data.model

data class RegisterData(
    val mailbox:String,
    val otpCode:String,
    val name:String,
    val password:String
)
