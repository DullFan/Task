package com.dullfan.module_main.ui.project.details

import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.dullfan.base.adapter.BaseRvAdapter
import com.dullfan.base.base.BaseFragment
import com.dullfan.base.lc.getLCFileImageUrl
import com.dullfan.base.lc.myObserver
import com.dullfan.base.utils.getNowDate
import com.dullfan.base.utils.myClickListener
import com.dullfan.base.utils.timeDifference
import com.dullfan.module_main.R
import com.dullfan.module_main.bean.FocusedExperienceBean
import com.dullfan.module_main.databinding.*
import com.dullfan.module_main.ui.MainActivity
import com.dullfan.module_main.utils.currentaskId
import com.dullfan.module_main.utils.projectDetailsData
import com.dullfan.module_main.utils.taskEndTime
import com.dullfan.module_main.utils.taskStartTime
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class FocusedFragment() : BaseFragment() {
    val binding by lazy {
        FragmentFocusedBinding.inflate(layoutInflater)
    }

    lateinit var fragmentViewDataBinding: FragmentProjectDetailsBinding

    constructor(_fragmentViewDataBinding: FragmentProjectDetailsBinding) : this() {
        fragmentViewDataBinding = _fragmentViewDataBinding
    }

    companion object {
        var focusedFragmentFlag: Boolean = false
        var inTime: Long = 0;
        var pauseDuration: Long = 0;
        var experienceContentList: ArrayList<FocusedExperienceBean> = ArrayList();
    }

    var min = 0
    var second = 0

    var pauseMin = 0
    var pauseSecond = 0

    //是否暂停
    var pauseFlag = false;

    val handler = object : Handler(Looper.myLooper()!!) {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun handleMessage(msg: Message) {
            if (pauseFlag) {
                pauseDuration++
                pauseSecond++
                if (pauseSecond >= 60) {
                    pauseMin++
                    pauseSecond = 0
                }
                binding.circularProgressIndicator.setProgress(pauseSecond, true)
                if (pauseSecond < 10) "0${pauseSecond}" else "$pauseSecond"
                if (pauseMin < 10) "0${pauseMin}" else "$pauseMin"
                binding.date.text =
                    "${if (pauseMin < 10) "0${pauseMin}" else "$pauseMin"}:${if (pauseSecond < 10) "0${pauseSecond}" else "$pauseSecond"}"
            } else {
                second++
                if (second >= 60) {
                    min++
                    second = 0
                }
                binding.circularProgressIndicator.setProgress(second, true)
                if (second < 10) "0${second}" else "$second"
                if (min < 10) "0${min}" else "$min"
                binding.date.text =
                    "${if (min < 10) "0${min}" else "$min"}:${if (second < 10) "0${second}" else "$second"}"
            }
            sendEmptyMessageDelayed(1, 1000)
        }
    }

    var alwaysOnFlag = false
    val sharedPreferences by lazy {
        requireActivity().getSharedPreferences("task", 0)
    }
    val edit by lazy {
        sharedPreferences.edit()
    }
    var mp3List = ArrayList<Int>()
    var player = MediaPlayer()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        initSetting()
        initData()
        handler.sendEmptyMessage(1)
        initTermination()
        initAlwaysOn()
        initPause()
        initAdd()
        initMusic()
        return binding.root
    }

    /**
     * 设置白噪音
     */
    private fun initMusic() {
        val stringList = ArrayList<String>()
        stringList.add("无声")
        stringList.add("春日鸟鸣")
        stringList.add("海边篝火")
        stringList.add("静谧钢琴")
        stringList.add("休闲咖啡")
        stringList.add("热带雨林")
        stringList.add("时钟滴答")
        stringList.add("雨声")
        stringList.add("夏日蝉鸣")
        stringList.add("小河流水")
        val imageList = ArrayList<Int>()
        imageList.add(com.dullfan.base.R.color.purple_700)
        imageList.add(R.drawable.music_image01)
        imageList.add(R.drawable.music_image02)
        imageList.add(R.drawable.music_image03)
        imageList.add(R.drawable.music_image04)
        imageList.add(R.drawable.music_image05)
        imageList.add(R.drawable.music_image06)
        imageList.add(R.drawable.music_image07)
        imageList.add(R.drawable.music_image08)
        imageList.add(R.drawable.music_image09)
        binding.music.setOnClickListener(myClickListener {
            val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            val dialogMusicLayoutBinding = DialogMusicLayoutBinding.inflate(layoutInflater)
            dialogMusicLayoutBinding.rv.adapter =
                object : BaseRvAdapter<String>(R.layout.item_dialog_music_layout, stringList) {
                    var selectedPosition = sharedPreferences.getInt("musicSelectedPosition", 0);
                    override fun onBind(
                        rvDataBinding: ViewDataBinding,
                        data: String,
                        position: Int
                    ) {
                        rvDataBinding as ItemDialogMusicLayoutBinding
                        rvDataBinding.image.setImageResource(imageList[position])
                        if (position == selectedPosition) {
                            rvDataBinding.text.text = "${stringList[position]}(当前)"
                            rvDataBinding.text.textSize = 17f
                            rvDataBinding.text.alpha = 1f
                            rvDataBinding.image.alpha = 1f
                        } else {
                            rvDataBinding.text.text = stringList[position]
                            rvDataBinding.text.textSize = 15f
                            rvDataBinding.text.alpha = 0.8f
                            rvDataBinding.image.alpha = 0.5f
                        }

                        rvDataBinding.root.setOnClickListener(myClickListener {
                            notifyItemChanged(selectedPosition)
                            selectedPosition = position
                            notifyItemChanged(selectedPosition)

                            if (position == 0) {
                                if (player.isPlaying) {
                                    player.stop()
                                }
                            } else {
                                if(player.isPlaying){
                                    player.stop()
                                }
                                //播放音乐
                                player = MediaPlayer.create(requireContext(),mp3List[position])
                                player.isLooping = true
                                player.start()
                            }
                            edit.putInt("musicSelectedPosition", selectedPosition)
                            edit.commit()
                        })
                    }
                }
            dialog.setView(dialogMusicLayoutBinding.root)
            dialog.show()
        })
    }

    /**
     * 灵感事件
     */
    private fun initAdd() {
        binding.add.setOnClickListener(myClickListener {
            if (System.currentTimeMillis() - inTime > 1000) {
                val dialogAddExperienceLayoutBinding =
                    DialogAddExperienceLayoutBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext()).create()
                dialog.setView(dialogAddExperienceLayoutBinding.root)
                dialog.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialogAddExperienceLayoutBinding.dialogFocusedClone.setOnClickListener(
                    myClickListener {
                        dialog.dismiss()
                    })

                dialogAddExperienceLayoutBinding.button.setOnClickListener(myClickListener {
                    taskEndTime = getNowDate()
                    val etString = dialogAddExperienceLayoutBinding.dialogFocusedEt.text.toString()
                    if (etString.isBlank()) {
                        showToast("请输入内容")
                        return@myClickListener
                    }
                    showToast("添加成功")
                    experienceContentList.add(FocusedExperienceBean(etString, getNowDate()))
                    dialog.dismiss()
                })
            } else {
                showToast("1分钟以下不可添加灵感/步骤")
            }
        })
    }

    /**
     * 暂停事件
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun initPause() {
        binding.pause.setOnClickListener(myClickListener {
            if (pauseFlag) {
                showToast("开启计时")
                binding.pauseText.visibility = View.GONE
                binding.pause.setImageResource(R.drawable.pause)
            } else {
                binding.circularProgressIndicator.setProgress(0, true)
                binding.pauseText.visibility = View.VISIBLE
                showToast("暂停成功")
                binding.pause.setImageResource(R.drawable.play)
            }
            pauseFlag = !pauseFlag
        })
    }

    /**
     * 常亮事件
     */
    private fun initAlwaysOn() {
        binding.alwaysBright.setOnClickListener(myClickListener {
            if (alwaysOnFlag) {
                showToast("开启屏幕常亮")
                binding.alwaysBright.alpha = 1f
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                showToast("关闭屏幕常亮")
                binding.alwaysBright.alpha = 0.5f
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            alwaysOnFlag = !alwaysOnFlag
        })
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).immersiveSetting()
    }

    /**
     * 终止事件
     */
    private fun initTermination() {
        binding.finish.setOnClickListener(myClickListener {
            if (System.currentTimeMillis() - inTime > 1000) {
                val dialogFinishLayoutBinding = DialogFinishLayoutBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext()).create()
                dialog.setView(dialogFinishLayoutBinding.root)
                dialog.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialogFinishLayoutBinding.dialogFocusedClone.setOnClickListener(myClickListener {
                    dialog.dismiss()
                })

                dialogFinishLayoutBinding.button.setOnClickListener(myClickListener {
                    taskEndTime = getNowDate()
                    val etString = dialogFinishLayoutBinding.dialogFocusedEt.text.toString()
                    val timeDifference = timeDifference(taskStartTime, taskEndTime)
                    val lcObject = LCObject("FocusMode")
                    lcObject.put("FocusModeExperienceContent", etString)
                    lcObject.put("FocusModeOffTime", taskEndTime)
                    lcObject.put("FocusModeOnTime", taskStartTime)
                    lcObject.put("FocusModeDuration", timeDifference)
                    lcObject.put("TaskId", currentaskId)
                    lcObject.put("ProjectId", projectDetailsData.objectId)
                    if (experienceContentList.size != 0) {
                        lcObject.put("ExperienceContentList", Gson().toJson(experienceContentList))
                    }
                    lcObject.saveInBackground().subscribe(myObserver(onError = {
                        showToast(it.message)
                    }, onNext = {
                        showToast("保存成功")
                        destroyFinish()
                        requireActivity().supportFragmentManager.popBackStack()
                        dialog.dismiss()
                    }))
                })
            } else {
                showToast("不记录1分钟以下的专注记录")
                destroyFinish()
                //退出专注模式
                requireActivity().supportFragmentManager.popBackStack()
            }
        })
    }


    /**
     * 初始化数据
     */
    private fun initData() {
        val queryImageCount = LCQuery<LCObject>("FocusedModeImage")
        queryImageCount.countInBackground().subscribe(myObserver {
            val queryImage = LCQuery<LCObject>("FocusedModeImage")
            queryImage.skip = Random().nextInt(it)
            queryImage.limit = 1
            queryImage.firstInBackground.subscribe(myObserver {
                getLCFileImageUrl(it["url"].toString(), binding.image, requireContext())
                binding.image.visibility = View.VISIBLE
            })
        })

        val queryPhraseCount = LCQuery<LCObject>("FocusedModePhrase")
        queryPhraseCount.countInBackground().subscribe(myObserver {
            val queryPhrase = LCQuery<LCObject>("FocusedModePhrase")
            val nextInt = Random().nextInt(it)
            queryPhrase.skip = nextInt
            queryPhrase.limit = 1
            queryPhrase.firstInBackground.subscribe(myObserver {
                binding.phrase.text = it["Phrase"].toString()
            })
        })

        /**
         * 音频文件
         */
        mp3List = ArrayList()
        mp3List.add(0)
        mp3List.add(R.raw.music_raw01)
        mp3List.add(R.raw.music_raw02)
        mp3List.add(R.raw.music_raw03)
        mp3List.add(R.raw.music_raw04)
        mp3List.add(R.raw.music_raw05)
        mp3List.add(R.raw.music_raw06)
        mp3List.add(R.raw.music_raw07)
        mp3List.add(R.raw.music_raw08)
        mp3List.add(R.raw.music_raw09)

        /**
         * 音乐循环播放
         */
        player.isLooping = true

        if (sharedPreferences.getInt("musicSelectedPosition", 0) != 0) {
            player = MediaPlayer.create(requireContext(),mp3List[sharedPreferences.getInt("musicSelectedPosition", 0)])
            player.isLooping = true
            player.start()
        }

    }

    private fun initSetting() {
        experienceContentList.clear()
        pauseDuration = 0
        //进入专注页面开始计时，并且设置标记
        focusedFragmentFlag = true
        //开启常亮
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        inTime = System.currentTimeMillis();
        fragmentViewDataBinding.projectDetailsBack.visibility = View.GONE
        fragmentViewDataBinding.projectDetailsView01.visibility = View.GONE
        (activity as MainActivity).titleLayout(false)
        taskStartTime = getNowDate()
        //设置侧滑禁止手势滑动
        fragmentViewDataBinding.projectDetailsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }


    /**
     * 关闭页面时进行重置数据
     */
    fun destroyFinish() {
        focusedFragmentFlag = false
        //关闭常亮
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (activity as MainActivity).titleLayout(true)
        //设置侧滑开启手势滑动
        fragmentViewDataBinding.projectDetailsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        fragmentViewDataBinding.projectDetailsBack.visibility = View.VISIBLE
        fragmentViewDataBinding.projectDetailsView01.visibility = View.VISIBLE
        player.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyFinish()
    }
}