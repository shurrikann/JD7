package com.shurrikann.jd7.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shurrikann.jd7.R
import com.shurrikann.jd7.activity.HistoryDataLanWayActivity
import com.shurrikann.jd7.adapter.SelectAdapter
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.LWData
import com.shurrikann.jd7.databinding.SelectLanwayFragmentBinding

class SelectLanwayFragment : Fragment(R.layout.select_lanway_fragment) {
    private var _binding: SelectLanwayFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectAdapter: SelectAdapter
    private var lanwaylist = mutableListOf<LWData>()
    private var list = mutableListOf<LWData>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SelectLanwayFragmentBinding.bind(view)
        binding.searchBtn.setOnClickListener {
            val intent = Intent(context, HistoryDataLanWayActivity::class.java)
            intent.putExtra("key_name", "102回风瓦斯巡检")  // "key_name" 是键，"Hello, World!" 是值
            startActivity(intent)
        }
        initAdapter()
        initData()
        initListener()
    }

    private fun initListener() {
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString()
                list.clear()
                if (!str.isNullOrEmpty()) {
                    for (item in lanwaylist) {
                        if (item.laneView.contains(str)) {
                            list.add(item)
                        }
                    }
                    selectAdapter.submitList(list)
                } else {
                    list.addAll(lanwaylist)
                    selectAdapter.submitList(list)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun initData() {
        val str = DataManager.getDataString("lanway")
        // 定义要转换成的 List<Fruit> 类型
        val listType = object : TypeToken<List<LWData>>() {}.type
        Log.d("str", str)
        lanwaylist.addAll(Gson().fromJson(str, listType))
        if (lanwaylist.size > 0)
            binding.noDataLayout.visibility = View.GONE
        else
            binding.noDataLayout.visibility = View.VISIBLE
        list.addAll(lanwaylist)
        selectAdapter.notifyDataSetChanged()
    }


    private fun initAdapter() {
        selectAdapter = SelectAdapter()
        binding.historyRecy.layoutManager = LinearLayoutManager(context)
        binding.historyRecy.adapter = selectAdapter
        selectAdapter.submitList(list)
        selectAdapter.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val intent = Intent()
            intent.setClass(requireContext(), HistoryDataLanWayActivity::class.java)
            intent.putExtra("id", list.get(position).id)
            intent.putExtra("name", list.get(position).laneName)
            startActivity(intent)
        })
    }
}