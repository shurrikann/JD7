package com.shurrikann.jd7.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.shurrikann.jd7.R
import com.shurrikann.jd7.bean.LocationData
import com.shurrikann.jd7.myinterface.ListInterface
import com.shurrikann.jd7.utils.DateUtils

class TransmitListAdapter(private val listInterface: ListInterface) :
    BaseQuickAdapter<LocationData, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: LocationData?) {
        holder.getView<TextView>(R.id.lanway_name_text).text = item?.name
        holder.getView<TextView>(R.id.jc_time).text =
            DateUtils.convertMillisToDate(item?.cjTime?.toLongOrNull()!! * 1000)
        if (!item?.upTime.isNullOrEmpty()) {
            holder.getView<TextView>(R.id.up_time).text =
                DateUtils.convertMillisToDate(item?.upTime?.toLongOrNull()!! * 1000)
        }
        val img = holder.getView<ImageView>(R.id.state_img)
        when (item?.state) {
            0 -> Glide.with(context).load(context.resources.getDrawable(R.drawable.awaitup_img))
                .into(img)

            1 -> Glide.with(context).load(context.resources.getDrawable(R.drawable.uping_img))
                .into(img)

            2 -> Glide.with(context).load(context.resources.getDrawable(R.drawable.upok_img))
                .into(img)
        }
        holder.getView<ImageView>(R.id.delete_img).setOnClickListener {
            listInterface.deleteItem(position)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.transmit_list_item, parent)
    }
}