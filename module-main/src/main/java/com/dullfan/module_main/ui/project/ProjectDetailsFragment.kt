package com.dullfan.module_main.ui.project

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import cn.leancloud.LCUser
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.*
import com.dullfan.base.utils.*
import com.dullfan.base.view.LoadingView
import com.dullfan.library_common.autoservice.PhotoService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.dullfan.library_common.utils.viewModels
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentProjectDetailsBinding
import com.dullfan.module_main.databinding.ItemProjectDetailsDrawerLayoutRvBinding
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.ui.project.details.TaskFragment
import com.dullfan.module_main.ui.project.details.TaskFragment.Companion.taskFragmentIsShow
import com.dullfan.module_main.ui.statistical_data.StatisticalDataFragment
import com.dullfan.module_main.utils.StatisticalDataMode
import com.dullfan.module_main.utils.projectDetailsData
import com.dullfan.module_main.utils.showBottomSelectPictureDialog
import com.dullfan.module_main.viewmodel.HomeViewModel


class ProjectDetailsFragment : BaseFragment() {
    lateinit var baseRvAdapter: BaseRvAdapter<String>

    val viewDataBinding by lazy {
        FragmentProjectDetailsBinding.inflate(layoutInflater)
    }

    val dialogLogging by lazy {
        LoadingView(requireContext(), R.style.CustomDialog)
    }
    val viewModel: HomeViewModel by viewModels()
    var emergencyLevelObjectId = ""

    companion object{
        var allTaskFragmentIsShow = false
    }

    /**
     * 文件读取权限申请
     */
    val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (it[Manifest.permission.READ_MEDIA_IMAGES]!! && it[Manifest.permission.CAMERA]!!
                    && it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
                ) {
                    showPictureSelector(requireContext(), onResult = { result ->
                        if (result != null) {
                            dialogLogging.show()
                            //保存图片
                            saveImage(
                                requireContext(),
                                result[0].realPath,
                                viewModel.projectAvatar,
                                viewModel.projectObjectId
                            )
                        }
                    })
                } else {
                    showToast("用户拒绝了权限")
                }
            }else{
                if (it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && it[Manifest.permission.CAMERA]!!
                    && it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
                ) {
                    showPictureSelector(requireContext(), onResult = { result ->
                        if (result != null) {
                            dialogLogging.show()
                            //保存图片
                            saveImage(
                                requireContext(),
                                result[0].realPath,
                                viewModel.projectAvatar,
                                viewModel.projectObjectId
                            )
                        }
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
        allTaskFragmentIsShow = true
        viewDataBinding.projectDetailsBack.setOnClickListener(myClickListener {
            onBack()
        })

        viewDataBinding.projectDetailsView01.setOnClickListener(myClickListener {
            onBack()
        })
        getLCFileImageUrl(projectDetailsData["project_avatar"].toString(), viewModel.projectAvatar)
        emergencyLevelObjectId = projectDetailsData["project_priority_level"].toString()
        initDrawer()
        // --------------------------->
        //默认跳转
        viewDataBinding.projectDetailsFramelayout.visibility = View.VISIBLE
        viewDataBinding.projectDetailsIntroduceLayout.visibility = View.GONE
        startFragment(
            viewDataBinding.projectDetailsFramelayout.id,
            TaskFragment(viewDataBinding)
        )
        baseRvAdapter.index = 1
        // -------------------------------------->


        initIntroduce()
        initEditIntroduce()
        initAvatar()

        viewModel.projectObjectId.observe(viewLifecycleOwner) { newObjectId ->
            updateAvatar(newObjectId)
        }


        return viewDataBinding.root
    }

    /**
     * 回退事件
     */
    private fun onBack() {
        if (taskFragmentIsShow) {
            taskFragmentIsShow = false
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            allTaskFragmentIsShow = false
            (activity as MainActivity).infoNavigateUp()
        }
    }

    /**
     * 更改完头像后获取最新数据
     */
    fun updateProjectData() {
        val lcQuery = LCQuery<LCObject>("Projects")
        lcQuery.getInBackground(projectDetailsData.objectId).subscribe(myObserver {
            projectDetailsData = it
            initIntroduce()
        })
    }

    /**
     * 更新头像数据
     */
    private fun updateAvatar(newObjectId: String) {
        delFile(projectDetailsData["project_avatar"].toString())
        val lcObject = LCObject.createWithoutData("Projects", projectDetailsData.objectId)
        lcObject.put("project_avatar", newObjectId)
        lcObject.saveInBackground().subscribe(myObserver(onError = { e ->
            e.printStackTrace()
            e.message?.let {
                showAlerter(
                    text = it,
                    color = com.dullfan.base.R.color.red
                )
            }
        }, onNext = {
            getLCFileImageUrl(newObjectId, viewModel.projectAvatar)
            showAlerter(text = "头像修改成功")
            updateProjectData()
            dialogLogging.dismiss()
        }))
    }

    /**
     * 头像设置
     */
    private fun initAvatar() {
        viewDataBinding.projectIntroduceAvatar.setOnClickListener(myClickListener {
            showBottomSelectPictureDialog(requireContext(), selectPicture = {
                if (LCUser.getCurrentUser().objectId != projectDetailsData["project_creator_id"]) {
                    showAlerterError(text = "当前用户没有权限修改内容~")
                    return@showBottomSelectPictureDialog
                }
                registerForActivityResult.launch(permissionsReadAndWrite)
            }, lookImage = {
                viewModel.projectAvatar.value?.let { it1 ->
                    CommonServiceLoader.load(PhotoService::class.java)
                        ?.startPhotoActivity(
                            requireContext(),
                            it1
                        )
                }
            })
        })
    }

    /**
     * 修改项目信息
     */
    private fun initEditIntroduce() {
        var nowDateFlag = true
        if (LCUser.getCurrentUser().objectId != projectDetailsData["project_creator_id"]) {
            viewDataBinding.infoEditImage.visibility = View.GONE
            viewDataBinding.infoEditImageCard.visibility = View.GONE
        }
        viewDataBinding.infoEditImageCard.setOnClickListener {
            viewDataBinding.projectIntroduceArrowImg.setOnClickListener {
                arrowDialog()
            }
            viewDataBinding.projectIntroduceArrowText.setOnClickListener {
                arrowDialog()
            }

            val projectTitle = viewDataBinding.projectIntroduceTitle.text.toString()
            val projectDescribe = viewDataBinding.projectIntroduceIntroduceDescribe.text.toString()

            if (projectDescribe.isBlank() || projectTitle.isBlank()) {
                showAlerterError(text = "请完善信息")
                return@setOnClickListener
            }


            if (viewDataBinding.projectIntroduceTitle.isEnabled) {
                viewDataBinding.projectIntroduceTitle.isEnabled = false
                viewDataBinding.projectIntroduceIntroduceDescribe.isEnabled = false
                val lcObject = LCObject.createWithoutData("Projects", projectDetailsData.objectId)
                lcObject.put("project_description", projectDescribe)
                lcObject.put("project_name", projectTitle)
                lcObject.put("project_priority_level", emergencyLevelObjectId)
                lcObject.saveInBackground().subscribe(myObserver(onError = {
                    viewDataBinding.projectIntroduceTitle.isEnabled = true
                    viewDataBinding.projectIntroduceIntroduceDescribe.isEnabled = true
                    viewDataBinding.infoEditImage.setImageResource(R.drawable.info_finish)
                    it.printStackTrace()
                }, onNext = {
                    viewDataBinding.projectIntroduceTitle.isEnabled = false
                    viewDataBinding.projectIntroduceIntroduceDescribe.isEnabled = false
                    viewDataBinding.infoEditImage.setImageResource(R.drawable.info_edit)
                    showAlerter("保存成功")
                }))
            } else {
                viewDataBinding.projectIntroduceTitle.isEnabled = true
                viewDataBinding.projectIntroduceIntroduceDescribe.isEnabled = true
                viewDataBinding.infoEditImage.setImageResource(R.drawable.info_finish)
                viewDataBinding.projectIntroduceTitle
                showToast("点击要修改的内容哦~")
            }
        }
    }

    /**
     * 选择优先等级对话框
     */
    private fun arrowDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(arrayOf("高", "中", "低")) { dialog, which ->
            when (which) {
                0 -> emergencyLevelObjectId = PROJECT_GRADE_HIGH
                1 -> emergencyLevelObjectId = PROJECT_GRADE_MIDDLE
                2 -> emergencyLevelObjectId = PROJECT_GRADE_LOW
            }
            emergencyLevelSet(
                emergencyLevelObjectId,
                viewDataBinding.projectIntroduceArrowImg,
                viewDataBinding.projectIntroduceArrowText,
                requireContext()
            )
        }
        builder.show()
    }

    /**
     * 初始化详情
     */
    private fun initIntroduce() {
        viewDataBinding.projectIntroduceTitle.setText(
            projectDetailsData.get("project_name").toString()
        )

        viewDataBinding.projectIntroduceId.text = projectDetailsData.objectId

        viewDataBinding.projectIntroduceIntroduceDescribe.setText(
            projectDetailsData.get("project_description").toString()
        )

        viewDataBinding.projectIntroduceCreateTime.text =
            "创建于${projectDetailsData.createdAtString.substring(0, 10)}"

        getLCFileImageUrl(
            projectDetailsData.get("project_avatar").toString(),
            viewDataBinding.projectIntroduceAvatar,
            requireContext()
        )

        emergencyLevelSet(
            projectDetailsData["project_priority_level"].toString(),
            viewDataBinding.projectIntroduceArrowImg,
            viewDataBinding.projectIntroduceArrowText,
            requireContext()
        )

        val whereEqualTo =
            LCQuery<LCObject>("Tasks").whereEqualTo("project_id", projectDetailsData.objectId)
        whereEqualTo.findInBackground().subscribe(myObserver {
            var num = 0
            viewDataBinding.itemProjectTaskNumber.text = "${it.size}"
            it.forEach {
                if (!it.getBoolean("task_is_completed")) {
                    num += 1;
                }
            }
            viewDataBinding.itemProjectTaskNewNumber.text = "${num}"

            LCQuery<LCObject>("FocusMode")
                .whereEqualTo("ProjectId", projectDetailsData.objectId)
                .findInBackground()
                .subscribe(myObserver {
                    viewDataBinding.itemProjectTaskUpcomingNumber.text = "${it.size}"
                })
        })
    }

    /**
     * 初始化侧滑栏
     */
    private fun initDrawer() {
        //设置侧滑栏为透明色
        viewDataBinding.projectDetailsDrawer.setScrimColor(Color.TRANSPARENT)
        viewDataBinding.projectDetailsNavigation.setBackgroundColor(Color.TRANSPARENT)

        val arrayListOf = arrayListOf("项目详情", "任务列表","数据统计")

        baseRvAdapter = object : BaseRvAdapter<String>(
            R.layout.item_project_details_drawer_layout_rv,
            arrayListOf
        ) {
            override fun onBind(
                rvDataBinding: ViewDataBinding,
                data: String,
                position: Int
            ) {
                rvDataBinding as ItemProjectDetailsDrawerLayoutRvBinding

                if (position == index) {
                    rvSelectColor(
                        rvDataBinding,
                        com.dullfan.base.R.color.purple_700,
                        com.dullfan.base.R.color.main_drawer_select_color
                    )
                } else {
                    rvSelectColor(
                        rvDataBinding,
                        com.dullfan.base.R.color.main_drawer_no_select_text_color,
                        com.dullfan.base.R.color.white
                    )
                }

                rvDataBinding.itemProjectDetailsDrawerText.text = data

                rvDataBinding.root.setOnClickListener {
                    allTaskFragmentIsShow = true
                    index = position
                    viewDataBinding.projectDetailsDrawer.closeDrawer(Gravity.RIGHT)
                    when (position) {
                        0 -> {
                            viewDataBinding.projectDetailsFramelayout.visibility = View.GONE
                            viewDataBinding.projectDetailsIntroduceLayout.visibility = View.VISIBLE
                            initIntroduce()
                        }
                        1 -> {
                            viewDataBinding.projectDetailsFramelayout.visibility = View.VISIBLE
                            viewDataBinding.projectDetailsIntroduceLayout.visibility = View.GONE
                            startFragment(
                                viewDataBinding.projectDetailsFramelayout.id,
                                TaskFragment(viewDataBinding)
                            )
                        }
                        2 -> {
                            viewDataBinding.projectDetailsFramelayout.visibility = View.VISIBLE
                            viewDataBinding.projectDetailsIntroduceLayout.visibility = View.GONE
                            StatisticalDataMode = 1
                            startFragment(viewDataBinding.projectDetailsFramelayout.id, StatisticalDataFragment())
                        }
                    }
                }
            }
        }

        viewDataBinding.projectDetailsRv.layoutManager = LinearLayoutManager(requireContext())
        viewDataBinding.projectDetailsRv.adapter = baseRvAdapter

        viewDataBinding.projectDetailsDrawer.addDrawerListener(object :
            DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val content = viewDataBinding.projectDetailsDrawer.getChildAt(0)
                val scale = 1 - slideOffset
                content.translationX = -(drawerView.measuredWidth * (1 - scale))
            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
    }

    /**
     * 设置侧滑选中效果和未选中效果
     */
    private fun rvSelectColor(
        rvDataBinding: ItemProjectDetailsDrawerLayoutRvBinding,
        rvItemTextColor: Int,
        rvItemCardColor: Int,
    ) {
        rvDataBinding.itemProjectDetailsDrawerText.setTextColor(resources.getColor(rvItemTextColor))
        rvDataBinding.itemProjectDetailsDrawerCard.setCardBackgroundColor(
            resources.getColor(
                rvItemCardColor
            )
        )
    }
}