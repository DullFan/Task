package com.dullfan.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.LCUser
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.utils.DEFAULT_AVATAR_ID
import com.dullfan.base.utils.myClickListener
import com.dullfan.base.utils.showLog
import com.dullfan.library_common.utils.viewModels
import com.dullfan.login.databinding.FragmentSignUpBinding
import com.dullfan.login.ui.activity.LoginActivity
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


class SignUpFragment : BaseFragment() {

    private val viewDataBinding by lazy {
        FragmentSignUpBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewDataBinding.signUpForgotLogin.setOnClickListener {
            (activity as LoginActivity).startNavigateUp()
        }

        viewDataBinding.signUpSubmit.setOnClickListener(myClickListener {
            val userNameEdit = viewDataBinding.signUpUsernameEdit.text.toString()
            val passwordEdit = viewDataBinding.signUpPasswordEdit.text.toString()
            val emailEdit = viewDataBinding.signUpEmailEdit.text.toString()

            if (userNameEdit.isBlank() || passwordEdit.isBlank() || emailEdit.isBlank()) {
                showAlerter(text = "请输入内容", color = com.dullfan.base.R.color.red)
                return@myClickListener
            }

            val lcUser = LCUser()
            lcUser.username = userNameEdit
            lcUser.password = passwordEdit
            lcUser.email = emailEdit
            lcUser.put("avatar", DEFAULT_AVATAR_ID)
            lcUser.signUpInBackground().subscribe(object :Observer<LCUser>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: LCUser) {
                    showAlerter(text = "注册成功")
                    (activity as LoginActivity).startNavigateUp()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    e.message?.let { showAlerter(text = it, color = com.dullfan.base.R.color.red) }
                }

                override fun onComplete() {

                }
            })
        })
        return viewDataBinding.root
    }
}