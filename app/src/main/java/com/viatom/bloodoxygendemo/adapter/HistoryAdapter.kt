package com.viatom.bloodoxygendemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.data.entity.DataEntity
import com.viatom.bloodoxygendemo.databinding.FragmentReportItemBinding


/**
 * @author：created by SunHao
 * 创建时间：2021/11/22 15:35
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
class HistoryAdapter(private val item : List<DataEntity>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    lateinit var binding : FragmentReportItemBinding

    /**
     * ViewHolder，因为实现数据绑定，所以实际操作由[binding]实现。
     */
     inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.fragment_report_item,parent,false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        Log.d("demo", item[position].toString())
        val dataEntity = item[position]
        binding.tvAiResult.text =  dataEntity.message
        binding.time.text =  dataEntity.time
        binding.timeEnd.text =  dataEntity.timeEnd
    }

    override fun getItemCount(): Int {
      return item.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}