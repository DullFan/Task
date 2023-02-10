package com.dullfan.module_main.utils

import cn.leancloud.LCObject

/**
 * 当前选中的项目
 */
lateinit var projectDetailsData : LCObject

/**
 * 项目列表
 */
lateinit var projectDataList : List<LCObject>

/**
 * 当前选中的任务
 */
lateinit var TaskDetailsData : LCObject
lateinit var currentaskId : String
lateinit var taskStartTime : String
lateinit var taskEndTime : String

/**
 * 统计数据当前处理模式
 * 0: 所有项目合计
 * 1: 当前项目中的任务合计
 * 2: 当前任务合计
 */
var StatisticalDataMode = 0