package com.dullfan.module_main.ui.calendar

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.dullfan.base.adapter.BaseFunAdapter
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.*
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.FocusedExperienceBean
import com.dullfan.module_main.databinding.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors


class CalendarFragment : BaseFragment() {
    val binding by lazy {
        FragmentCalendarBinding.inflate(layoutInflater)
    }
    lateinit var vp2Adapter: BaseRvAdapter<Calendar>
    var leftSwipeNumber = 1
    var rightSwipeNumber = 1


    /**
     * true:正序
     * false:倒序
     */
    var sortFlag: Boolean = false
    var allDataList = mutableListOf<LCObject>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initData()
        return binding.root
    }

    private fun initData() {
        val query = LCQuery<LCObject>("FocusMode")
        query.findInBackground().subscribe(myObserver {
            allDataList = it
            initCalendarVp2()
        })
    }

    /**
     * 初始化日历
     */
    private fun initCalendarVp2() {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val mutableListOf = mutableListOf<Calendar>()

        for (i in 1..120) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -leftSwipeNumber)
            leftSwipeNumber++
            mutableListOf.add(0, calendar)
        }

        mutableListOf += Calendar.getInstance()
        mutableListOf += Calendar.getInstance().apply {
            add(Calendar.MONTH, +rightSwipeNumber)
            rightSwipeNumber++
        }

        vp2Adapter = object : BaseRvAdapter<Calendar>(R.layout.item_calendar_vp, mutableListOf) {
            override fun onBind(rvDataBinding: ViewDataBinding, data: Calendar, position: Int) {
                rvDataBinding as ItemCalendarVpBinding
                val dateBeanList = mutableListOf<DateBean>()

                for (i in 0 until getMonthOneDayWeek(data)) {
                    dateBeanList += DateBean(0, 0, 0)
                }
                for (i in 0 until getMonthMaxData(data)) {
                    dateBeanList += DateBean(
                        data.get(Calendar.YEAR),
                        data.get(Calendar.MONTH) + 1,
                        i + 1
                    )
                }
                val rvAdapter = object :
                    BaseRvAdapter<DateBean>(R.layout.item_calendar_rv_text, dateBeanList) {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onBind(
                        rvDataBinding: ViewDataBinding,
                        data: DateBean,
                        position: Int
                    ) {
                        rvDataBinding as ItemCalendarRvTextBinding

                        rvDataBinding.itemCalendarRvText.visibility =
                            if (data.day != 0) View.VISIBLE else View.GONE
                        rvDataBinding.itemCalendarRvText.text = "${data.day}"
                        rvDataBinding.itemCalendarRvCard.strokeWidth = 0
                        allDataList.forEach {
                            val nowMonth = if (data.month < 10) {
                                "0${data.month}"
                            } else {
                                "${data.month}"
                            }
                            val nowDay = if (data.day < 10) {
                                "0${data.day}"
                            } else {
                                "${data.day}"
                            }
                            if (it.getString("FocusModeOnTime")
                                    .contains("${data.year}-${nowMonth}-${nowDay}")
                            ) {
                                rvDataBinding.itemCalendarRvCard.strokeWidth = 5
                            }
                        }
                        if (index == position) {
                            rvDataBinding.itemCalendarRvText.setTextColor(resources.getColor(com.dullfan.base.R.color.white))
                            rvDataBinding.itemCalendarRvText.setBackgroundColor(
                                resources.getColor(
                                    com.dullfan.base.R.color.purple_700
                                )
                            )
                            val nowMonth = if (data.month < 10) {
                                "0${data.month}"
                            } else {
                                "${data.month}"
                            }
                            val nowDay = if (data.day < 10) {
                                "0${data.day}"
                            } else {
                                "${data.day}"
                            }
                            binding.calendarDateDay.text = "${nowDay}号"
                            initIndexData("${data.year}-${nowMonth}-${nowDay}")
                        } else {
                            rvDataBinding.itemCalendarRvText.setTextColor(resources.getColor(com.dullfan.base.R.color.black))
                            rvDataBinding.itemCalendarRvText.setBackgroundColor(
                                resources.getColor(
                                    com.dullfan.base.R.color.background_color
                                )
                            )
                        }
                        rvDataBinding.root.setOnClickListener(myClickListener {
                            if(data.day != 0){
                                index = position
                            }
                        })
                    }

                    /**
                     * 处理选中事件
                     */
                    @RequiresApi(Build.VERSION_CODES.N)
                    private fun initIndexData(date: String) {
                        val newDataList = mutableListOf<LCObject>()
                        allDataList.forEach {
                            if (it.getString("FocusModeOnTime").contains(date)) {
                                newDataList += it
                            }
                        }

                        if (newDataList.size == 0) {
                            binding.calendarNull.visibility = View.VISIBLE
                            binding.calendarFocusedRv.visibility = View.INVISIBLE
                            binding.calendarView2.visibility = View.INVISIBLE
                        } else {
                            newDataList.sortBy {
                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                simpleDateFormat.parse(it.getString("FocusModeOnTime")).time
                            }
                            if (!sortFlag) {
                                newDataList.reverse()
                            }

                            binding.calendarNull.visibility = View.INVISIBLE
                            binding.calendarFocusedRv.visibility = View.VISIBLE
                            binding.calendarView2.visibility = View.VISIBLE
                            val query = LCQuery<LCObject>("Tasks")
                            query.findInBackground().subscribe(myObserver { taskList ->
                                val calendarFocusedRvAdapter = object : BaseRvAdapter<LCObject>(
                                    R.layout.item_calendar_focused_layout,
                                    newDataList
                                ) {
                                    override fun onBind(
                                        rvDataBinding: ViewDataBinding,
                                        data: LCObject,
                                        position: Int
                                    ) {
                                        rvDataBinding as ItemCalendarFocusedLayoutBinding
                                        rvDataBinding.itemCalendarFocusedStartTime.text =
                                            data.getString("FocusModeOnTime").substring(11, 16)
                                        rvDataBinding.itemCalendarFocusedEndTime.text =
                                            data.getString("FocusModeOffTime").substring(11, 16)
                                        if (data.getString("FocusModeExperienceContent")
                                                .isEmpty()
                                        ) {
                                            rvDataBinding.itemCalendarFocusedContent.visibility =
                                                View.GONE
                                        } else {
                                            rvDataBinding.itemCalendarFocusedContent.visibility =
                                                View.VISIBLE
                                            rvDataBinding.itemCalendarFocusedContent.text =
                                                data.getString("FocusModeExperienceContent")
                                        }
                                        rvDataBinding.itemCalendarFocusedDate.text =
                                            durationToYearAndMin(data.getInt("FocusModeDuration"))
                                        taskList.forEach {
                                            if (it.objectId == data.getString("TaskId")) {
                                                rvDataBinding.itemCalendarFocusedTitle.text =
                                                    it.getString("task_title")
                                            }
                                        }
                                        rvDataBinding.root.setOnClickListener(myClickListener {
                                            val bottomSheetDialog =
                                                BottomSheetDialog(
                                                    requireContext(),
                                                    R.style.CustomDialog
                                                )
                                            bottomSheetDialog.setCanceledOnTouchOutside(false)
                                            val dialogTaskFocusedDetailsLayoutBinding =
                                                DialogTaskFocusedDetailsLayoutBinding.inflate(
                                                    layoutInflater
                                                )
                                            bottomSheetDialog.setContentView(
                                                dialogTaskFocusedDetailsLayoutBinding.root
                                            )
                                            bottomSheetDialog.show()
                                            dialogTaskFocusedDetailsLayoutBinding.title.setText(
                                                rvDataBinding.itemCalendarFocusedTitle.text.toString()
                                            )
                                            dialogTaskFocusedDetailsLayoutBinding.content.setText(
                                                data.getString(
                                                    "FocusModeExperienceContent"
                                                )
                                            )
                                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsStartTime.setText(
                                                data.getString("FocusModeOnTime")
                                            )
                                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsEndTime.setText(
                                                data.getString("FocusModeOffTime")
                                            )
                                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsTime.text =
                                                "${durationToYearAndMin(data.getInt("FocusModeDuration"))}"
                                            dialogTaskFocusedDetailsLayoutBinding.dialogTaskFocusedDetailsOutTime.text =
                                                "${durationToYearAndMin(data.getInt("FocusModePauseDuration"))}"
                                            dialogTaskFocusedDetailsLayoutBinding.back.setOnClickListener(
                                                myClickListener { bottomSheetDialog.dismiss() })
                                            dialogTaskFocusedDetailsLayoutBinding.cardEdit.setOnClickListener(
                                                myClickListener {
                                                    if (dialogTaskFocusedDetailsLayoutBinding.content.isEnabled) {
                                                        val content =
                                                            dialogTaskFocusedDetailsLayoutBinding.content.text.toString()
                                                        val createWithoutData =
                                                            LCObject.createWithoutData(
                                                                "FocusMode",
                                                                data.objectId
                                                            )
                                                        createWithoutData.put(
                                                            "FocusModeExperienceContent",
                                                            content
                                                        )
                                                        createWithoutData.saveInBackground()
                                                            .subscribe(myObserver {
                                                                LCQuery<LCObject>("FocusMode").findInBackground()
                                                                    .subscribe(myObserver {
                                                                        allDataList = it
                                                                        initIndexData(date)
                                                                        showToast("保存成功")
                                                                    })
                                                            })

                                                        dialogTaskFocusedDetailsLayoutBinding.editText.text =
                                                            "编辑心得"
                                                    } else {
                                                        dialogTaskFocusedDetailsLayoutBinding.editText.text =
                                                            "保存"
                                                        showToast("可以编辑心得啦!")
                                                    }
                                                    dialogTaskFocusedDetailsLayoutBinding.content.isEnabled =
                                                        !dialogTaskFocusedDetailsLayoutBinding.content.isEnabled
                                                })

                                            dialogTaskFocusedDetailsLayoutBinding.cardDel.setOnClickListener(
                                                myClickListener {
                                                    showAlertDialog(
                                                        requireContext(),
                                                        "确定要删除专注记录吗?删除后无法恢复了哦!",
                                                        positiveButtonClick = {
                                                            val createWithoutData =
                                                                LCObject.createWithoutData(
                                                                    "FocusMode",
                                                                    data.objectId
                                                                )
                                                            createWithoutData.deleteInBackground()
                                                                .subscribe(myObserver {
                                                                    LCQuery<LCObject>("FocusMode").findInBackground()
                                                                        .subscribe(myObserver {
                                                                            allDataList = it
                                                                            initIndexData(date)
                                                                            bottomSheetDialog.dismiss()
                                                                            showToast("删除成功")
                                                                        })

                                                                })
                                                        })
                                                })
                                            if (data.getString("ExperienceContentList") == "null") {
                                                dialogTaskFocusedDetailsLayoutBinding.dialogTaskDetailsCard01.visibility =
                                                    View.GONE
                                            } else {
                                                dialogTaskFocusedDetailsLayoutBinding.dialogTaskDetailsCard01.visibility =
                                                    View.VISIBLE
                                                val jsonArray =
                                                    JSONArray(data.getString("ExperienceContentList"))
                                                val experienceBaseList =
                                                    ArrayList<FocusedExperienceBean>()
                                                for (i in 0 until jsonArray.length()) {
                                                    experienceBaseList.add(
                                                        FocusedExperienceBean(
                                                            jsonArray.getJSONObject(
                                                                i
                                                            ).getString("content"),
                                                            jsonArray.getJSONObject(i)
                                                                .getString("date")
                                                        )
                                                    )
                                                }

                                                dialogTaskFocusedDetailsLayoutBinding.taskDetailsRv.adapter =
                                                    BaseFunAdapter(
                                                        R.layout.item_task_experience_layout,
                                                        experienceBaseList
                                                    ) { view, data, position ->
                                                        val itemTaskExperienceLayoutBinding =
                                                            ItemTaskExperienceLayoutBinding.bind(
                                                                view
                                                            )
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
                                binding.calendarFocusedRv.layoutManager =
                                    LinearLayoutManager(requireContext())
                                binding.calendarFocusedRv.adapter = calendarFocusedRvAdapter
                                binding.calendarSort.setOnClickListener(myClickListener {
                                    sortFlag = !sortFlag
                                    ObjectAnimator.ofFloat(
                                        binding.calendarSort,
                                        "rotation",
                                        0f,
                                        180f
                                    )
                                        .setDuration(500)
                                        .start()
                                    if (sortFlag) {
                                        showToast("切换升序显示")
                                    } else {
                                        showToast("切换倒序显示")
                                    }
                                    newDataList.reverse()
                                    calendarFocusedRvAdapter.notifyDataSetChanged()
                                })
                            })
                        }
                    }
                }
                rvDataBinding.itemCalendarVpRv.adapter = rvAdapter
                rvDataBinding.itemCalendarVpRv.isNestedScrollingEnabled = false
                dateBeanList.forEachIndexed { index, data ->
                    val timeString = simpleDateFormat.format(System.currentTimeMillis())
                    var nowMonth = if (data.month < 10) {
                        "0${data.month}"
                    } else {
                        "${data.month}"
                    }
                    var nowDay = if (data.day < 10) {
                        "0${data.day}"
                    } else {
                        "${data.day}"
                    }
                    if (timeString.contains("${data.year}-${nowMonth}-${nowDay}")) {
                        rvAdapter.index = index + 1
                    } else if (data.day == 1) {
                        rvAdapter.index = index
                    }
                }
            }
        }
        binding.calendarVp2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.calendarDate.text = "${mutableListOf[position].get(Calendar.YEAR)}年${
                    (mutableListOf[position].get(Calendar.MONTH) + 1)
                }月"

                if (position == mutableListOf.size - 1) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, +rightSwipeNumber)
                    rightSwipeNumber++
                    mutableListOf.add(mutableListOf.size, calendar)
                    vp2Adapter.notifyItemChanged(position)
                }
                val recyclerView = binding.calendarVp2.getChildAt(0) as RecyclerView
                val view = recyclerView.layoutManager?.findViewByPosition(position)
                if (view != null) {
                    updatePagerHeightForChild(view, binding.calendarVp2)
                }
            }
        })

        binding.calendarVp2.adapter = vp2Adapter
        binding.calendarVp2.setCurrentItem(mutableListOf.size - 2, false)


    }

    /**
     * 动态测量ViewPager2高度
     */
    private fun updatePagerHeightForChild(view: View, calendarVp2: ViewPager2) {
        view.post {
            val weightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(weightMeasureSpec, heightMeasureSpec)
            if (calendarVp2.layoutParams.height != view.measuredHeight) {
                val layoutParams = calendarVp2.layoutParams
                layoutParams.height = view.measuredHeight
                calendarVp2.layoutParams = layoutParams
            }
        }
    }
}

data class DateBean(val year: Int, val month: Int, val day: Int)