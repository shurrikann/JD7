package com.shurrikann.jd7.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.shurrikann.jd7.R
import com.shurrikann.jd7.bean.LWData

class SelectAdapter : BaseQuickAdapter<LWData, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: LWData?) {
        holder.getView<TextView>(R.id.lanway_text).text = item?.laneName
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.history_item, parent)
    }

}