package com.dullfan.module_main.bean

data class UserInfoBean(
    val code: Int,
    val `data`: UserInfoData,
    val msg: String
)

data class UserInfoData(
    val avatar: String,
    val email: String,
    val id: Int,
    val join_the_project_id: String,
    val managed_projects_id: String,
    val phone_number: String,
    val profession: String,
    val register_time: String,
    val username: String
)