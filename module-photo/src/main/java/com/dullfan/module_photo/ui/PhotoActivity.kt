package com.dullfan.module_photo.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import com.dullfan.base.base.BaseActivity
import com.dullfan.module_photo.databinding.ActivityPhotoBinding


class PhotoActivity : BaseActivity() {

    val viewDataBinding by lazy {
        ActivityPhotoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(com.dullfan.base.R.anim.alpha_in,com.dullfan.base.R.anim.alpha_out)
        setContentView(viewDataBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val stringExtra = intent.getStringExtra("imageUrl")
        if(stringExtra != null){
            glide(stringExtra, viewDataBinding.activityPhoto)
        }
        viewDataBinding.activityPhoto.setOnClickListener { onBackPressed() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(com.dullfan.base.R.anim.alpha_in,com.dullfan.base.R.anim.alpha_out)
    }

}