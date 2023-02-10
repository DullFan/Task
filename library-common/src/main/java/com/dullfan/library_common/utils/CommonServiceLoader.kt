package com.dullfan.library_common.utils

import android.util.Log
import java.util.*

/**
 * 工具类
 */
object CommonServiceLoader {
    fun <S> load(service: Class<S>?): S? {
        //一个组件只有一个实现者，将ServiceLoader给Load出来，
        //可能会Load多个出来，所以在这里只拿第一个
        val iterator = ServiceLoader.load(service).iterator()

        return if (iterator.hasNext()) {
            iterator.next()
        } else {
            null
        }
    }
}