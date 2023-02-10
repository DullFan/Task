package com.dullfan.login.common

import android.content.Context
import android.content.Intent
import com.dullfan.library_common.autoservice.LoginService
import com.dullfan.login.ui.activity.LoginActivity
import com.google.auto.service.AutoService

@AutoService(LoginService::class)
class LoginServiceImpl :LoginService{
    override fun startLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}