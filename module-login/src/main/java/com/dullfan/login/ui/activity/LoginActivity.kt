package com.dullfan.login.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.Navigation
import cn.leancloud.LCUser
import com.dullfan.base.base.BaseActivity
import com.dullfan.base.utils.KV
import com.dullfan.base.utils.MMKVData
import com.dullfan.library_common.autoservice.MainService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.dullfan.login.R
import com.dullfan.login.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private val viewBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val findNavController by lazy {
        Navigation.findNavController(this, R.id.login_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(LCUser.getCurrentUser() != null){
            startMainActivity()
            overridePendingTransition(0,0)
            return
        }
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        supportActionBar?.hide()
    }

    fun startNavigateUp() {
        findNavController.navigateUp()
    }

    // 跳转忘记密码
    fun startForgotPasswordFragment() {
        findNavController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    // 跳转注册
    fun startSignUpFragment() {
        findNavController.navigate(R.id.action_loginFragment_to_signUpFragment)
    }

    fun startMainActivity(){
        val load = CommonServiceLoader.load(MainService::class.java)
        finish()
        load?.startMainActivity(this)
    }
}