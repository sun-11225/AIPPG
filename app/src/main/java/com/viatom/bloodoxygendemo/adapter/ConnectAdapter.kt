package com.viatom.bloodoxygendemo.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.blepro.objs.Bluetooth
import com.viatom.bloodoxygendemo.R

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 15:07
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
class ConnectAdapter(layoutResId: Int, data: MutableList<Bluetooth>?) : BaseQuickAdapter<Bluetooth, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bluetooth) {
        holder.setText(R.id.name, item.name)
    }
}