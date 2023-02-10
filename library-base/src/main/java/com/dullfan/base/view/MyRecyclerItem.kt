package com.dullfan.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.dullfan.base.adapter.BaseCeilingAdapter


// RecyclerView.ItemDecoration作用是:向 RecyclerView中的 ItemView 添加分割线
class MyRecyclerItem(context: Context) : ItemDecoration() {
    //头部高度
    private val groupHeaderHeight: Int

    //头部矩形画笔
    private val headPaint: Paint by lazy {
        Paint().apply {
            setColor(Color.parseColor("#f4f9fd"))
        }
    }

    // 头部文字画笔
    private val textPaint: Paint

    //矩形
    private val textRect: Rect

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.adapter is BaseCeilingAdapter<*>) {
            val adapter = parent.adapter as BaseCeilingAdapter<*>
            //获取view的位置
            val position = parent.getChildAdapterPosition(view)
            //判断是否属于头部item
            val isGroupName: Boolean = adapter.isGroupHeader(position)
            if (isGroupName) {
                //如果是头部预    留更大的地方
                outRect.set(0, groupHeaderHeight, 0, 0)
            } else {
                outRect.set(
                    0, 0, 0,
                    0
                )
            }
        }
    }

    /*
     *
     * recyclerView绘制流程  onDraw-->绘制itemview-->onDrawOver
     * 所以在绘制之后，itemview会覆盖onDraw，onDrawOver会覆盖itemview
     * 因此我们在onDraw方法内绘制移动的header，在onDrawOver方法内绘制吸顶的header
     * 在子视图上设置绘制范围，并绘制内容
     * 类似平时自定义View时写onDraw()一样
     * 绘制图层在ItemView以下，所以如果绘制区域与ItemView区域相重叠，会被遮挡该方法实现了显示头部
  */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (parent.adapter is BaseCeilingAdapter<*>) {
            val adapter = parent.adapter as BaseCeilingAdapter<*>
            //当前屏幕的item个数
            val childCount = parent.childCount
            //绘制做有变的位置
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            for (i in 0 until childCount) {
                //获取对应i的View
                val view: View = parent.getChildAt(i)
                //获取View的布局位置
                val position = parent.getChildLayoutPosition(view)
                //是否是头部
                val isGroupHeader: Boolean = adapter.isGroupHeader(position)
                //需要加上padding的判断要不然添加paddingTop的时候会在padding中绘制
                if (isGroupHeader && view.getTop() - groupHeaderHeight - parent.paddingTop >= 0) {
                    c.drawRect(
                        left.toFloat(), (view.getTop() - groupHeaderHeight).toFloat(),
                        right.toFloat(), view.getTop().toFloat(), headPaint
                    )
                    //获取组名
                    val groupname: String = adapter.getGroupName(position)
                    //getTextBounds是将文本放入一个矩形中,测量TextView的高度和宽度
                    textPaint.getTextBounds(groupname, 0, groupname.length, textRect)
                    //绘制文字
                    c.drawText(
                        groupname,
                        (left + 40).toFloat(),
                        (view.getTop() - groupHeaderHeight / 2 + textRect.height() / 2).toFloat(),
                        textPaint
                    )
                } else if (view.getTop() - groupHeaderHeight - parent.paddingTop >= 0) {
                    //分割线
                    c.drawRect(left.toFloat(),
                        (view.getTop()).toFloat(), right.toFloat(),
                        view.getTop().toFloat(), headPaint)
                }
            }
        }
    }

    //与onDraw（）类似，都是绘制内容,但与onDraw（）的区别是：
    //作用：同样是绘制内容，但与onDraw（）的区别是：绘制在图层的最上层
    //onDrawOver()是在onDraw()绘制完之后绘制的
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (parent.adapter is BaseCeilingAdapter<*>) {
            val adapter = parent.adapter as BaseCeilingAdapter<*>
            //返回可见区域内的第一个item的position
            val position =
                (parent.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
            //获取对应position的View ,这里的item是RecyclerView中的ViewHolder
            val view: View = parent.findViewHolderForAdapterPosition(position)!!.itemView
            //绘制左右位置
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            //由于是吸顶效果,需要设置top的位置
            val top = parent.paddingTop
            //判断第二个item是否为组的头部
            //如果是,表示下一组的头部将要到来,要准备绘制下一组的头部,且当前头部准备移除
            val isGroupName: Boolean = adapter.isGroupHeader(position + 1)
            //无论什么情况都需要绘制头部
            if (isGroupName) {
                // - top表示当前view在屏幕内显示的高度，与headerHeight对比 取最小值，谁的值小就以谁为基准开始移除
                val bottom = Math.min(groupHeaderHeight, view.getBottom() - parent.paddingTop)
                // 头部根据bottom的值逐渐变小而逐渐往上移除
                c.drawRect(
                    left.toFloat(), top.toFloat(), right.toFloat(),
                    (top + bottom).toFloat(), headPaint
                )
                // 绘制文字
                val groupName: String = adapter.getGroupName(position)
                textPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                // 绘制的文字的高度不能超出的区域,不加此行代码，当设置了paddingtop值的时候，文字会在padding部分绘制
                c.drawText(
                    groupName,
                    left + 40f,
                    (top + bottom - groupHeaderHeight / 2 + textRect.height() / 2).toFloat(),
                    textPaint
                )
            } else {
                //属于同一组的不变
                c.drawRect(
                    left.toFloat(), top.toFloat(), right.toFloat(),
                    (top + groupHeaderHeight).toFloat(), headPaint
                )
                val groupName: String = adapter.getGroupName(position)
                textPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                c.drawText(
                    groupName,
                    left + 40f,
                    (top + groupHeaderHeight / 2 + textRect.height() / 2).toFloat(),
                    textPaint
                )
            }
        }
    }

    //dp转px
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (dpValue * scale * 0.5f).toInt()
    }

    //初始化数据
    init {
        groupHeaderHeight = dp2px(context, 80f)
        textPaint = Paint()
        textPaint.setTextSize(50f)
        textPaint.setColor(Color.BLACK)
        textRect = Rect()
    }
}