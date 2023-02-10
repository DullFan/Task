package com.dullfan.base.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.dullfan.base.engine.GlideEngine
import com.dullfan.library_common.autoservice.LoginService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureSelectorStyle
import com.tapadoo.alerter.Alerter
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min


/**
 * 显示Log
 * @param tag Log标识
 * @param content 输出的内容
 */
fun showLog(content: Any, tag: String = "TAG") {
    Log.e(tag, "$content")
}

private var toast: Toast? = null

/**
 * 封装Toast
 */
fun Any.showToast(context: Context?) {
    if (toast == null) {
        toast = Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT)
    } else {
        toast!!.cancel()
        toast = Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT)
    }
    toast!!.show()
}

/**
 * 将URI路径转化为path路径
 */
fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
    //获取图片路径
    val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
    //通过selectedImage和filePathColumns 获取图片真实位置
    val c: Cursor? = context.contentResolver.query(contentURI, filePathColumns, null, null, null)
    //移动到第一行
    c?.moveToFirst()
    val columnIndex: Int? = c?.getColumnIndex(filePathColumns[0])
    //获取图片位置,不存在返回-1
    val path = columnIndex?.let { c.getString(it) }
    c?.close()
    return path
}

/**
 * 判断手机号码是否合法
 */
fun regexPhone(phone: String): Boolean {
    if (phone.length != 11) {
        return false
    }
    val mainRegex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,1,2,3,5-9])|(177))\\d{8}$"
    val p = Pattern.compile(mainRegex)
    val m = p.matcher(phone)
    return m.matches()
}

/**
 * 获取当前时间(年-月-日 小时:分:秒)
 */
fun getNowDate(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return simpleDateFormat.format(Date())
}

/**
 * 获取当前时间(年-月-日)
 */
fun getYearMonthDay(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    return simpleDateFormat.format(Date())
}

/**
 * 计算两个日期时间差
 */
fun timeDifference(startTime: String, endTime: String): Long {
    //获取当前区域的时间格式
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val time1 = simpleDateFormat.parse(startTime).time
    val time2 = simpleDateFormat.parse(endTime).time
    //分钟差
    val minutes = (time2 - time1) / (1000)
    return minutes
}

/**
 * 判断网络是否连接
 */
fun isNetConnected(context: Context?): Boolean {
    if (context != null) {
        val mConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = mConnectivityManager.activeNetworkInfo
        if (mNetworkInfo != null) {
            // 有网络返回true
            return mNetworkInfo.isAvailable
        }
    }
    // 无网络返回false
    return false
}

/**
 * 判断是否使用网络请求
 */
fun isNet(context: Context, launch: () -> Unit) {
    if (isNetConnected(context)) {
        launch.invoke()
    } else {
        "当前无网络".showToast(context)
    }
}

/**
 * 判断是否有登录
 */
fun isCookie(context: Context, launch: () -> Unit) {
    if (KV.decodeInt(MMKVData.U_ID) != 0) {
        launch.invoke()
    } else {
        CommonServiceLoader.load(LoginService::class.java)?.startLoginActivity(context)
        "当前还未登录哦!!!".showToast(context)
    }
}


/**
 * 判断是否有网络或登录
 */
fun isNetCookie(context: Context, launch: () -> Unit) {
    if (isNetConnected(context)) {
        if (KV.decodeInt(MMKVData.U_ID) != 0) {
            launch.invoke()
        } else {
            CommonServiceLoader.load(LoginService::class.java)?.startLoginActivity(context)
            "当前还未登录哦!!!".showToast(context)

        }
    } else {
        "当前无网络".showToast(context)
    }
}

/**
 * 选择日期以及时间
 */
fun initDateTime(context: Context, textView: TextView) {
    val calendar = Calendar.getInstance()
    val calendarYear = calendar.get(Calendar.YEAR)
    val calendarMonth = calendar.get(Calendar.MONTH)
    val calendarDay = calendar.get(Calendar.DAY_OF_MONTH)
    val calendarHour = calendar.get(Calendar.HOUR_OF_DAY)
    val calendarMinute = calendar.get(Calendar.MINUTE)
    val datePickerDialog =
        DatePickerDialog(context, { view, year, month, dayOfMonth ->

            val date = "${year}-${month + 1}-${dayOfMonth}"

            val timePickerDialog = TimePickerDialog(
                context, { view, hourOfDay, minute ->
                    val dateTime = "${hourOfDay}:${minute}"
                    textView.text = "$date $dateTime"
                },
                calendarHour,
                calendarMinute,
                true
            )
            timePickerDialog.show()
        }, calendarYear, calendarMonth, calendarDay)
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
    datePickerDialog.show()
}

/**
 * 选择日期以及时间,并且设置当前选中时间
 */
fun initDate(context: Context, textView: TextView, nowDate: String = "") {
    val calendarYear: Int
    val calendarMonth: Int
    val calendarDay: Int
    val calendar = Calendar.getInstance()
    if (nowDate == "") {
        calendarYear = calendar.get(Calendar.YEAR)
        calendarMonth = calendar.get(Calendar.MONTH)
        calendarDay = calendar.get(Calendar.DAY_OF_MONTH)
    } else {
        val split = nowDate.split("-")
        calendarYear = split[0].toInt()
        calendarMonth = split[1].toInt() - 1
        calendarDay = split[2].toInt()
    }
    val datePickerDialog =
        DatePickerDialog(context, { view, year, month, dayOfMonth ->
            val date = "${year}-${month + 1}-${dayOfMonth}"
            textView.text = date
        }, calendarYear, calendarMonth, calendarDay)
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
    datePickerDialog.show()
}


/**
 * 根据优先等级修改图片以及文本
 */
fun emergencyLevelSet(
    objectId: String,
    imageView: ImageView,
    textView: TextView,
    context: Context
) {
    var color = 1
    when (objectId) {
        PROJECT_GRADE_HIGH -> {
            imageView.rotation = 0f
            textView.text = "高"
            color = com.dullfan.base.R.color.project_priority_high_color
        }
        PROJECT_GRADE_MIDDLE -> {
            imageView.rotation = 0f
            textView.text = "中"
            color = com.dullfan.base.R.color.project_priority_middle_color
        }
        PROJECT_GRADE_LOW -> {
            imageView.rotation = 180f
            textView.text = "低"
            color = com.dullfan.base.R.color.project_priority_low_color
        }
    }
    emergencyLevelColorSet(
        textView,
        imageView,
        color,
        context
    )
}

/**
 * 根据优先等级修改图片以及文本显示颜色
 */
fun emergencyLevelColorSet(
    textView: TextView,
    imageView: ImageView,
    color: Int,
    context: Context
) {
    textView.setTextColor(
        context.resources.getColor(
            color
        )
    )
    imageView.imageTintList =
        ColorStateList.valueOf(
            context.resources.getColor(
                color
            )
        )
}

/**
 * 显示图片选择器
 */
fun showPictureSelector(
    context: Context,
    maxSelectNum: Int = 1,
    onResult: (ArrayList<LocalMedia>) -> Unit,
    onCancel: () -> Unit = {}
) {
    val pictureSelectorStyle = PictureSelectorStyle()
    val back = context.resources.getColor(
        com.dullfan.base.R.color.purple_700
    )

    pictureSelectorStyle.albumWindowStyle.apply {
        albumAdapterItemTitleColor = back
    }

    PictureSelector.create(context)
        .openGallery(SelectMimeType.ofImage())
        .setSelectorUIStyle(pictureSelectorStyle)
        .setImageEngine(GlideEngine.createGlideEngine())
        .setMaxSelectNum(maxSelectNum)//最大选中的数量
        .forResult(object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                onResult.invoke(result)
            }

            override fun onCancel() {
                onCancel.invoke()
            }
        })
}

/**
 * 成功提示信息
 */
@RequiresApi(Build.VERSION_CODES.M)
fun showAlerter(
    activity: FragmentActivity,
    title: String = "提示",
    text: String = "修改成功",
    color: Int = com.dullfan.base.R.color.purple_500
) {
    Alerter.create(activity)
        .setTitle(title)
        .setText(text)
        .setBackgroundColorInt(activity.getColor(color))
        .show()
}

/**
 * 失败提示显示
 */
@RequiresApi(Build.VERSION_CODES.M)
fun showAlerterError(
    activity: FragmentActivity,
    title: String = "提示",
    text: String = "修改失败",
    color: Int = com.dullfan.base.R.color.red
) {
    Alerter.create(activity)
        .setTitle(title)
        .setText(text)
        .setBackgroundColorInt(activity.getColor(color))
        .show()
}

/**
 * 设置文本删除线
 * @param textView 文本View
 * @param flag 为true设置删除线,为false取消删除线
 */
fun deleteLineText(textView: TextView, flag: Boolean = true) {
    if (flag) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
    //开启抗锯齿
    textView.paint.isAntiAlias = true
}

/**
 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
 */
fun px2dip(context: Context, pxValue: Float): Int {
    val density = context.resources.displayMetrics.density
    return (pxValue / density + 0.5f).toInt()
}

/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun dip2px(context: Context, dpValue: Float): Int {
    val density = context.resources.displayMetrics.density
    return (density * dpValue + 0.5f).toInt()
}

/**
 *  秒转换成分钟/小时
 */
fun durationToYearAndMin(duration: Int): String {
    val l = duration / 60
    if (l >= 60) {
        val l1 = l / 60
        return "${l1} 小时${l - l1 * 60} 分钟"
    } else {
        return "${l}分钟"
    }
}

/**
 * 秒转换成分钟/小时
 */
fun durationToYearAndMin(duration: Int,action:(year:Int,min:Int)->Unit) {
    val l = duration / 60
    if (l >= 60) {
        val l1 = l / 60
        action.invoke(l1,l - l1 * 60)
    } else {
        action.invoke(0,l)
    }
}

/**
 * 获取每个月第一天为星期几
 */
fun getMonthOneDayWeek(calendar:Calendar): Int {
    calendar.set(Calendar.DAY_OF_MONTH,1)
    return calendar.get(Calendar.DAY_OF_WEEK) - 1
}

/**
 * 获取当月有几天
 */
fun getMonthMaxData(calendar:Calendar): Int {
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}