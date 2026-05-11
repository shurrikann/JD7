package com.shurrikann.jd7.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.shurrikann.jd7.R
import com.shurrikann.jd7.bean.HData

class HistoryDataTimeAdapter :
    BaseQuickAdapter<HData, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: HData?) {
        holder.getView<TextView>(R.id.name_text).text = item?.laneName
        holder.getView<TextView>(R.id.date_text).text =
            item?.curDateStr + " " + item?.cjTimeStr
        holder.getView<TextView>(R.id.fs_text).text = item?.speedValue.toString()
        holder.getView<TextView>(R.id.wd_text).text = item?.heatValue.toString()
        holder.getView<TextView>(R.id.qy_text).text = item?.presValue.toString()
        holder.getView<TextView>(R.id.sd_text).text = item?.humValue.toString()
        holder.getView<TextView>(R.id.o2_text).text = item?.o2Value.toString()
        holder.getView<TextView>(R.id.co_text).text = item?.coValue.toString()
        holder.getView<TextView>(R.id.ch4_text).text = item?.ch4Value.toString()
        holder.getView<TextView>(R.id.fl_text).text = item?.quantityValue.toString()
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.history_data_time_item, parent)
    }
}