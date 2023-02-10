package com.dullfan.module_main.ui.project.details

import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.dullfan.base.adapter.BaseCeilingAdapter
import com.dullfan.base.adapter.BaseFunAdapter
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.bean.RecyclerViewItemBean
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.durationToYearAndMin
import com.dullfan.base.utils.myClickListener
import com.dullfan.base.utils.showAlertDialog
import com.dullfan.base.utils.showLog
import com.dullfan.base.view.MyRecyclerItem
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.FocusedExperienceBean
import com.dullfan.module_main.databinding.DialogTaskFocusedDetailsLayoutBinding
import com.dullfan.module_main.databinding.FragmentTaskFocusedBinding
import com.dullfan.module_main.databinding.ItemDialogFocusedItemBinding
import com.dullfan.module_main.databinding.ItemTaskExperienceLayoutBinding
import com.dullfan.module_main.ui.project.details.TaskFragment.Companion.taskFragmentIsShow
import com.dullfan.module_main.utils.currentaskId
import com.dullfan.module_main.utils.projectDetailsData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import org.json.JSONObject

class TaskFocusedFragment : BaseFragment() {
    val binding by lazy {
        FragmentTaskFocusedBinding.inflate(layoutInflater)
    }

    val myRecyclerItem by lazy {
        MyRecyclerItem(requireContext())
    }

    var sortFlag: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        taskFragmentIsShow = true
        initSpinner()
        initData(currentaskId, sortFlag)
        initSort()
        return binding.root
    }

    private fun initSort() {
        binding.sort.setOnClickListener(myClickListener {
            sortFlag = !sortFlag
            ObjectAnimator.ofFloat(binding.sort, "rotation", 0f, 180f)
                .setDuration(500)
                .start()
            if (sortFlag) {
                showToast("切换倒序显示")
            } else {
                showToast("切换升序显示")
            }
            initData(currentaskId, sortFlag)
        })
    }

    private fun initSpinner() {
        val whereEqualTo =
            LCQuery<LCObject>("Tasks").whereEqualTo("project_id", projectDetailsData.objectId)
        whereEqualTo.findInBackground().subscribe(myObserver {
            val spinnerList = ArrayList<String>()
            var currenIndex = 0
            it.forEachIndexed { index, lcObject ->
                spinnerList.add(lcObject.getString("task_title"))
                if (currentaskId == lcObject.objectId) {
                    currenIndex = index;
                }
            }
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                com.airbnb.lottie.R.layout.support_simple_spinner_dropdown_item,
                spinnerList
            )
            binding.taskFocusedSpinner.setText(it.get(currenIndex).getString("task_title"))
            binding.taskFocusedSpinner.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    currentaskId = it[position].objectId
                    initData(
                        it[position].objectId,
                        sortFlag
                    )
                }
            binding.taskFocusedSpinner.setAdapter(arrayAdapter)
        })
    }

    private fun initData(currentaskId: String, sortFlag: Boolean) {
        binding.taskFocusedSpinner.setDropDownBackgroundDrawable(
            ColorDrawable(
                resources.getColor(
                    com.dullfan.base.R.color.white
                )
            )
        )
        LCQuery<LCObject>("FocusMode").whereEqualTo("TaskId", currentaskId)
            .findInBackground().subscribe(myObserver(onError = {
                it.printStackTrace()
            }, onNext = {
                var dataList = it
                if (sortFlag) {
                    dataList = it.reversed()
                }
                val list = ArrayList<RecyclerViewItemBean<LCObject>>()
                dataList.forEach {
                    val focusModeOffTime = it.getString("FocusModeOffTime")
                    val substring = focusModeOffTime.substring(0, 10)
                    list.add(RecyclerViewItemBean(substring, it))
                }
                if (list.size == 0) {
                    binding.lottieNullData.visibility = View.VISIBLE
                    binding.rv.visibility = View.GONE
                } else {
                    binding.rv.removeItemDecoration(myRecyclerItem)
                    binding.lottieNullData.visibility = View.GONE
                    binding.rv.visibility = View.VISIBLE
                    binding.rv.addItemDecoration(myRecyclerItem)
                    binding.rv.adapter = BaseCeilingAdapter(
                        R.layout.item_dialog_focused_item,
                        list
                    ) { view, data, position ->
                        val contentTextView =
                            view.findViewById<TextView>(R.id.item_dialog_focused_content)
                        val durationTextView =
                            view.findViewById<TextView>(R.id.item_dialog_focused_duration)
                        val timeTextView =
                            view.findViewById<TextView>(R.id.item_dialog_focused_time)
                        val cardView =
                            view.findViewById<MaterialCardView>(R.id.item_dialog_focused_card)

                        val startTime = data.data.getString("FocusModeOnTime")
                        val endTime = data.data.getString("FocusModeOffTime")
                        timeTextView.text = "${startTime.substring(11, startTime.length)} - ${
                            endTime.substring(
                                11,
                                startTime.length
                            )
                        }"
                        contentTextView.text = data.data.getString("FocusModeExperienceContent")
                        durationTextView.text =
                            "${durationToYearAndMin(data.data.getInt("FocusModeDuration"))}"

                        //点击事件
                        cardView.setOnClickListener(myClickListener {
                            val bottomSheetDialog =
                                BottomSheetDialog(requireContext(), R.style.CustomDialog)
                            bottomSheetDialog.setCanceledOnTouchOutside(false)
                            val dialogTaskFocusedDetailsLayoutBinding =
                                DialogTaskFocusedDetailsLayoutBinding.inflate(layoutInflater)
                            bottomSheetDialog.setContentView(dialogTaskFocusedDetailsLayoutBinding.root)
                            bottomSheetDialog.show()
                            dialogTaskFocusedDetailsLayoutBinding.title.setText(binding.taskFocusedSpinner.text.toString())
                            dialogTaskFocusedDetailsLayoutBinding.content.setText(
                                data.data.getString(
                                    "FocusModeExperienceContent"
                                )
                            )
                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsStartTime.setText(
                                data.data.getString("FocusModeOnTime")
                            )
                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsEndTime.setText(
                                data.data.getString("FocusModeOffTime")
                            )
                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsTime.text =
                                "${durationToYearAndMin(data.data.getInt("FocusModeDuration"))}"
                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsOutTime.text =
                                "${durationToYearAndMin(data.data.getInt("FocusModePauseDuration"))}"
                            dialogTaskFocusedDetailsLayoutBinding.back.setOnClickListener(
                                myClickListener { bottomSheetDialog.dismiss() })
                            dialogTaskFocusedDetailsLayoutBinding.cardEdit.setOnClickListener(
                                myClickListener {
                                    if (dialogTaskFocusedDetailsLayoutBinding.content.isEnabled) {
                                        val content =
                                            dialogTaskFocusedDetailsLayoutBinding.content.text.toString()
                                        val createWithoutData = LCObject.createWithoutData(
                                            "FocusMode",
                                            data.data.objectId
                                        )
                                        createWithoutData.put("FocusModeExperienceContent", content)
                                        createWithoutData.saveInBackground()
                                            .subscribe(myObserver {
                                                initData(com.dullfan.module_main.utils.currentaskId, sortFlag)
                                                showToast("保存成功")
                                            })

                                        dialogTaskFocusedDetailsLayoutBinding.editText.text = "编辑心得"
                                    } else {
                                        dialogTaskFocusedDetailsLayoutBinding.editText.text = "保存"
                                        showToast("可以编辑心得啦!")
                                    }
                                    dialogTaskFocusedDetailsLayoutBinding.content.isEnabled =
                                        !dialogTaskFocusedDetailsLayoutBinding.content.isEnabled
                                })

                            dialogTaskFocusedDetailsLayoutBinding.cardDel.setOnClickListener(
                                myClickListener {
                                    showAlertDialog(requireContext(),"确定要删除专注记录吗?删除后无法恢复了哦!", positiveButtonClick = {
                                        val createWithoutData = LCObject.createWithoutData(
                                            "FocusMode",
                                            data.data.objectId
                                        )
                                        createWithoutData.deleteInBackground().subscribe(myObserver {
                                            initData(com.dullfan.module_main.utils.currentaskId, sortFlag)
                                            bottomSheetDialog.dismiss()
                                            showToast("删除成功")
                                        })
                                    })
                                })
                            if (data.data.getString("ExperienceContentList") == "null") {
                                dialogTaskFocusedDetailsLayoutBinding.dialogTaskDetailsCard01.visibility =
                                    View.GONE
                            } else {
                                dialogTaskFocusedDetailsLayoutBinding.dialogTaskDetailsCard01.visibility =
                                    View.VISIBLE
                                val jsonArray =
                                    JSONArray(data.data.getString("ExperienceContentList"))
                                val experienceBaseList = ArrayList<FocusedExperienceBean>()
                                for (i in 0 until jsonArray.length()) {
                                    experienceBaseList.add(
                                        FocusedExperienceBean(
                                            jsonArray.getJSONObject(
                                                i
                                            ).getString("content"),
                                            jsonArray.getJSONObject(i).getString("date")
                                        )
                                    )
                                }

                                dialogTaskFocusedDetailsLayoutBinding.taskDetailsRv.adapter =
                                    BaseFunAdapter(
                                        R.layout.item_task_experience_layout,
                                        experienceBaseList
                                    ) { view, data, position ->
                                        val itemTaskExperienceLayoutBinding =
                                            ItemTaskExperienceLayoutBinding.bind(view)
                                        itemTaskExperienceLayoutBinding.itemTaskExperienceTitle.setText(
                                            data.content
                                        )
                                        itemTaskExperienceLayoutBinding.itemTaskExperienceTime.setText(
                                            data.date
                                        )
                                    }


                            }
                        })
                    }
                }
            }))
    }
}