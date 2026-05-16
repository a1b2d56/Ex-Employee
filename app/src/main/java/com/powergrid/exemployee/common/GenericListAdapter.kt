package com.powergrid.exemployee.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Reusable generic ListAdapter — no more boilerplate per-screen adapters.
 *
 * Usage:
 *   val adapter = GenericListAdapter(
 *       inflate            = { inf, parent, attach -> ItemNoticeBinding.inflate(inf, parent, attach) },
 *       areItemsTheSame    = { old, new -> old.id == new.id },
 *       areContentsTheSame = { old, new -> old == new },
 *   ) { binding, item -> binding.tvTitle.text = item.title }
 */
class GenericListAdapter<T, VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    areItemsTheSame: (T, T) -> Boolean,
    areContentsTheSame: (T, T) -> Boolean = { a, b -> a == b },
    private val bind: (VB, T) -> Unit,
) : ListAdapter<T, GenericListAdapter<T, VB>.Holder>(
    object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(o: T & Any, n: T & Any)    = areItemsTheSame(o, n)
        override fun areContentsTheSame(o: T & Any, n: T & Any) = areContentsTheSame(o, n)
    }
) {
    inner class Holder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) =
        bind(holder.binding, getItem(position))
}
