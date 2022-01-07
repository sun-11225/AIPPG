package com.viatom.bloodoxygendemo.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

class VH<T : View> private constructor(private val contentView: View, private val mViews: SparseArray<T> = SparseArray()) : RecyclerView.ViewHolder(contentView) {

    fun getContentView() = contentView

    fun getView(@IdRes id: Int): T {
        var view = mViews[id]
        if (view == null) {
            view = contentView.findViewById(id)
            mViews.put(id, view)
        }
        return view
    }

    companion object {
        fun <T : View> get(parent: ViewGroup, layoutId: Int): VH<T> {
            val contentView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return VH(contentView)
        }
    }

}