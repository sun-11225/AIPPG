package com.viatom.bloodoxygendemo.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView

abstract class BaseQuickAdapter<T, V : View>(private val data: List<T>, @param:LayoutRes private val layoutId: Int, private var currentPosition: Int = 0) : RecyclerView.Adapter<VH<V>>(),
    OnItemClickListener<T> {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<V> {
        return VH.get(parent, layoutId)
    }

    override fun getItemCount(): Int = data.size

    @Nullable
    fun getItem(index: Int): T {
        require(index >= 0) { "Only indexes >= 0 are allowed. Input was: $index" }
        require(index < data.size) { "Only indexes < ${data.size} are allowed. Input was: $index" }
        // To avoid exception, return null if there are some extra positions that the
        // child adapter is adding in getItemCount (e.g: to display footer view in recycler view)
        return data[index]
    }

    override fun onBindViewHolder(holder: VH<V>, position: Int) {
        val item: T = getItem(position)
        if (currentPosition == position) {
            convert(holder, position, item, 0)
        } else {
            convert(holder, position, item, 1)
        }
        holder.getContentView().setOnClickListener{
            currentPosition = position
            onItemClick(holder, position, item)
        }
    }

    abstract fun convert(holder: VH<V>, position: Int, data: T, status: Int)

}