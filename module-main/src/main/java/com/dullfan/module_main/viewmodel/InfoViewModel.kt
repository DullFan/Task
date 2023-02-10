package com.dullfan.module_main.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.leancloud.LCObject
import cn.leancloud.LCUser
import com.dullfan.base.utils.showAlerter
import com.dullfan.base.utils.showAlerterError
import com.dullfan.base.utils.showLog
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class InfoViewModel : ViewModel() {

    var whetherToEditFlag = MutableLiveData(false)
    val phoneString = MutableLiveData<String>("")
    val positionString = MutableLiveData<String>("")
    val userNameString = MutableLiveData<String>("")
    val emailString = MutableLiveData<String>("")

    @RequiresApi(Build.VERSION_CODES.M)
    fun editOnClick(fragmentActivity: FragmentActivity) {
        showLog(phoneString.value.toString())
        showLog(positionString.value.toString())
        showLog(userNameString.value.toString())
        showLog(emailString.value.toString())
        if (whetherToEditFlag.value == true) {
            if (phoneString.value!!.isBlank() || positionString.value == null || userNameString.value == null || emailString.value!!.isBlank()) {
                showAlerterError(
                    fragmentActivity,
                    text = "请完善信息",
                    color = com.dullfan.base.R.color.red
                )
                return
            }
            LCUser.getCurrentUser()?.apply {
                put("username", userNameString.value)
                put("mobilePhoneNumber", phoneString.value)
                put("office", positionString.value)
                put("email", emailString.value)
                saveInBackground().subscribe(object : Observer<LCObject> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: LCObject) {
                        showAlerter(fragmentActivity)
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let {
                            showAlerterError(
                                fragmentActivity,
                                text = it,
                                color = com.dullfan.base.R.color.red
                            )
                        }
                    }

                    override fun onComplete() {

                    }
                })
            }
        }
        whetherToEditFlag.value = !whetherToEditFlag.value!!
    }


}