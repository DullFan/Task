package com.dullfan.base.view

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import com.dullfan.base.R


class LoadingView :AlertDialog{

    constructor(context: Context,theme:Int):super(context,theme)

    constructor(context: Context):super(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        setCancelable(false)
        //加载布局
        setContentView(R.layout.dialog_loading_layout)
        //设置对话框的宽高
        val params = window!!.attributes
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window!!.attributes = params
    }

}