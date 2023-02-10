package com.dullfan.base.loadsir;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.dullfan.base.R;
import com.kingja.loadsir.callback.Callback;


public class TimeoutCallback extends Callback {

    @Override
    protected int onCreateView() {
        return R.layout.layout_timeout;
    }

    @Override
    protected boolean onReloadEvent(Context context, View view) {
        Toast.makeText(context.getApplicationContext(), "再次连接网络", Toast.LENGTH_SHORT).show();
        return false;
    }

}
