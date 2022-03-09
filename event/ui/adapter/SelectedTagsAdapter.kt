package com.dev.podo.event.ui.adapter

import com.dev.podo.common.model.entities.Choice
import com.dev.podo.core.ui.adapter.BaseAdapter
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.RecyclerViewChipItemSelectedBinding

class SelectedTagsAdapter(
    onItemClick: OnRecyclerItemClick,
    viewBinding: ViewBindingGetter<RecyclerViewChipItemSelectedBinding>
) : BaseAdapter<RecyclerViewChipItemSelectedBinding, Choice>(onItemClick, viewBinding) {

    override fun bind(viewBinding: RecyclerViewChipItemSelectedBinding, position: Int) {
        viewBinding.chipItem.setOnClickListener {
            onItemClick?.invoke(data[position].id)
        }
        viewBinding.apply {
            bindView(data[position])
        }
    }

    private fun RecyclerViewChipItemSelectedBinding.bindView(
        model: Choice,
    ) {
        chipTitle.text = model.title
    }
}
