package com.dullfan.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.LCUser
import cn.leancloud.types.LCNull
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.utils.myClickListener
import com.dullfan.library_common.utils.viewModels
import com.dullfan.login.databinding.FragmentForgotPasswordBinding
import com.dullfan.login.ui.activity.LoginActivity
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


class ForgotPasswordFragment : BaseFragment() {

    private val viewDataBinding by lazy {
        FragmentForgotPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewDataBinding.forgotPasswordLogin.setOnClickListener {
            (activity as LoginActivity).startNavigateUp()
        }

        viewDataBinding.forgotPasswordSubmit.setOnClickListener(myClickListener{
            val email = viewDataBinding.forgotPasswordEmailEdit.text.toString()

            if (email.isBlank()) {
                showAlerter(text = "请完善信息", color = com.dullfan.base.R.color.red)
                return@myClickListener
            }

            LCUser.requestPasswordResetInBackground(email).subscribe(object :Observer<LCNull>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: LCNull) {
                    showAlerter(text = "邮件已发送,请注意查收")
                    (activity as LoginActivity).startNavigateUp()
                }

                override fun onError(e: Throwable) {
                    e.message?.let { showAlerter(text = it,color = com.dullfan.base.R.color.red) }
                }

                override fun onComplete() {

                }
            })
        })

        return viewDataBinding.root
    }
}