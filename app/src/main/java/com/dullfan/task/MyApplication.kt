package com.dullfan.task

import android.app.Application
import cn.leancloud.LeanCloud
import com.dullfan.base.loadsir.EmptyCallback
import com.dullfan.base.loadsir.ErrorCallback
import com.dullfan.base.loadsir.LoadingCallback
import com.dullfan.base.loadsir.TimeoutCallback
import com.kingja.loadsir.core.LoadSir
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.tencent.mmkv.MMKV


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //MMKV初始化
        MMKV.initialize(this)

        //LeanCloud初始化
        LeanCloud.initialize(
            this,
            "vvOaUP2pMHuvi99MgJFmIPf3-gzGzoHsz",
            "jMoGIiFG6oTFDC07hzl1aLHd",
            "https://vvoaup2p.lc-cn-n1-shared.com"
        );

        //LoadSir初始化
        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(EmptyCallback())
            .addCallback(TimeoutCallback())
            .addCallback(LoadingCallback())
            .setDefaultCallback(LoadingCallback::class.java)
            .commit()

        AppCenter.start(
            this,
            "20f03823-10d4-4551-a04e-4f84442e25ac",
            Analytics::class.java,
            Crashes::class.java
        )
    }
}