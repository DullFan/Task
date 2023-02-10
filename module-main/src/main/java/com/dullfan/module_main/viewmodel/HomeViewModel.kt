package com.dullfan.module_main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.leancloud.LCUser
import com.dullfan.base.lc.getLCFileImageUrl
import com.dullfan.base.lc.getLCUserIndoData

class HomeViewModel : ViewModel() {
    /**
     * 当前用户信息
     */
    val infoData: MutableLiveData<LCUser> = MutableLiveData()

    /**
     * 当前用户头像Uri
     */
    val infoAvatarUri: MutableLiveData<String> = MutableLiveData()

    /**
     * 当前用户头像Id
     */
    val infoAvatarObjectId: MutableLiveData<String> = MutableLiveData()

    /**
     * 创建项目头像
     */
    val projectAvatar: MutableLiveData<String> = MutableLiveData()

    /**
     * 创建项目头像Id
     */
    val projectObjectId: MutableLiveData<String> = MutableLiveData()

    fun findInfoAvatar(objectId:String) {
        getLCFileImageUrl(objectId, infoAvatarUri)
    }

    fun findInfoData() {
        val currentUser = LCUser.getCurrentUser()
        getLCUserIndoData(currentUser.objectId.toString(), infoData)
    }


}