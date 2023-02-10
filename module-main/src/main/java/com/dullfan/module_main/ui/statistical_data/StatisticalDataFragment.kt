package com.dullfan.module_main.ui.statistical_data

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.*
import com.dullfan.module_main.R
import com.dullfan.module_main.databinding.FragmentStatisticalDataBinding
import com.dullfan.module_main.utils.StatisticalDataMode
import com.dullfan.module_main.utils.currentaskId
import com.dullfan.module_main.utils.projectDetailsData
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*


class StatisticalDataFragment : BaseFragment() {
    val binding by lazy {
        FragmentStatisticalDataBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initData()

        return binding.root
    }

    /**
     * 统计要处理的数据
     */
    private fun initData() {
        val query = LCQuery<LCObject>("FocusMode")
        when (StatisticalDataMode) {
            1 -> {
                query.whereEqualTo("ProjectId", projectDetailsData.objectId)
            }
            2 -> {
                query.whereEqualTo("TaskId", currentaskId)
            }
        }
        query.findInBackground().subscribe(myObserver {
            initCumulativeFocus(it)
            initNowDays(it)
            initFocusTimeDistribution(it)
            initMonthlyFocus(it)
            initYearFocus(it)
        })

    }

    /**
     * 年度专注时长统计
     */
    private fun initYearFocus(mutableList: MutableList<LCObject>) {
        val simpleDateFormat = SimpleDateFormat("yyyy")
        binding.statisticalDataYearFocusTimeDate.text =
            simpleDateFormat.format(System.currentTimeMillis())
        initYearFocusedTimeDateNewData(mutableList)
        binding.statisticalDataYearFocusTimeDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time =
                simpleDateFormat.parse(binding.statisticalDataMonthlyFocusTimeDate.text.toString())
            TimePickerBuilder(
                requireActivity()
            ) { date, v ->
                binding.statisticalDataYearFocusTimeDate.text = simpleDateFormat.format(date)
                initYearFocusedTimeDateNewData(mutableList)
            }.setType(booleanArrayOf(true, false, false, false, false, false))
                .setLabel("年", "", "", "", "", "")
                .setDate(calendar)
                .build()
                .show()
        }
    }

    private fun initYearFocusedTimeDateNewData(mutableList: MutableList<LCObject>) {
        val newDataMap = HashMap<String, Int>()
        mutableList.forEach {
            if (it.getString("FocusModeOnTime")
                    .contains(binding.statisticalDataYearFocusTimeDate.text.toString())
            ) {
                val day = it.getString("FocusModeOnTime").substring(0, 7)
                if (newDataMap.containsKey(day)) {
                    newDataMap[day] =
                        it.getInt("FocusModeDuration") + newDataMap[day]!!
                } else {
                    newDataMap[day] = it.getInt("FocusModeDuration")
                }
            }
        }
        initYearFocusLine(newDataMap, binding.statisticalDataYearFocusTimeDate.text.toString())
    }

    /**
     * 月度专注时长统计
     */
    private fun initMonthlyFocus(mutableList: MutableList<LCObject>) {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        binding.statisticalDataMonthlyFocusTimeDate.text =
            simpleDateFormat.format(System.currentTimeMillis())
        initMonthlyFocusedTimeDateNewData(mutableList, simpleDateFormat)

        binding.statisticalDataMonthlyFocusTimeDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time =
                simpleDateFormat.parse(binding.statisticalDataMonthlyFocusTimeDate.text.toString())
            TimePickerBuilder(
                requireActivity()
            ) { date, v ->
                binding.statisticalDataMonthlyFocusTimeDate.text = simpleDateFormat.format(date)
                initMonthlyFocusedTimeDateNewData(mutableList, simpleDateFormat)
            }.setType(booleanArrayOf(true, true, false, false, false, false))
                .setLabel("年", "月", "", "", "", "")
                .setDate(calendar)
                .build()
                .show()
        }
    }

    private fun initMonthlyFocusedTimeDateNewData(
        mutableList: MutableList<LCObject>,
        simpleDateFormat: SimpleDateFormat
    ) {
        val newDataMap = HashMap<String, Int>()
        mutableList.forEach {
            if (it.getString("FocusModeOnTime")
                    .contains(binding.statisticalDataMonthlyFocusTimeDate.text.toString())
            ) {
                val day = it.getString("FocusModeOnTime").substring(0, 10)
                if (newDataMap.containsKey(day)) {
                    newDataMap[day] =
                        it.getInt("FocusModeDuration") + newDataMap[day]!!
                } else {
                    newDataMap[day] = it.getInt("FocusModeDuration")
                }
            }
        }
        initMonthlyFocusLine(
            newDataMap,
            simpleDateFormat.parse(binding.statisticalDataMonthlyFocusTimeDate.text.toString())
        )
    }

    private fun initYearFocusLine(newDataHash: java.util.HashMap<String, Int>, nowYear: String) {
        val entryList = mutableListOf<Entry>()
        for (i in 1..12) {
            var monthly = ""
            monthly = if (i < 10) {
                "${nowYear}-0$i"
            } else {
                "${nowYear}-$i"
            }
            showLog(monthly)
            if (newDataHash.containsKey(monthly)) {
                entryList.add(Entry(i.toFloat(), newDataHash[monthly]!!.toFloat()))
            } else {
                entryList.add(Entry(i.toFloat(), 0f))
            }
        }
        val lineDataSet = LineDataSet(entryList, "")
        lineDataSet.setCircleColor(resources.getColor(com.dullfan.base.R.color.purple_700))
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.color = resources.getColor(com.dullfan.base.R.color.purple_700)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.line_chart_gradient)
        lineDataSet.fillDrawable = drawable
        lineDataSet.setDrawFilled(true)
        lineDataSet.valueTextSize = 8f
        lineDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${durationToYearAndMin(value.toInt())}"
            }
        }
        val lineData = LineData(lineDataSet)

        binding.statisticalDataYearFocusTimeLine.apply {
            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            description.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            zoom(0f, 0f, 0f, 0f)
            zoom(2f, 1f, 0f, 0f)
            isHighlightPerDragEnabled = false
            isHighlightPerTapEnabled = false

            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}月"
                }
            }

            axisLeft.setDrawGridLines(false)
            axisLeft.axisMinimum = 0f
            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${(value / 60).toInt()}分钟"
                }
            }
            animateXY(500, 500)
            data = lineData
        }
    }

    private fun initMonthlyFocusLine(newDataHash: HashMap<String, Int>, date: Date) {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val calendar = Calendar.getInstance()
        calendar.time = date
        val maximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val format = simpleDateFormat.format(date)
        val entryList = mutableListOf<Entry>()
        for (i in 1..maximum) {
            var day = ""
            day = if (i < 10) {
                "${format}-0$i"
            } else {
                "${format}-$i"
            }
            if (newDataHash.containsKey(day)) {
                entryList.add(Entry(i.toFloat(), newDataHash[day]!!.toFloat()))
            } else {
                entryList.add(Entry(i.toFloat(), 0f))
            }
        }
        val lineDataSet = LineDataSet(entryList, "")
        lineDataSet.setCircleColor(resources.getColor(com.dullfan.base.R.color.purple_700))
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.color = resources.getColor(com.dullfan.base.R.color.purple_700)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.line_chart_gradient)
        lineDataSet.fillDrawable = drawable
        lineDataSet.setDrawFilled(true)
        lineDataSet.valueTextSize = 8f
        lineDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${durationToYearAndMin(value.toInt())}"
            }
        }
        val lineData = LineData(lineDataSet)

        binding.statisticalDataMonthlyFocusTimeLine.apply {
            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            description.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            //防止数据更新导致再次放大
            zoom(0f, 0f, 0f, 0f)
            zoom(5f, 1f, 0f, 0f)
            isHighlightPerDragEnabled = false
            isHighlightPerTapEnabled = false

            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.mAxisMaximum = maximum.toFloat()
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${format.substring(format.length - 2, format.length)}-${value.toInt()}"
                }
            }

            axisLeft.setDrawGridLines(false)
            axisLeft.axisMinimum = 0f
            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${(value / 60).toInt()}分钟"
                }
            }
            animateXY(500, 500)
            data = lineData
        }
    }

    /**
     * 专注时长分布
     */
    private fun initFocusTimeDistribution(mutableList: MutableList<LCObject>) {
        LCQuery<LCObject>("Tasks").findInBackground().subscribe(myObserver { taskDataList ->
            val nowadays = getYearMonthDay()
            val newDataList = HashMap<String, Int>()
            binding.statisticalDataFocusTimeDistributionDate.text = nowadays

            mutableList.forEach { focused ->
                if (focused.getString("FocusModeOnTime").contains(nowadays)) {
                    taskDataList.forEach { task ->
                        initHashMap(focused, task, newDataList)
                    }
                }
            }
            var startTime = System.currentTimeMillis()
            var endTime = System.currentTimeMillis()
            initFocusTimeDistributionFinish(newDataList)
            binding.statisticalDataFocusTimeDistributionDate.setOnClickListener(myClickListener {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                val build = CalendarConstraints.Builder()
                    .build()
                val typedValue = TypedValue()
                requireContext().theme.resolveAttribute(
                    com.google.android.material.R.attr.materialCalendarTheme,
                    typedValue,
                    true
                )
                val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                    .setTheme(typedValue.data)
                    .setSelection(Pair(startTime, endTime))
                    .setCalendarConstraints(build)
                    .build()
                materialDatePicker.show(
                    requireActivity().supportFragmentManager,
                    materialDatePicker.toString()
                )
                materialDatePicker.addOnPositiveButtonClickListener {
                    val first = simpleDateFormat.format(it.first)
                    val second = simpleDateFormat.format(it.second)
                    startTime = it.first
                    endTime = it.second
                    if (first == second) {
                        binding.statisticalDataFocusTimeDistributionDate.text = first
                    } else {
                        binding.statisticalDataFocusTimeDistributionDate.text =
                            "${first} ~ ${second}"
                    }
                    val newDataList = HashMap<String, Int>()

                    mutableList.forEach { focused ->
                        val parse =
                            simpleDateFormat.parse(focused.getString("FocusModeOnTime")).time
                        if (startTime == endTime) {
                            if (focused.getString("FocusModeOnTime")
                                    .contains(binding.statisticalDataFocusTimeDistributionDate.text.toString())
                            ) {
                                taskDataList.forEach { task ->
                                    initHashMap(focused, task, newDataList)
                                }
                            }
                        } else {
                            if (parse in startTime - 28800000..endTime - 28800000) {
                                taskDataList.forEach { task ->
                                    initHashMap(focused, task, newDataList)
                                }
                            }
                        }
                    }
                    initFocusTimeDistributionFinish(newDataList)
                }
            })
        })
        binding.statisticalDataFocusTimeDistributionShare.setOnClickListener(myClickListener {
            showToast("该功能还未开通")
        })
    }

    private fun initHashMap(
        focused: LCObject,
        task: LCObject,
        newDataList: HashMap<String, Int>
    ) {
        if (focused.getString("TaskId") == task.objectId) {
            if (newDataList.containsKey(task.getString("task_title"))) {
                newDataList[task.getString("task_title")]?.let {
                    newDataList[task.getString("task_title")] =
                        it + focused.getInt("FocusModeDuration")
                }
            } else {
                newDataList[task.getString("task_title")] =
                    focused.getInt("FocusModeDuration")
            }
        }
    }

    private fun initFocusTimeDistributionFinish(
        newDataList: HashMap<String, Int>,
    ) {
        if (newDataList.size == 0) {
            binding.statisticalDataFocusTimeDistributionPie.visibility = View.GONE
            binding.statisticalDataFocusTimeDistributionPieNoData.visibility = View.VISIBLE
        } else {
            binding.statisticalDataFocusTimeDistributionPie.visibility = View.VISIBLE
            binding.statisticalDataFocusTimeDistributionPieNoData.visibility = View.GONE
            val pieEntryListData = mutableListOf<PieEntry>()
            val xStringList = mutableListOf<String>()
            newDataList.forEach {
                xStringList += durationToYearAndMin(it.value)
                pieEntryListData.add(
                    PieEntry(
                        it.value.toFloat(),
                        it.key
                    )
                )
            }

            val pieDataSet = PieDataSet(pieEntryListData, "")
            val colors = mutableListOf<Int>()
            for (i in 0 until newDataList.size) {
                val random = Random()
                val r: Int = random.nextInt(60) + 40
                val g: Int = random.nextInt(100) + 156
                val b: Int = random.nextInt(156) + 100
                colors.add(Color.rgb(r, g, b))
            }
            pieDataSet.colors = colors
            pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            pieDataSet.valueLinePart1Length = 0.5f
            val pieData = PieData(pieDataSet)
            pieData.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return durationToYearAndMin(value.toInt())
                }
            })
            binding.statisticalDataFocusTimeDistributionPie.apply {
                legend.isWordWrapEnabled = true
                legend.form = Legend.LegendForm.CIRCLE
                setCenterTextColor(Color.BLACK)
                setEntryLabelColor(Color.BLACK)
                isDrawHoleEnabled = false
                description.isEnabled = false
                animateXY(500, 500)
                data = pieData
                setNoDataText("暂无专注数据")
                invalidate()
            }
        }
    }

    /**
     * 当日专注
     */
    private fun initNowDays(
        mutableList: MutableList<LCObject>,
    ) {
        val nowadays = getYearMonthDay()
        val newDataList = mutableListOf<LCObject>()
        binding.statisticalDataNowadaysDate.text = nowadays
        mutableList.forEach {
            if (it.getString("FocusModeOnTime").contains(nowadays)) {
                newDataList += it
            }
        }
        initNowadays(newDataList)
        binding.statisticalDataNowadaysDate.setOnClickListener(myClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val time =
                simpleDateFormat.parse(binding.statisticalDataNowadaysDate.text.toString()).time + 86400000
            val build = CalendarConstraints.Builder()
                .build()
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setSelection(time)
                .setCalendarConstraints(build)
                .build()
            materialDatePicker.show(
                requireActivity().supportFragmentManager,
                materialDatePicker.toString()
            )
            materialDatePicker.addOnPositiveButtonClickListener {
                val format = simpleDateFormat.format(it)
                binding.statisticalDataNowadaysDate.text = format
                val newDataList = mutableListOf<LCObject>()
                mutableList.forEach {
                    if (it.getString("FocusModeOnTime").contains(format)) {
                        newDataList += it
                    }
                }
                initNowadays(newDataList)
            }
        })
    }

    /**
     * 当日专注设置
     */
    private fun initNowadays(
        mutableList: MutableList<LCObject>
    ) {
        binding.statisticalDataNowadaysNum1.text = mutableList.size.toString()
        var timeDate = 0
        mutableList.forEach {
            timeDate += it.getInt("FocusModeDuration")
        }
        durationToYearAndMin(timeDate) { year, min ->
            if (year == 0) {
                binding.statisticalDataNowadaysHour.visibility = View.GONE
                binding.statisticalDataNowadaysHourText.visibility = View.GONE
                binding.statisticalDataNowadaysMin.text = min.toString()
            } else {
                binding.statisticalDataNowadaysHour.visibility = View.VISIBLE
                binding.statisticalDataNowadaysHourText.visibility = View.VISIBLE
                binding.statisticalDataNowadaysMin.text = min.toString()
                binding.statisticalDataNowadaysHour.text = year.toString()
            }
        }

        var timeDatePause = 0
        mutableList.forEach {
            timeDatePause += it.getInt("FocusModePauseDuration")
        }
        durationToYearAndMin(timeDatePause) { year, min ->
            if (year == 0) {
                binding.statisticalDataNowadaysPauseHour.visibility = View.GONE
                binding.statisticalDataNowadaysPauseHourText.visibility = View.GONE
                binding.statisticalDataNowadaysPauseMin.text = min.toString()
            } else {
                binding.statisticalDataNowadaysPauseHour.visibility = View.VISIBLE
                binding.statisticalDataNowadaysPauseHourText.visibility = View.VISIBLE
                binding.statisticalDataNowadaysPauseMin.text = min.toString()
                binding.statisticalDataNowadaysPauseHour.text = year.toString()
            }
        }
    }

    /**
     * 累计专注处理
     */
    private fun initCumulativeFocus(mutableList: MutableList<LCObject>) {
        binding.statisticalDataCumulativeFocusNum1.text = mutableList.size.toString()
        var timeDate = 0
        mutableList.forEach {
            timeDate += it.getInt("FocusModeDuration")
        }
        durationToYearAndMin(timeDate) { year, min ->
            if (year == 0) {
                binding.statisticalDataCumulativeFocusHour.visibility = View.GONE
                binding.statisticalDataCumulativeFocusHourText.visibility = View.GONE
                binding.statisticalDataCumulativeFocusMinute.text = min.toString()
            } else {
                binding.statisticalDataCumulativeFocusHour.visibility = View.VISIBLE
                binding.statisticalDataCumulativeFocusHourText.visibility = View.VISIBLE
                binding.statisticalDataCumulativeFocusMinute.text = min.toString()
                binding.statisticalDataCumulativeFocusHour.text = year.toString()
            }
        }

        var timeDatePause = 0
        mutableList.forEach {
            timeDatePause += it.getInt("FocusModePauseDuration")
        }
        durationToYearAndMin(timeDatePause) { year, min ->
            if (year == 0) {
                binding.statisticalDataPauseHour.visibility = View.GONE
                binding.statisticalDataPauseHourText.visibility = View.GONE
                binding.statisticalDataPauseMin.text = min.toString()
            } else {
                binding.statisticalDataPauseHour.visibility = View.VISIBLE
                binding.statisticalDataPauseHourText.visibility = View.VISIBLE
                binding.statisticalDataPauseMin.text = min.toString()
                binding.statisticalDataPauseHour.text = year.toString()
            }
        }
    }

}