package com.dullfan.module_main.ui

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils.loadAnimation
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.leancloud.LCObject
import cn.leancloud.LCUser
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseActivity
import com.dullfan.base.lc.getLCFileImageUrl
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.*
import com.dullfan.library_common.autoservice.LoginService
import com.dullfan.library_common.utils.CommonServiceLoader
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.MainDrawerLayoutItemBean
import com.dullfan.module_main.databinding.ActivityMainBinding
import com.dullfan.module_main.databinding.DialogFocusedLayoutBinding
import com.dullfan.module_main.databinding.ItemDrawerLayoutRvBinding
import com.dullfan.module_main.ui.calendar.CalendarFragment
import com.dullfan.module_main.ui.info.InFoFragment
import com.dullfan.module_main.ui.info.InFoFragment.Companion.isInFoFragmentFlag
import com.dullfan.module_main.ui.project.AddProjectFragment
import com.dullfan.module_main.ui.project.AddProjectTaskFragment
import com.dullfan.module_main.ui.project.ProjectDetailsFragment
import com.dullfan.module_main.ui.project.ProjectFragment
import com.dullfan.module_main.ui.project.details.FocusedFragment
import com.dullfan.module_main.ui.project.details.TaskFragment
import com.dullfan.module_main.ui.statistical_data.StatisticalDataFragment
import com.dullfan.module_main.utils.*
import com.dullfan.module_main.viewmodel.HomeViewModel
import com.dullfan.module_main.viewmodel.MainViewModel
import com.google.gson.Gson
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes


class MainActivity : BaseActivity() {

    val viewDataBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val viewModel: HomeViewModel by viewModels()

    lateinit var baseRvAdapter: BaseRvAdapter<MainDrawerLayoutItemBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewDataBinding.root)
        AppCenter.start(
            application,
            "20f03823-10d4-4551-a04e-4f84442e25ac",
            Analytics::class.java,
            Crashes::class.java
        )
        initDrawerLayout()
        getLCFileImageUrl(LCUser.getCurrentUser()["avatar"].toString(), viewModel.infoAvatarUri)
        initStartInfo()
        startFragment(viewDataBinding.mainFrame.id, ProjectFragment.newInstance())
        viewModel.infoAvatarUri.observe(this) {
            glide(it, viewDataBinding.mainHomeAvatar)
        }
    }

    /**
     * 点击头像跳转至个人中心
     */
    private fun initStartInfo() {
        viewDataBinding.mainHomeAvatar.setOnClickListener(myClickListener {
            if (InFoFragment.isInFoFragmentFlag) {
                startFragment(viewDataBinding.mainFrame.id, InFoFragment())
                baseRvAdapter.index = 100
                InFoFragment.isInFoFragmentFlag = false
            }
        })
    }

    /**
     * 更新头像
     */
    fun updateAvatar(newObjectId: String) {
        viewModel.findInfoAvatar(newObjectId)
    }

    /**
     * 返回上一级
     */
    fun infoNavigateUp() {
        outFragment(viewDataBinding.mainFrame.id, ProjectFragment())
        for (i in 0 until supportFragmentManager.fragments.size) {
            if (supportFragmentManager.fragments[i].isHidden) {
                supportFragmentManager.popBackStack(
                    supportFragmentManager.fragments[i].id,
                    POP_BACK_STACK_INCLUSIVE
                )
            }
        }
    }

    /**
     * 跳转至项目详情
     */
    fun startDetailsFragment() {
        startFragment(viewDataBinding.mainFrame.id, ProjectDetailsFragment())
    }

    /**
     * 跳转至数据统计
     */
    fun startStatisticalDataFragment() {
        startFragment(viewDataBinding.mainFrame.id, StatisticalDataFragment())
    }
    /**
     * 跳转至日程
     */
    fun startCalendarFragment() {
        startFragment(viewDataBinding.mainFrame.id, CalendarFragment())
    }

    /**
     * 跳转至添加项目
     */
    fun startAddProjectFragment() {
        startFragment(viewDataBinding.mainFrame.id, AddProjectFragment())
    }

    /**
     * 跳转至添加任务
     */
    fun startAddProjectTaskFragment() {
        startFragment(viewDataBinding.mainFrame.id, AddProjectTaskFragment())
    }


    /**
     * 退出登录
     */
    fun outLogin() {
        CommonServiceLoader.load(LoginService::class.java)?.startLoginActivity(this)
        LCUser.logOut()
        finish()
    }

    /**
     * 初始化DrawerLayout
     */
    private fun initDrawerLayout() {
        initDrawerLayoutRv()
        viewDataBinding.mainDrawerLayout.setScrimColor(resources.getColor(com.dullfan.base.R.color.main_drawerlayout_back_color))
        viewDataBinding.mainNavigation.setBackgroundColor(Color.TRANSPARENT)
        viewDataBinding.mainNavigationOut.setOnClickListener(myClickListener {
            showAlertDialog(context = this, message = "确定要退出登录吗?", positiveButtonClick = {
                outLogin()
            })
        })

        viewDataBinding.mainLayoutLogoFont.setOnClickListener(myClickListener {
            viewDataBinding.mainDrawerLayout.open()
        })

    }

    /**
     * 初始化RecyclerView
     */
    private fun initDrawerLayoutRv() {
        val drawerLayoutItemBeanList = mutableListOf<MainDrawerLayoutItemBean>()

        drawerLayoutItemBeanList += MainDrawerLayoutItemBean(
            "项目", R.drawable.main_drawlayout_project_img
        )
        drawerLayoutItemBeanList += MainDrawerLayoutItemBean(
            "日程", R.drawable.main_drawlayout_schedule_img
        )
        drawerLayoutItemBeanList += MainDrawerLayoutItemBean(
            "统计数据", R.drawable.main_drawlayout_data_img
        )

        viewDataBinding.mainNavigationRv.layoutManager = LinearLayoutManager(this)

        baseRvAdapter = object : BaseRvAdapter<MainDrawerLayoutItemBean>(
            R.layout.item_drawer_layout_rv, drawerLayoutItemBeanList
        ) {
            override fun onBind(
                rvDataBinding: ViewDataBinding, data: MainDrawerLayoutItemBean, position: Int
            ) {
                rvDataBinding as ItemDrawerLayoutRvBinding

                if (position == index) {
                    rvSelectColor(
                        rvDataBinding,
                        com.dullfan.base.R.color.purple_700,
                        com.dullfan.base.R.color.main_drawer_select_color,
                        true
                    )
                } else {
                    rvSelectColor(
                        rvDataBinding,
                        com.dullfan.base.R.color.main_drawer_no_select_text_color,
                        com.dullfan.base.R.color.white,
                        false
                    )
                }

                rvDataBinding.itemDrawerLayoutRvText.text = data.text
                rvDataBinding.itemDrawerLayoutRvImg.setImageResource(data.img)

                rvDataBinding.root.setOnClickListener {
                    index = position
                    viewDataBinding.mainDrawerLayout.close()
                    when (position) {
                        0 -> infoNavigateUp()
                        1 -> startCalendarFragment()
                        2 -> {
                            StatisticalDataMode = 0
                            startStatisticalDataFragment()
                        }
                    }
                    isInFoFragmentFlag = true
                }

            }
        }

        viewDataBinding.mainNavigationRv.adapter = baseRvAdapter
    }

    /**
     * 设置侧滑选中效果和未选中效果
     */
    private fun rvSelectColor(
        rvDataBinding: ItemDrawerLayoutRvBinding,
        rvItemTextColor: Int,
        rvItemCardColor: Int,
        isVisible: Boolean
    ) {
        rvDataBinding.itemDrawerLayoutRvText.setTextColor(resources.getColor(rvItemTextColor))
        rvDataBinding.itemDrawerLayoutRvCard.setCardBackgroundColor(
            resources.getColor(
                rvItemCardColor
            )
        )

        rvDataBinding.itemDrawerLayoutRvImg.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this@MainActivity, rvItemTextColor
            )
        )

        rvDataBinding.itemDrawerLayoutRvSelectLine.visibility =
            if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        //判断是否启动专注模式
        if (FocusedFragment.focusedFragmentFlag) {
            if (System.currentTimeMillis() - FocusedFragment.inTime > 60000) {
                val dialogFocusedLayoutBinding =
                    DialogFocusedLayoutBinding.inflate(LayoutInflater.from(this))
                val dialog = AlertDialog.Builder(this).create()
                dialog.setCanceledOnTouchOutside(false)
                dialog.setView(dialogFocusedLayoutBinding.root)
                dialog.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
                dialog.show()
                dialogFocusedLayoutBinding.dialogFocusedText03.setOnClickListener {
                    dialog.dismiss()
                }
                dialogFocusedLayoutBinding.dialogFocusedClone.setOnClickListener {
                    dialog.dismiss()
                }
                dialogFocusedLayoutBinding.dialogFocusedText02.setOnClickListener {
                    dialogFocusedLayoutBinding.group1.visibility = View.GONE
                    dialogFocusedLayoutBinding.group2.visibility = View.VISIBLE
                }
                dialogFocusedLayoutBinding.button.setOnClickListener(myClickListener {
                    taskEndTime = getNowDate()
                    val etString = dialogFocusedLayoutBinding.dialogFocusedEt.text.toString()
                    val timeDifference = timeDifference(taskStartTime, taskEndTime)
                    val lcObject = LCObject("FocusMode")
                    lcObject.put("FocusModeExperienceContent", etString)
                    lcObject.put("FocusModeOffTime", taskEndTime)
                    lcObject.put("FocusModeOnTime", taskStartTime)
                    lcObject.put("FocusModeDuration", timeDifference)
                    lcObject.put("TaskId", currentaskId)
                    lcObject.put("ProjectId", projectDetailsData.objectId)
                    lcObject.put("FocusModePauseDuration", FocusedFragment.pauseDuration)
                    if (FocusedFragment.experienceContentList.size != 0) {
                        lcObject.put(
                            "ExperienceContentList",
                            Gson().toJson(FocusedFragment.experienceContentList)
                        )
                    }
                    lcObject.saveInBackground().subscribe(myObserver(onError = {
                        showToast(it.message)
                    }, onNext = {
                        showToast("保存成功")
                        supportFragmentManager.popBackStack()
                        dialog.dismiss()
                    }))
                })
            } else {
                showToast("不记录1分钟以下的专注记录")
                super.onBackPressed()
            }
        //判断TaskFragment中是否有其他页面存在,有的话返回到项目详情页面
        } else if (TaskFragment.taskFragmentIsShow) {
            TaskFragment.taskFragmentIsShow = false
            supportFragmentManager.popBackStack()
        } else if (ProjectDetailsFragment.allTaskFragmentIsShow) {
            //判断TaskFragment页面是否存在,如果没有的话返回到项目列表
            infoNavigateUp()
            ProjectDetailsFragment.allTaskFragmentIsShow = false
        } else if(!isInFoFragmentFlag){
            supportFragmentManager.popBackStack()
            isInFoFragmentFlag = true
        }else {
            exit()
        }
    }

    var exitTime:Long = 0;

    fun exit(){
        if(System.currentTimeMillis() - exitTime > 2000){
            showToast("再按一次退出程序")
            exitTime = System.currentTimeMillis()
        }else{
            finish()
        }
    }

    fun titleLayout(flag: Boolean) {
        val loadAnimation = loadAnimation(
            this,
            if (flag) com.dullfan.base.R.anim.slide_pop_in_top else com.dullfan.base.R.anim.slide_out_top
        )

        val windowInsetsControllerCompat =
            WindowInsetsControllerCompat(window, window.decorView)
        //设置沉浸式
        if (flag) {
            windowInsetsControllerCompat.isAppearanceLightStatusBars = true
            window.statusBarColor = resources.getColor(com.dullfan.base.R.color.background_color)
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else {
            windowInsetsControllerCompat.isAppearanceLightStatusBars = false
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        viewDataBinding.mainCardView.startAnimation(loadAnimation)
        viewDataBinding.mainCardView.visibility = if (flag) View.VISIBLE else View.GONE
        viewDataBinding.mainDrawerLayout.setDrawerLockMode(if (flag) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun immersiveSetting() {
        val windowInsetsControllerCompat =
            WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsControllerCompat.isAppearanceLightStatusBars = false
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)


    }
}