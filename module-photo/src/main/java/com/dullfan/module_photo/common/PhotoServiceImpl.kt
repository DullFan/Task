package com.dullfan.module_photo.common

import android.content.Context
import android.content.Intent
import com.dullfan.library_common.autoservice.PhotoService
import com.dullfan.module_photo.ui.PhotoActivity
import com.google.auto.service.AutoService


@AutoService(PhotoService::class)
class PhotoServiceImpl :PhotoService{
    override fun startPhotoActivity(context: Context, imageUrl: String) {
        val intent = Intent(context, PhotoActivity::class.java)
        intent.putExtra("imageUrl",imageUrl)
        context.startActivity(intent)
    }
}