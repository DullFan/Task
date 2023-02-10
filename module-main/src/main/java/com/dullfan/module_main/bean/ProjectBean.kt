package com.dullfan.module_main.bean

data class ProjectBean(
    val code: Int,
    val `data`: List<ProjectData>,
    val msg: String
)

data class ProjectData(
    val id: Int,
    val project_avatar: String,
    val project_created_time: String,
    val project_creator_id: Int,
    val project_description: String,
    val project_member_id: String,
    val project_name: String,
    val project_priority_level_id: Int,
    val project_status_id: Int,
    val project_whether_public: String
)