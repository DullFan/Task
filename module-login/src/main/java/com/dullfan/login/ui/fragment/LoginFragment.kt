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

        viewDataBinding.loginForgotPassword.setOnClickListener {
            (activity as LoginActivity).startForgotPasswordFragment()
        }

        viewDataBinding.loginSignUp.setOnClickListener {
            (activity as LoginActivity).startSignUpFragment()
        }

        viewDataBinding.loginSubmit.setOnClickListener(myClickListener {
            val email = viewDataBinding.loginEmailEdit.text.toString()
            val password = viewDataBinding.loginPasswordEdit.text.toString()
            if (email.isBlank() || password.isBlank()) {
                showAlerter(text = "请输入邮箱或密码", color = com.dullfan.base.R.color.red)
                return@myClickListener
            }

            LCUser.logIn(email, password).subscribe(object : Observer<LCUser> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: LCUser) {
                    (activity as LoginActivity).startMainActivity()
                    showToast("登录成功")
                }

                override fun onError(e: Throwable) {
                    e.message?.let { showAlerter(text = it, color = com.dullfan.base.R.color.red) }
                }

                override fun onComplete() {

                }
            })
        })
        return viewDataBinding.root
    }
}