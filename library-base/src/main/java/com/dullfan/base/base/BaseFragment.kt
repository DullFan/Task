package com.dullfan.base.base

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dullfan.base.R
import com.tapadoo.alerter.Alerter

open class BaseFragment : Fragment() {

    fun <T> startA(clazz: Class<T>, title: String) {
        val intent = Intent(requireContext(), clazz)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    var toast: Toast? = null
    fun <T> showToast(value: T) {
        if(toast == null){
            toast = Toast.makeText(requireContext(),"${value}",Toast.LENGTH_LONG)
        }else{
            toast?.cancel()
            toast = Toast.makeText(requireContext(),"${value}",Toast.LENGTH_LONG)
        }
        toast?.show()
    }

    fun showAlerter(
        title: String = "提示",
        text: String = "修改成功",
        color: Int = com.dullfan.base.R.color.purple_500
    ) {
        Alerter.create(requireActivity())
            .setTitle(title)
            .setText(text)
            .setBackgroundColorInt(resources.getColor(color))
            .show()
    }

    fun showAlerterError(
        title: String = "提示",
        text: String = "修改失败",
        color: Int = R.color.red
    ) {
        Alerter.create(requireActivity())
            .setTitle(title)
            .setText(text)
            .setBackgroundColorInt(resources.getColor(color))
            .show()
    }

    fun glide(url: String, view: ImageView) {
        Glide.with(this).load(url).into(view)
    }

    /**
     * 收起软键盘
     */
    fun collapseTheSoftKeyboard() {
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }

    fun startFragment(layoutId: Int, clazz: Fragment) =
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_top,
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom
            )
            .replace(layoutId, clazz)
            .addToBackStack("")
            .commit()


    fun outFragment(layoutId: Int, clazz: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom,
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom
            )
            .replace(layoutId, clazz)
            .addToBackStack("")
            .commit()
//        requireActivity().supportFragmentManager.popBackStack()
    }
}