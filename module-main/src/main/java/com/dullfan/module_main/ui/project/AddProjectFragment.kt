package com.dullfan.module_main.ui.project

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import cn.leancloud.LCUser
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.base.lc.saveImage
import com.dullfan.base.utils.*
import com.dullfan.base.view.LoadingView
import com.dullfan.library_common.utils.viewModels
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentAddProjectBinding
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.viewmodel.HomeViewModel

class AddProjectFragment : BaseFragment() {

    val viewDataBinding by lazy {
        FragmentAddProjectBinding.inflate(layoutInflater)
    }

    val viewModel: HomeViewModel by viewModels()

    var spinnerData: String = "0"

    var realPath: String = ""

    val dialogLogging by lazy {
        LoadingView(requireContext(), R.style.CustomDialog)
    }

    /**
     * 动态权限申请
     */
    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && it[Manifest.permission.CAMERA]!!
                && it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
            ) {
                showPictureSelector(requireContext(), onResult = { result ->
                    if (result != null) {
                        realPath = result[0].realPath
                        glide(result[0].realPath, viewDataBinding.addProjectImage)
                        viewDataBinding.addProjectAddImage.visibility = View.GONE
                        viewDataBinding.addProjectImageLayout.visibility = View.VISIBLE
                    }
                })
            } else {
                showToast("用户拒绝了权限")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initEmergDataencyLevel()
        initSelectImage()
        initSubmit()
        initBack()
        return viewDataBinding.root
    }

    /**
     * 返回上一级
     */
    private fun initBack() {
        ProjectDetailsFragment.allTaskFragmentIsShow = true
        viewDataBinding.addProjectBack.setOnClickListener {
            (activity as MainActivity).infoNavigateUp()
        }

        viewDataBinding.addProjectView01.setOnClickListener {
            (activity as MainActivity).infoNavigateUp()
        }
    }

    /**
     * 设置提交事件
     */
    private fun initSubmit() {
        viewDataBinding.addProjectButton.setOnClickListener {
            val name = viewDataBinding.addProjectName.text.toString()
            val detail = viewDataBinding.addProjectDetail.text.toString()

            if (name.isBlank() || detail.isBlank()) {
                showAlerter(text = "请完善信息", color = com.dullfan.base.R.color.red)
                return@setOnClickListener
            }
            dialogLogging.show()

            val lcObject = LCObject("Projects")

            lcObject.put("project_creator_id", LCUser.getCurrentUser().objectId)
            lcObject.put("project_description", detail)
            lcObject.put("project_name", name)
            lcObject.put("project_priority_level", spinnerData)
            lcObject.put("project_avatar", DEFAULT_AVATAR_ID)

            if (viewDataBinding.addProjectAddImage.visibility == View.GONE) {
                //保存图片
                saveImage(
                    requireContext(),
                    realPath,
                    viewModel.projectAvatar,
                    viewModel.projectObjectId
                )

                viewModel.projectObjectId.observe(viewLifecycleOwner) {
                    lcObject.put("project_avatar", it)
                    initSaveProject(lcObject)
                }
            } else {
                initSaveProject(lcObject)
            }
        }
    }

    /**
     * 保存项目
     */
    private fun initSaveProject(lcObject: LCObject) {
        lcObject.saveInBackground().subscribe(myObserver(onNext = { t ->
            showAlerter(text = "添加成功")
            dialogLogging.dismiss()
            (activity as MainActivity).infoNavigateUp()
        }, onError = {
            showAlerterError()
        }))
    }

    /**
     * 选择本地图片
     */
    private fun initSelectImage() {
        viewDataBinding.addProjectImageCard.setOnClickListener {
            registerForActivityResult.launch(permissionsReadAndWrite)
        }

        viewDataBinding.addProjectImageDel.setOnClickListener {
            viewDataBinding.addProjectAddImage.visibility = View.VISIBLE
            viewDataBinding.addProjectImageLayout.visibility = View.GONE
        }
    }

    /**
     * 初始化优先等级
     */
    private fun initEmergDataencyLevel() {
        val arrayListOf = arrayListOf<String>()

        val emergDataencyLevel = LCQuery<LCObject>("EmergencyLevel")
        emergDataencyLevel.findInBackground().subscribe(myObserver { t ->
            for (datum in t) {
                arrayListOf.add(datum.get("emergencyLevelInformation").toString())
            }

            val arrayAdapter = ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                arrayListOf
            )

            viewDataBinding.addProjectPriorityLevel.adapter = arrayAdapter

            viewDataBinding.addProjectPriorityLevel.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        spinnerData = t[position].objectId
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
        })
    }
}