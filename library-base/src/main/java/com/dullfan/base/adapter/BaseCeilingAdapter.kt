package com.dullfan.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dullfan.base.bean.RecyclerViewItemBean
import com.dullfan.base.utils.showLog

/**
 * RecyclerView吸顶效果Adapter
 */
class BaseCeilingAdapter<T>(
    val layoutId: Int,
    _dataList: List<RecyclerViewItemBean<T>>,
    _index: Int = 0,
    val onBind: (view: View, data: RecyclerViewItemBean<T>, position: Int) -> Unit
) : RecyclerView.Adapter<BaseFunViewHolder>() {
    var dataList = _dataList
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var index = _index
        set(value) {
            notifyItemChanged(field)
            field = value
            notifyItemChanged(index)
        }


    /**
     * 是否是组的第一个item
     *
     * @param position
     * @return
     */
    fun isGroupHeader(position: Int): Boolean {
        //如果等于0直接返回true,因为他是第一个头部
        return if (position == 0 || dataList.size == 1) {
            true
        } else {
            //获取头部数据
            val currentGroupName = getGroupName(position)
            //获取上一个头部数据
            val preGroupName = getGroupName(position - 1)
            //拿上一个头部数据和下一个头部数据进行比较,相等返回false,不相等返回true
            preGroupName != currentGroupName
        }
    }

    //获取头部数据
    fun getGroupName(position: Int): String {
        return dataList[position].name
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BaseFunViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BaseFunViewHolder(view)
    }



    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(
        holder: BaseFunViewHolder,
        position: Int
    ) {
        onBind.invoke(holder.itemView,dataList[position],position)

    }
}