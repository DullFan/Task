package com.dullfan.module_main.ui.project

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import cn.leancloud.LCUser
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.getLCFileImageUrl
import com.dullfan.base.lc.getLCUserIndoData
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.*
import com.dullfan.library_common.utils.viewModels
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.MainDrawerLayoutItemBean
import com.dullfan.module_main.databinding.FragmentProjectBinding
import com.dullfan.module_main.databinding.ItemProjectRvLayoutBinding
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.utils.projectDataList
import com.dullfan.module_main.utils.projectDetailsData
import com.dullfan.module_main.utils.showBottomAddProjectDialog
import com.dullfan.module_main.viewmodel.HomeViewModel
import com.scwang.smart.refresh.header.BezierRadarHeader

class ProjectFragment : BaseFragment() {

    companion object{
        @JvmStatic
        fun newInstance():ProjectFragment{
            return instance!!
        }

        private var instance:ProjectFragment? = null
            get() {
                if(field == null){
                    field = ProjectFragment()
                }
                return field
            }
    }


    private val viewDataBinding by lazy {
        FragmentProjectBinding.inflate(layoutInflater)
    }

    val viewModel: HomeViewModel by viewModels()

    var floatButtonIsFlag = true
    var flag = true

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initBottomSheetDialog()
        //只让Fragment初始化一次
        if (flag) {
            viewDataBinding.projectRv.layoutManager = LinearLayoutManager(requireContext())
            viewDataBinding.projectRv.adapter = rvAdapter
        }
        flag = false

        //刷新
        viewDataBinding.projectSmart.setRefreshHeader(BezierRadarHeader(requireContext()))
        viewDataBinding.projectSmart.setOnRefreshListener { initRv() }
        return viewDataBinding.root
    }

    val rvAdapter = object : BaseRvAdapter<LCObject>(R.layout.item_project_rv_layout, ArrayList()) {
        override fun onBind(
            rvDataBinding: ViewDataBinding,
            data: LCObject,
            position: Int
        ) {
            rvDataBinding as ItemProjectRvLayoutBinding
            getLCFileImageUrl(
                data["project_avatar"] as String,
                rvDataBinding.itemProjectAvatar,
                requireContext()
            )

            rvDataBinding.itemProjectTitle.text =
                data["project_name"].toString()
            rvDataBinding.itemProjectCreateTime.text =
                "创建于${data.createdAtString.substring(0, 10)}"
            rvDataBinding.itemProjectId.text = data.objectId

            emergencyLevelSet(
                data["project_priority_level"].toString(),
                rvDataBinding.itemProjectArrowImg,
                rvDataBinding.itemProjectArrowText,
                requireContext()
            )

            rvDataBinding.root.setOnClickListener {
                projectDetailsData = data
                (activity as MainActivity).startDetailsFragment()
            }
        }
    }


    /**
     * 初始化页面信息
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initRv() {
        val createLcQuery = LCQuery<LCObject>("Projects")
        createLcQuery.whereEqualTo("project_creator_id", LCUser.getCurrentUser().objectId)
        val query = LCQuery.or(listOf(createLcQuery))

        query.findInBackground().subscribe(
            myObserver(onError = { e -> e.printStackTrace() },
                onNext = { t ->
                    if (t.size != 0) {
                        viewDataBinding.projectRv.visibility = View.VISIBLE
                        viewDataBinding.projectLottie.visibility = View.GONE
                        rvAdapter.dataList = t
                    } else {
                        viewDataBinding.projectRv.visibility = View.GONE
                        viewDataBinding.projectLottie.visibility = View.VISIBLE
                    }
                    viewDataBinding.projectSmart.finishRefresh()
                })
        )

        //滑动判断
        viewDataBinding.projectRv.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 0 && floatButtonIsFlag) {
                floatButtonIsFlag = false
            } else if (scrollY - oldScrollY < 0 && !floatButtonIsFlag) {
                val mShowAction: Animation = AnimationUtils.loadAnimation(
                    context, R.anim.project_float_button_in_anim
                )
                floatButtonIsFlag = true
                viewDataBinding.projectFloatButton.visibility = View.VISIBLE
                viewDataBinding.projectFloatButton.startAnimation(mShowAction)
            }
        }
    }

    /**
     * 设置底部对话框
     */
    private fun initBottomSheetDialog() {
        viewDataBinding.projectFloatButton.setOnClickListener {
            val drawerLayoutItemBeanList = mutableListOf<MainDrawerLayoutItemBean>()

            drawerLayoutItemBeanList += MainDrawerLayoutItemBean(
                "项目",
                R.drawable.main_drawlayout_project_img
            )
            drawerLayoutItemBeanList += MainDrawerLayoutItemBean(
                "任务",
                R.drawable.dialog_task_img
            )
            showBottomAddProjectDialog(requireContext(), drawerLayoutItemBeanList) { position ->
                when (position) {
                    0 -> {
                        (activity as MainActivity).startAddProjectFragment()
                    }
                    1 -> {
                        projectDataList = rvAdapter.dataList
                        (activity as MainActivity).startAddProjectTaskFragment()
                    }
                }
            }
        }
    }

    /**
     * 页面恢复后重置
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        initRv()
        (activity as MainActivity).baseRvAdapter.index = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finish()
    }
}