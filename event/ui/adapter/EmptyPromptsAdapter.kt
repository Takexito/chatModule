package com.dev.podo.event.ui.adapter

import com.dev.podo.core.ui.adapter.BaseAdapter
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.EmptyTitledViewBinding

class EmptyPromptsAdapter(
    onItemClick: OnRecyclerItemClick,
    viewBinding: ViewBindingGetter<EmptyTitledViewBinding>
) : BaseAdapter<EmptyTitledViewBinding, Any> (onItemClick, viewBinding) {
    override fun bind(viewBinding: EmptyTitledViewBinding, position: Int) {
        viewBinding.root.setOnClickListener {
            onItemClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }
}
