package com.dullfan.base.utils

import com.tencent.mmkv.MMKV

val KV: MMKV = MMKV.defaultMMKV()

object MMKVData {
    const val U_ID = "id"
    const val U_TOKEN = "token"
}