package com.dullfan.login.bean

data class LoginSuccessBean(
    val code: Int,
    val msg: String,
    val token: String
)