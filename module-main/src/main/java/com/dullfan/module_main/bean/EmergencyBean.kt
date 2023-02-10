package com.dullfan.module_main.bean

data class EmergencyBean(
    val code: Int,
    val `data`: List<EmergencyBeanData>,
    val msg: String
)

data class EmergencyBeanData(
    val id: Int,
    val level_information: String
)