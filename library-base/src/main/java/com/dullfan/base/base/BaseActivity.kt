package com.dullfan.base.base

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dullfan.base.R
import com.tapadoo.alerter.Alerter

open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        window.statusBarColor = resources.getColor(R.color.background_color)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    }

    fun <T> startA(clazz: Class<T>, title: String) {
        val intent = Intent(this, clazz)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    var toast: Toast? = null
    fun <T> showToast(value: T) {
        if(toast == null){
            toast = Toast.makeText(this,"${value}",Toast.LENGTH_LONG)
        }else{
            toast?.cancel()
            toast = Toast.makeText(this,"${value}",Toast.LENGTH_LONG)
        }
        toast?.show()
    }

    fun showAlerter(
        title: String = "提示",
        text: String,
        color: Int = resources.getColor(com.dullfan.base.R.color.purple_500)
    ) {
        Alerter.create(this)
            .setTitle(title)
            .setText(text)     
            .setBackgroundColorInt(color)
            .show()
    }

    fun glide(url: String, view: ImageView) {
        Glide.with(this).load(url).into(view)
    }

    fun startFragment(layoutId: Int, clazz: Fragment) =
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_top,
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom
            )
            .replace(layoutId, clazz)
            .addToBackStack("")
            .commit()

    /**
     * 收起软键盘
     */
    fun collapseTheSoftKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    fun outFragment(layoutId: Int, clazz: Fragment){
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom,
                R.anim.slide_pop_in_top,
                R.anim.slide_pop_out_bottom
            )
            .replace(layoutId, clazz)
            .addToBackStack("")
            .commit()
    }

}