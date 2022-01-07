package com.viatom.bloodoxygendemo.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.adapter.HistoryAdapter
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.data.entity.DataEntity
import com.viatom.bloodoxygendemo.databinding.FragmentHistoryBinding
import com.viatom.bloodoxygendemo.recyclerview.BaseQuickAdapter
import com.viatom.bloodoxygendemo.recyclerview.VH
import com.viatom.bloodoxygendemo.viewmodel.MainViewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 10:46
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 历史页面
 */
class HistoryFragment : Fragment() {

    lateinit var binding: FragmentHistoryBinding
//    lateinit var adapter : HistoryAdapter
    lateinit var adapter: BaseQuickAdapter<DataEntity,View>
    private var dataList: ArrayList<DataEntity> = ArrayList()
    lateinit var collectUtil: CollectUtil
    private val mainViewModel : MainViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        collectUtil = CollectUtil.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"onCreateView")
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_history,container,false)
        binding.lifecycleOwner = this
        initView()
        initLiveEvent()
        return binding.root
    }

    private fun initView() {
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
//            //倒序
//            this.stackFromEnd = true
//            this.reverseLayout = true
            binding.rcv.layoutManager = this

        }
//        adapter = HistoryAdapter(dataList)

    }

    private fun initLiveEvent() {
        LiveEventBus.get(Constant.Event.getAIResult).observe(viewLifecycleOwner){ list ->
//            Log.d(TAG, "添加所有数据：$list")
            dataList.clear()
            dataList.addAll(list as Collection<DataEntity>)
            refreshList(dataList)
        }

        LiveEventBus.get(Constant.Event.getAIResultOne).observe(viewLifecycleOwner){ item ->
//            Log.d(TAG, "添加一条数据：$item")
            (item as DataEntity)
            dataList.add(0,item)
//            for (data in dataList){
//                Log.d(TAG,data.toString())
//            }
            refreshList(dataList)
//            adapter.notifyItemChanged(0)
        }
    }

    private fun refreshList(list: ArrayList<DataEntity>) {
        adapter = object : BaseQuickAdapter<DataEntity, View>(list, R.layout.fragment_report_item) {
                override fun convert(
                    holder: VH<View>,
                    position: Int,
                    data: DataEntity,
                    status: Int
                ) {
                    val result = holder.getView(R.id.tv_aiResult) as TextView
                    val startTime = holder.getView(R.id.time) as TextView
                    val endTime = holder.getView(R.id.timeEnd) as TextView
                    result.text = data.message
                    startTime.text = data.time
                    endTime.text = data.timeEnd
                }

                override fun onItemClick(
                    holder: RecyclerView.ViewHolder,
                    position: Int,
                    t: DataEntity
                ) {
                }
            }
        binding.rcv.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    companion object {
        private const val TAG = "HistoryFragment"
    }
}