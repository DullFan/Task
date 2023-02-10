package com.dullfan.module_main.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.library_common.autoservice.PhotoService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.MainDrawerLayoutItemBean
import com.dullfan.module_main.databinding.DialogInfoAvatarBinding
import com.dullfan.module_main.databinding.DialogProjectAddBinding
import com.dullfan.module_main.databinding.ItemDialogInfoLayoutRvBinding
import com.dullfan.module_main.databinding.ItemDialogLayoutRvBinding
import com.dullfan.module_main.generated.callback.OnClickListener
import com.dullfan.module_main.ui.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * 调用底部弹窗，进行图片操作
 */
fun showBottomSelectPictureDialog(context: Context,selectPicture:()->Unit,lookImage:()->Unit) {
    val bottomSheetDialog =
        BottomSheetDialog(context, R.style.BottomSheetDialogStyle)

    val dialogInfoAvatarBinding =
        DialogInfoAvatarBinding.inflate(LayoutInflater.from(context))
    bottomSheetDialog.setContentView(dialogInfoAvatarBinding.root)

    val arrayListOf = arrayListOf("从相册选择", "查看头像", "取消")

    dialogInfoAvatarBinding.dialogInfoRv.layoutManager =
        LinearLayoutManager(context)
    dialogInfoAvatarBinding.dialogInfoRv.adapter =
        object : BaseRvAdapter<String>(
            R.layout.item_dialog_info_layout_rv,
            arrayListOf
        ) {
            override fun onBind(
                rvDataBinding: ViewDataBinding,
                data: String,
                position: Int
            ) {
                rvDataBinding as ItemDialogInfoLayoutRvBinding
                rvDataBinding.itemDrawerLayoutRvText.text = data
                if (position == arrayListOf.size - 1) {
                    rvDataBinding.itemDrawerLayoutRvView.visibility = View.GONE
                }
                rvDataBinding.root.setOnClickListener {
                    when (position) {
                        0 -> {
                            selectPicture.invoke()
                        }
                        1 -> {
                            lookImage.invoke()
                        }
                    }
                    bottomSheetDialog.dismiss()
                }
            }
        }
    bottomSheetDialog.show()
}

fun showBottomAddProjectDialog(
    context: Context,
    drawerLayoutItemBeanList: MutableList<MainDrawerLayoutItemBean>,
    onClickListener:(position:Int)->Unit
){
    val dialogProjectAddBinding =
        DialogProjectAddBinding.inflate(LayoutInflater.from(context))
    val bottomSheetDialog =
        BottomSheetDialog(context, R.style.BottomSheetDialogStyle)
    dialogProjectAddBinding.dialogProjectAddRv.apply {
        layoutManager = LinearLayoutManager(context)
        adapter = object : BaseRvAdapter<MainDrawerLayoutItemBean>(
            R.layout.item_dialog_layout_rv,
            drawerLayoutItemBeanList
        ) {
            override fun onBind(
                rvDataBinding: ViewDataBinding,
                data: MainDrawerLayoutItemBean,
                position: Int
            ) {
                rvDataBinding as ItemDialogLayoutRvBinding
                rvDataBinding.itemDrawerLayoutRvImg.setImageResource(data.img)
                rvDataBinding.itemDrawerLayoutRvText.text = data.text
                rvDataBinding.root.setOnClickListener {
                    onClickListener.invoke(position)
                    bottomSheetDialog.dismiss()
                }
            }
        }
    }

    bottomSheetDialog.setContentView(dialogProjectAddBinding.root)
    bottomSheetDialog.show()
}