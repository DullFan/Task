package com.dullfan.module_main.common

import android.content.Context
import android.content.Intent
import com.dullfan.library_common.autoservice.MainService
import com.dullfan.module_main.ui.MainActivity
import com.google.auto.service.AutoService

@AutoService(MainService::class)
class MainServiceImpl :MainService{
    override fun startMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }
}