package com.dullfan.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.LCUser
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.utils.myClickListener
import com.dullfan.login.databinding.FragmentLoginBinding
import com.dullfan.login.ui.activity.LoginActivity
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


class LoginFragment : BaseFragment() {

    private val viewDataBinding by lazy {
        FragmentLoginBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 跳转忘记密码页面
        viewDataBinding.loginForgotPassword.setOnClickListener {
            (activity as LoginActivity).startForgotPasswordFragment()
        }

        // 跳转注册页面
        viewDataBinding.loginSignUp.setOnClickListener {
            (activity as LoginActivity).startSignUpFragment()
        }

        viewDataBinding.loginSubmit.setOnClickListener(myClickListener {
            val email = viewDataBinding.loginEmailEdit.text.toString()
            val password = viewDataBinding.loginPasswordEdit.text.toString()

            // 判断是否邮箱和密码是否为空
            if (email.isBlank() || password.isBlank()) {
                showAlerter(text = "请输入邮箱或密码", color = com.dullfan.base.R.color.red)
                return@myClickListener
            }


            // 使用LeanCloud提供的LCUser判断邮箱和密码是否正确
            LCUser.logIn(email, password).subscribe(object : Observer<LCUser> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: LCUser) {
                    // 正确就跳转主页
                    (activity as LoginActivity).startMainActivity()
                    showToast("登录成功")
                }

                override fun onError(e: Throwable) {
                    // 错误的话就提醒用户输入错误
                    e.message?.let { showAlerter(text = it, color = com.dullfan.base.R.color.red) }
                }

                override fun onComplete() {

                }
            })
        })
        return viewDataBinding.root
    }
}