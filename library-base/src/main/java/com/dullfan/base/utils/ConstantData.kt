package com.dullfan.base.utils

import android.Manifest

/**
 *  默认头像Id
 */
const val DEFAULT_AVATAR_ID = "631f51a031893156770c7d4b"

/**
 * 项目级别ID:低
 */
const val PROJECT_GRADE_LOW = "63034a76c4ae9e05acb36c4a"

/**
 * 项目级别ID:中
 */
const val PROJECT_GRADE_MIDDLE = "63034a6fc4ae9e05acb36c44"

/**
 * 项目级别ID:高
 */
const val PROJECT_GRADE_HIGH = "63034a67c4ae9e05acb36c36"

val permissionsReadAndWrite: Array<String> =
    arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
