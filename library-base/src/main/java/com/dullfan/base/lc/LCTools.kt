package com.dullfan.base.lc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.rotationMatrix
import androidx.lifecycle.MutableLiveData
import cn.leancloud.LCFile
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import cn.leancloud.LCUser
import com.bumptech.glide.Glide
import com.dullfan.base.utils.*
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * 根据文件Id删除文件
 */
fun delFile(objectId: String) {
    LCQuery<LCObject>("_File").getInBackground(objectId).subscribe(myObserver(
        onError = { e ->
            e.printStackTrace()
        },
        onNext = {
            //判断该文件是否是初始头像
            if (DEFAULT_AVATAR_ID != it.objectId) {
                LCObject.createWithoutData("_File", objectId).deleteInBackground()
                    .subscribe(
                        myObserver(
                            onError = { e -> e.printStackTrace() },
                            onNext = { _ -> showLog("文件删除成功,文件ID为:${it.objectId}") })
                    )
            }
        }
    ))
}

/**
 *  根据文件Id去寻找图片的Uri,在将Uri保存到ViewModel
 */
fun getLCFileImageUrl(objectId: String, avatarLiveData: MutableLiveData<String>) {
    val lcQuery = LCQuery<LCObject>("_File")
    lcQuery.whereEqualTo("objectId", objectId)
    lcQuery.findInBackground().subscribe(myObserver(onError = {
        it.printStackTrace()
    }, onNext = {
        try {
            avatarLiveData.value = it[0].serverData["url"] as String
        } catch (e: Exception) {
        }
    }))
}

/**
 * 根据文件Id去寻找图片的Uri,在用Glide加载图片
 */
fun getLCFileImageUrl(objectId: String, imageView: ImageView, context: Context) {
    val lcQuery = LCQuery<LCObject>("_File")
    lcQuery.whereEqualTo("objectId", objectId)
    lcQuery.findInBackground().subscribe(myObserver(onError = {
        it.printStackTrace()
    }, onNext = {
        try {
            Glide.with(context).load(it[0].serverData["url"] as String).into(imageView)
        } catch (e: Exception) {
        }
    }))
}

/**
 * 根据文件Id去寻找文件的Uri。
 */
fun getLCFileUrl(objectId: String, next: (url: String) -> Unit) {
    val lcQuery = LCQuery<LCObject>("_File")
    lcQuery.whereEqualTo("objectId", objectId)
    lcQuery.findInBackground().subscribe(myObserver(onError = {
        it.printStackTrace()
    }, onNext = {
        try {
            next.invoke(it[0].serverData["url"].toString())
        } catch (e: Exception) {
        }
    }))
}


/**
 * 根据用户Id查找对应的用户
 */
fun getLCUserIndoData(objectId: String, findInfoData: MutableLiveData<LCUser>) {
    val query = LCUser.getQuery()
    query.whereEqualTo("objectId", objectId)
    query.findInBackground().subscribe(object : Observer<List<LCUser>> {
        override fun onSubscribe(d: Disposable) {

        }

        override fun onNext(t: List<LCUser>) {
            try {
                findInfoData.value = t[0]
            } catch (e: Exception) {
            }
        }

        override fun onError(e: Throwable) {

        }

        override fun onComplete() {

        }
    })
}

/**
 * 根据用户Id查找对应的用户
 */
fun getLCUserIndoData(objectId: String, onNext: (t: LCUser) -> Unit) {
    val query = LCUser.getQuery()
    showLog(objectId, "测试中")
    query.getInBackground(objectId)
        .subscribe(myObserver(onError = { e -> showLog("${e.message}", "测试中") }, onNext = { t ->
            showLog("${objectId}---成功", "测试中")
            onNext.invoke(t)
        }))
}


/**
 * 保存图片
 */
fun saveImage(
    context: Context,
    absoluteLocalFilePath: String,
    avatarLiveData: MutableLiveData<String>,
    avatarObjectIdLiveData: MutableLiveData<String>
) {
    //这里的第二个参数必须需要是Path
    val lFile =
        LCFile.withAbsoluteLocalPath(
            "avatar-${System.currentTimeMillis()}-${(1..100000).shuffled().first()}.png",
            absoluteLocalFilePath
        )
    lFile.saveInBackground().subscribe(object : Observer<LCFile> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onNext(t: LCFile) {
            avatarLiveData.value = t.url
            avatarObjectIdLiveData.value = t.objectId
        }

        override fun onError(e: Throwable) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }

        override fun onComplete() {
        }
    })
}

/**
 * 获取表中的所有数据
 */
fun getLCTableData(tableName: String, projectTableTypeData: MutableLiveData<List<LCObject>>) {
    val lcQuery = LCQuery<LCObject>(tableName)
    lcQuery.findInBackground().subscribe(myObserver(onNext = {
        projectTableTypeData.value = it
    }))
}

/**
 * 根据优先等级Id获取优先等级
 */
fun findLevel(objectId: String, objectEmergencyLevel: MutableLiveData<String>) {
    LCQuery<LCObject>("EmergencyLevel").getInBackground(objectId).subscribe(myObserver {
        objectEmergencyLevel.value = it["emergencyLevelInformation"].toString()
    })
}

fun <T : Any> myObserver(
    onError: (e: Throwable) -> Unit = {
        it.printStackTrace()
    },
    onNext: (t: T) -> Unit = {}
): Observer<T> {
    return object : Observer<T> {
        override fun onSubscribe(d: Disposable) {

        }

        override fun onNext(t: T) {
            onNext.invoke(t)
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            onError.invoke(e)
        }

        override fun onComplete() {

        }
    }
}