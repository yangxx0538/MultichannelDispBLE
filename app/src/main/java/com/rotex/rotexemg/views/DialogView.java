package com.rotex.rotexemg.views;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rotex.rotexhand.R;
import com.victor.loading.rotate.RotateLoading;

/**
 * Created by Administrator on 2016/3/3.
 */
public class DialogView {
    /**
     * 自定义Dialog
     * @param context
     * @param resId
     * @return
     */
    public static Dialog loadDialog(Context context, int resId) {
        Dialog dialog = new Dialog(context, R.style.mydialog) ;
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog_layout, null) ;
        //	dialog.setContentView(R.layout.dialog) ;
        dialog.setContentView(view) ;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        //  ProgressBar mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar) ;
        TextView textView = (TextView) view.findViewById(R.id.dialog_msg) ;
        textView.setText(resId) ;
        RotateLoading bookLoading= (RotateLoading) view.findViewById(R.id.rotateLoading);
        bookLoading.start();
        // mProgressBar.set(R.drawable.loading);
//        imageView.setBackgroundResource(R.anim.loading);
//        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground() ;
//        drawable.start();
        return dialog ;


    }



}
