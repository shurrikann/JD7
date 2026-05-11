package com.shurrikann.jd7.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.shurrikann.jd7.R
import com.shurrikann.jd7.bean.LWData
import com.shurrikann.jd7.view.FullImageDialog

class LanWayAdapter :
    BaseQuickAdapter<LWData, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: LWData?) {
        holder.getView<TextView>(R.id.lanway_name_text).text = item?.laneView
        val img = holder.getView<ImageView>(R.id.lanway_img)
        Glide.with(context).load(item?.addrImg).into(img)
        img.setOnClickListener {
            val dialog = FullImageDialog(context, item?.addrImg ?: "")
            dialog.show()
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.lanway_item, parent)
    }
}