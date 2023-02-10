package com.dullfan.module_main.ui.project.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentAddTaskBinding
import com.dullfan.module_main.databinding.FragmentProjectDetailsBinding
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.utils.projectDetailsData
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.resources.MaterialAttributes.resolveOrThrow

class AddTaskFragment() : BaseFragment() {
    val binding by lazy {
        FragmentAddTaskBinding.inflate(layoutInflater)
    }

    lateinit var fragmentViewDataBinding: FragmentProjectDetailsBinding

    constructor(_fragmentViewDataBinding: FragmentProjectDetailsBinding) : this() {
        fragmentViewDataBinding = _fragmentViewDataBinding
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        TaskFragment.taskFragmentIsShow = true
        initSet()
        initAdd()
        return binding.root
    }

    /**
     * 添加任务
     */
    private fun initAdd() {
        binding.addProjectButton.setOnClickListener {
            collapseTheSoftKeyboard()
            val name = binding.addProjectName.text.toString()
            val detail = binding.addProjectDetail.text.toString()
            if (name.isBlank() || detail.isBlank()) {
                showToast("请输入内容")
                return@setOnClickListener
            }
            val lcObject = LCObject("Tasks")
            lcObject.put("project_id", projectDetailsData.objectId)
            lcObject.put("task_description", detail)
            lcObject.put("task_title", name)
            lcObject.saveInBackground().subscribe(myObserver(onNext = { t ->
                showAlerter(text = "添加成功")
                back()
            }, onError = {
                showAlerterError()
            }))
        }
    }

    /**
     * 基础设置
     */
    private fun initSet() {
        fragmentViewDataBinding.projectDetailsBack.visibility = View.GONE
        fragmentViewDataBinding.projectDetailsView01.visibility = View.GONE

        binding.addProjectBack.setOnClickListener {
            back()
        }
        binding.addProjectView01.setOnClickListener {
            back()
        }
    }

    private fun back() {
        fragmentViewDataBinding.projectDetailsBack.visibility = View.VISIBLE
        fragmentViewDataBinding.projectDetailsView01.visibility = View.VISIBLE
        outFragment(
            fragmentViewDataBinding.projectDetailsFramelayout.id,
            TaskFragment(fragmentViewDataBinding)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentViewDataBinding.projectDetailsBack.visibility = View.VISIBLE
        fragmentViewDataBinding.projectDetailsView01.visibility = View.VISIBLE
    }
}