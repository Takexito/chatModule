package com.dev.podo.core.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BasePagingAdapter<B : ViewBinding, D : Any>(
    val onItemClick: OnRecyclerItemClick,
    private val getViewBinding: ViewBindingGetter<B>,
    diffUtil: DiffUtil.ItemCallback<D>
) : PagingDataAdapter<D, BasePagingAdapter.BaseViewHolder<B>>(diffUtil) {

    var viewBinding: B? = null

    val data: List<D>
        get() = snapshot().items

    class BaseViewHolder<B : ViewBinding>(val viewBinding: B) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<B> {
        viewBinding = getViewBinding.invoke(parent)
        return BaseViewHolder(viewBinding!!)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<B>, position: Int) {
        bind(holder.viewBinding, position)
    }

    abstract fun bind(viewBinding: B, position: Int)
}
