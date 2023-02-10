package com.dullfan.module_main.ui.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cn.leancloud.LCObject
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentAddProjectTaskBinding
import com.dullfan.module_main.utils.projectDataList
import com.dullfan.module_main.utils.projectDetailsData


class AddProjectTaskFragment : BaseFragment() {
    val binding by lazy {
        FragmentAddProjectTaskBinding.inflate(layoutInflater)
    }
    var selectPosition = 0;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.addProjectTaskBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.addProjectTaskView01.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        initSpinner()
        initSubmit()




        return binding.root
    }

    /**
     * 提交事件
     */
    private fun initSubmit() {
        binding.addProjectTaskButton.setOnClickListener {
            collapseTheSoftKeyboard()
            val name = binding.addProjectTaskName.text.toString()
            val detail = binding.addProjectTaskDetail.text.toString()
            if (name.isBlank() || detail.isBlank()) {
                showToast("请输入内容")
                return@setOnClickListener
            }
            val lcObject = LCObject("Tasks")
            lcObject.put("project_id", projectDataList[selectPosition].objectId)
            lcObject.put("task_description", detail)
            lcObject.put("task_title", name)
            lcObject.saveInBackground().subscribe(myObserver(onNext = { t ->
                showAlerter(text = "添加成功")
                requireActivity().supportFragmentManager.popBackStack()
            }, onError = {
                showAlerterError()
            }))
        }
    }

    /**
     * 初始化下拉列表
     */
    private fun initSpinner() {
        val stringList = ArrayList<String>()
        projectDataList.forEach {
            stringList.add(it.getString("project_name"))
        }
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            com.airbnb.lottie.R.layout.support_simple_spinner_dropdown_item,
            stringList
        )
        binding.addProjectTaskSpinnerLayout.adapter = arrayAdapter
        binding.addProjectTaskSpinnerLayout.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

}