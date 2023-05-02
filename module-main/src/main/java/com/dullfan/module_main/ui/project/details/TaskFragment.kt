package com.dullfan.module_main.ui.project.details

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.dullfan.base.adapter.BaseFunAdapter
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.deleteLineText
import com.dullfan.base.utils.myClickListener
import com.dullfan.base.utils.showLog
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.*
import com.dullfan.module_main.ui.project.ProjectDetailsFragment.Companion.allTaskFragmentIsShow
import com.dullfan.module_main.ui.statistical_data.StatisticalDataFragment
import com.dullfan.module_main.utils.StatisticalDataMode
import com.dullfan.module_main.utils.currentaskId
import com.dullfan.module_main.utils.projectDetailsData
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TaskFragment() : BaseFragment() {
    lateinit var fragmentViewDataBinding: FragmentProjectDetailsBinding

    constructor(_fragmentViewDataBinding: FragmentProjectDetailsBinding) : this() {
        fragmentViewDataBinding = _fragmentViewDataBinding
    }

    val viewDataBinding by lazy {
        FragmentTaskBinding.inflate(layoutInflater)
    }

    companion object{
        //判断是否有界面显示
        var taskFragmentIsShow:Boolean = false
    }

    val baseAdapter: BaseRvAdapter<LCObject> =
        object : BaseRvAdapter<LCObject>(R.layout.item_task_layout, mutableListOf()) {
            override fun onBind(rvDataBinding: ViewDataBinding, data: LCObject, position: Int) {
                rvDataBinding as ItemTaskLayoutBinding
                rvDataBinding.itemTaskTitle.text = data.getString("task_title")

                rvDataBinding.itemTaskCreateTime.text =
                    "创建于${data.createdAtString.substring(0, 10)}"
                if (data.getBoolean("task_is_completed")) {
                    //设置删除线
                    deleteLineText(rvDataBinding.itemTaskTitle, true)
                    rvDataBinding.itemTaskStart.visibility = View.GONE
                } else {
                    deleteLineText(rvDataBinding.itemTaskTitle, false)
                    rvDataBinding.itemTaskStart.visibility = View.VISIBLE
                }

                rvDataBinding.root.setOnClickListener {
                    val dialogTaskDetailsLayoutBinding =
                        DialogTaskDetailsLayoutBinding.inflate(LayoutInflater.from(requireContext()))

                    val bottomSheetDialog =
                        BottomSheetDialog(requireContext(), R.style.CustomDialog)
                    bottomSheetDialog.setCanceledOnTouchOutside(false)
                    bottomSheetDialog.setContentView(dialogTaskDetailsLayoutBinding.root)
                    bottomSheetDialog.window!!.decorView.setBackgroundColor(Color.TRANSPARENT)
                    bottomSheetDialog.show()
                    initDialogLayout(
                        dialogTaskDetailsLayoutBinding,
                        bottomSheetDialog,
                        data
                    )
                }

                rvDataBinding.itemTaskStart.setOnClickListener {
                    currentaskId = data.objectId
                    startFragment(
                        fragmentViewDataBinding.projectDetailsFramelayout.id,
                        FocusedFragment(fragmentViewDataBinding)
                    )
                }
            }
        }

    /**
     * 对话框设置
     */
    private fun initDialogLayout(
        dialogTaskDetailsLayoutBinding: DialogTaskDetailsLayoutBinding,
        alertDialog: BottomSheetDialog,
        data: LCObject
    ) {
        dialogTaskDetailsLayoutBinding.back.setOnClickListener {
            alertDialog.dismiss()
        }
        dialogTaskDetailsLayoutBinding.title.setText(data.getString("task_title"))
        dialogTaskDetailsLayoutBinding.content.setText(data.getString("task_description"))
        if (data.getBoolean("task_is_completed")) {
            dialogTaskDetailsLayoutBinding.finishText.text = "继续"
            deleteLineText(dialogTaskDetailsLayoutBinding.title, true)
            deleteLineText(dialogTaskDetailsLayoutBinding.content, true)
        } else {
            dialogTaskDetailsLayoutBinding.finishText.text = "完成"
            //编辑点击事件
            dialogTaskDetailsLayoutBinding.cardEdit.setOnClickListener {
                if (dialogTaskDetailsLayoutBinding.title.isEnabled) {
                    val title = dialogTaskDetailsLayoutBinding.title.text.toString()
                    val content = dialogTaskDetailsLayoutBinding.content.text.toString()

                    if (title.isBlank()) {
                        showToast("请输入内容")
                        return@setOnClickListener
                    }

                    val lcObject = LCObject.createWithoutData("Tasks", data.objectId)
                    lcObject.put("task_description", content)
                    lcObject.put("task_title", title)
                    lcObject.saveInBackground().subscribe(myObserver(onError = {
                        showLog(it.message.toString())
                        showToast("错误了，需要删除这个Item")
                    }, onNext = {
                        dialogTaskDetailsLayoutBinding.title.isEnabled = false
                        dialogTaskDetailsLayoutBinding.content.isEnabled = false
                        dialogTaskDetailsLayoutBinding.editText.text = "编辑"
                        showToast("修改成功")
                        initRv()
                    }))

                } else {
                    dialogTaskDetailsLayoutBinding.title.isEnabled = true
                    dialogTaskDetailsLayoutBinding.content.isEnabled = true
                    dialogTaskDetailsLayoutBinding.editText.text = "确定"
                }
            }
        }

        //完成点击事件
        dialogTaskDetailsLayoutBinding.cardFinish.setOnClickListener {
            val lcObject = LCObject.createWithoutData("Tasks", data.objectId)
            lcObject.put("task_is_completed", !data.getBoolean("task_is_completed"))
            lcObject.saveInBackground().subscribe(myObserver {
                alertDialog.dismiss()
                showToast("设置成功")
                initRv()
            })
        }

        //删除点击事件
        dialogTaskDetailsLayoutBinding.cardDel.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("提醒")
                .setMessage("确定要删除吗(专注记录也会删除)？")
                .setNegativeButton("取消") { dialog, flag -> }
                .setPositiveButton("确定") { dialog, flag ->
                    val delWithoutData = LCObject.createWithoutData("Tasks", data.objectId)
                    delWithoutData.deleteInBackground().subscribe(myObserver {
                        LCQuery<LCObject>("FocusMode")
                            .whereEqualTo("TaskId", data.objectId)
                            .findInBackground()
                            .subscribe(
                                myObserver(onError = {
                                    it.printStackTrace()
                                },
                                    onNext = {
                                        LCObject.deleteAllInBackground(it)
                                    })
                            )

                    })
                    showToast("删除成功")
                    alertDialog.dismiss()
                    initRv()
                }.show()
        }

        dialogTaskDetailsLayoutBinding.cardTaskStatistics.setOnClickListener(myClickListener {
            StatisticalDataMode = 2
            alertDialog.dismiss()
            currentaskId = data.objectId
            taskFragmentIsShow = true
            startFragment(fragmentViewDataBinding.projectDetailsFramelayout.id, StatisticalDataFragment())
        })

        dialogTaskDetailsLayoutBinding.cardTaskFocused.setOnClickListener(myClickListener {
            currentaskId = data.objectId
            alertDialog.dismiss()
            startFragment(
                fragmentViewDataBinding.projectDetailsFramelayout.id,
                TaskFocusedFragment()
            )
        })

        LCQuery<LCObject>("FocusMode").whereEqualTo("TaskId", data.objectId)
            .findInBackground().subscribe(myObserver(onError = {
                it.printStackTrace()
            }, onNext = {
                dialogTaskDetailsLayoutBinding.taskDetailsNumber.text = "${it.size}"
                var number = 0
                it.forEach {
                    number += it.getInt("FocusModeDuration")
                }
                dialogTaskDetailsLayoutBinding.taskDetailsMin.text = "${number / 60}"
            }))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initAddTask()
        initRv()

        return viewDataBinding.root
    }

    private fun initRv() {
        viewDataBinding.rv.layoutManager = LinearLayoutManager(requireContext())
        viewDataBinding.rv.adapter = baseAdapter
        val whereEqualTo =
            LCQuery<LCObject>("Tasks").whereEqualTo("project_id", projectDetailsData.objectId)
        whereEqualTo.findInBackground().subscribe(myObserver {
            if (it.size == 0) {
                viewDataBinding.rv.visibility = View.GONE
                viewDataBinding.lottieNullData.visibility = View.VISIBLE
            } else {
                it.reverse()
                baseAdapter.dataList = it
                viewDataBinding.rv.visibility = View.VISIBLE
                viewDataBinding.lottieNullData.visibility = View.GONE
            }
        })
    }

    private fun initAddTask() {
        viewDataBinding.projectFloatButton.setOnClickListener {
            startFragment(
                fragmentViewDataBinding.projectDetailsFramelayout.id,
                AddTaskFragment(fragmentViewDataBinding)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fragmentViewDataBinding.isInitialized) {
            (fragmentViewDataBinding.projectDetailsRv.adapter as BaseRvAdapter<String>).index = 0
            fragmentViewDataBinding.projectDetailsFramelayout.visibility = View.GONE
            fragmentViewDataBinding.projectDetailsIntroduceLayout.visibility = View.VISIBLE
        }
    }
}