package com.viatom.bloodoxygendemo.recyclerview

import androidx.recyclerview.widget.RecyclerView

interface OnItemClickListener<T> {
    fun onItemClick(holder: RecyclerView.ViewHolder, position: Int, t: T)
}