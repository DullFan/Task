package com.dullfan.module_main.ui.info

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import cn.leancloud.LCUser
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.delFile
import com.dullfan.base.lc.myObserver
import com.dullfan.base.lc.saveImage
import com.dullfan.base.utils.myClickListener
import com.dullfan.base.utils.permissionsReadAndWrite
import com.dullfan.base.utils.showPictureSelector
import com.dullfan.base.view.LoadingView
import com.dullfan.library_common.autoservice.PhotoService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.dullfan.library_common.utils.viewModels
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentInFoBinding
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.utils.showBottomSelectPictureDialog
import com.dullfan.module_main.viewmodel.HomeViewModel
import com.dullfan.module_main.viewmodel.InfoViewModel


class InFoFragment : BaseFragment() {

    companion object{
       var isInFoFragmentFlag = true;
    }

    val viewDataBinding by lazy {
        FragmentInFoBinding.inflate(layoutInflater)
    }

    val viewModel: HomeViewModel by viewModels()
    val infoViewModel: InfoViewModel by viewModels()

    private val dialogLogging by lazy {
        LoadingView(requireContext(), R.style.CustomDialog)
    }

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (it[Manifest.permission.READ_MEDIA_IMAGES]!! && it[Manifest.permission.CAMERA]!!
                    && it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
                ) {
                    showPictureSelector(requireContext(), onResult = { result ->
                        dialogLogging.show()
                        saveImage(
                            requireContext(),
                            result[0].realPath,
                            viewModel.infoAvatarUri,
                            viewModel.infoAvatarObjectId
                        )
                    })
                } else {
                    showToast("用户拒绝了权限")
                }
            }else{
                if (it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && it[Manifest.permission.CAMERA]!!
                    && it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
                ) {
                    showPictureSelector(requireContext(), onResult = { result ->
                        dialogLogging.show()
                        saveImage(
                            requireContext(),
                            result[0].realPath,
                            viewModel.infoAvatarUri,
                            viewModel.infoAvatarObjectId
                        )
                    })
                } else {
                    showToast("用户拒绝了权限")
                }
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding.lifecycleOwner = this
        viewDataBinding.infoViewModel = infoViewModel
        viewDataBinding.fragmentActivity = requireActivity()
        initData()
        initAvatar()

        viewModel.infoAvatarObjectId.observe(viewLifecycleOwner) { newObjectId ->
            updateAvatar(newObjectId)
        }
        return viewDataBinding.root
    }

    override fun onPause() {
        super.onPause()
        isInFoFragmentFlag = true
    }

    /**
     * 修改头像
     */
    private fun updateAvatar(newObjectId: String) {
        LCUser.getCurrentUser()?.apply {
            delFile(viewModel.infoData.value!!["avatar"].toString())

            put("avatar", newObjectId)

            saveInBackground().subscribe(myObserver(onError = { e ->
                e.message?.let {
                    showAlerter(
                        text = it,
                        color = com.dullfan.base.R.color.red
                    )
                }
            }, onNext = {
                (activity as MainActivity).updateAvatar(newObjectId)
                showAlerter(text = "头像修改成功")
                dialogLogging.dismiss()
            }))
        }
    }

    /**
     * 设置头像
     */
    private fun initAvatar() {
        viewDataBinding.infoAvatar.setOnClickListener(myClickListener {
            showBottomSelectPictureDialog(requireContext(), selectPicture = {
                registerForActivityResult.launch(permissionsReadAndWrite)
            }, lookImage = {
                if (viewModel.infoAvatarUri.value != null) {
                    CommonServiceLoader.load(PhotoService::class.java)
                        ?.startPhotoActivity(
                            requireContext(),
                            viewModel.infoAvatarUri.value!!
                        )
                }
            })
        })
    }
    /**
     * 初始化数据
     */
    private fun initData() {
        viewModel.findInfoData()
        viewModel.infoData.observe(viewLifecycleOwner) {
            viewModel.findInfoAvatar(it.getString("avatar"))
            infoViewModel.userNameString.value = it.username
            infoViewModel.positionString.value = it.getString("office")
            infoViewModel.phoneString.value = it.mobilePhoneNumber
            infoViewModel.emailString.value = it.email
        }

        viewModel.infoAvatarUri.observe(viewLifecycleOwner) {
            glide(it, viewDataBinding.infoAvatar)
        }
    }
}