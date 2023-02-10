package com.dullfan.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseFunAdapter<T>(
    val layoutId: Int,
    _dataList: List<T>,
    _index: Int = 0,
    val onBind: (view:View,data:T,position:Int) -> Unit
) :
    RecyclerView.Adapter<BaseFunViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseFunViewHolder {
        return BaseFunViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseFunViewHolder, position: Int) {
        onBind.invoke(holder.itemView,dataList[position],position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}

class BaseFunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)