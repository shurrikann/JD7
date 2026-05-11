package com.shurrikann.jd7.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.shurrikann.jd7.R

class FullImageDialog(context: Context, private val imageUrl: String) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_full_image)
        val imageView = findViewById<ScaleView>(R.id.dialog_full_image_view)
//        imageView.loadImage(imageUrl)
        Log.d("FullImageDialog", imageUrl)
        // 使用 Glide 加载图片
        Glide.with(context).load(imageUrl).into(imageView)
    }
}