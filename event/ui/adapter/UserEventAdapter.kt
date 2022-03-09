package com.dev.podo.event.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.adapters.selectable.ItemClickListener
import com.dev.podo.databinding.EventChatItemBinding
import com.dev.podo.databinding.ItemEventViewPagerBinding
import com.dev.podo.databinding.ItemEventViewPagerNavigationBinding
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.home.model.entities.HomeEventModel
import com.eudycontreras.boneslibrary.bindings.addSkeletonLoader
import com.eudycontreras.boneslibrary.extensions.disableSkeletonLoading
import com.eudycontreras.boneslibrary.extensions.dp
import com.eudycontreras.boneslibrary.framework.skeletons.SkeletonDrawable
import com.eudycontreras.boneslibrary.properties.CornerRadii
import com.eudycontreras.boneslibrary.properties.MutableColor

enum class EventViewPagerTypes {
    NAVIGATION,
    EVENT
}

class UserEventAdapter(
    private val onItemClick: ItemClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoading = false

    fun changeState(isLoading: Boolean){
        this.isLoading = isLoading
        if(isLoading){
            data = mutableListOf(HomeEventModel.empty())
            notifyDataSetChanged()
        } else {
            notifyDataSetChanged()
        }
    }

    fun enableSkeleton(viewBinding: ItemEventViewPagerBinding) {
        viewBinding.root.let {
            val view = it
            val skelet = SkeletonDrawable.create(view)
                .builder()
                .setEnabled(true)
                .setColor(MutableColor.Companion.fromColor(Color.WHITE))
                .withBoneBuilder {
                    setColor(MutableColor.Companion.fromColor(Color.LTGRAY))
                        .setCornerRadii(CornerRadii.Companion.create(16.dp))
                        .withShimmerBuilder {
                            setColor(MutableColor.fromColor(Color.WHITE))
                            setThickness(20.dp)
                            setTilt(0.3f)
                            this.setAnimationDuration(1200)
                            this.setCount(2)
                        }
                }
                .setAllowSavedState(true)

            view.addSkeletonLoader(true, skelet.get())
        }
    }

    fun disableSkeleton(viewBinding: ItemEventViewPagerBinding) {
        viewBinding.root.disableSkeletonLoading()
    }

    var data: MutableList<HomeEventModel?> = arrayListOf()
        set(value) {
            field = value
            data.add(null)
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        if (data[position] == null) {
            return EventViewPagerTypes.NAVIGATION.ordinal
        }
        return EventViewPagerTypes.EVENT.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        val viewHolder: RecyclerView.ViewHolder?
        when (EventViewPagerTypes.values()[viewType]) {
            EventViewPagerTypes.NAVIGATION -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_event_view_pager_navigation, parent, false)
                viewHolder = NavigationViewHolder(view, onItemClick)
            }
            EventViewPagerTypes.EVENT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_event_view_pager, parent, false)
                viewHolder = EventViewHolder(view, onItemClick)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NavigationViewHolder -> holder.bind()
            is EventViewHolder -> data[position]?.let { holder.bind(it) }
        }
    }

    override fun getItemCount(): Int = data.size

    inner class NavigationViewHolder(
        itemView: View,
        private val clickListener: ItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        val viewBinding: ItemEventViewPagerNavigationBinding by viewBinding()
        fun bind() {
            itemView.setOnClickListener {
                clickListener.onItemClicked(bindingAdapterPosition)
            }
//            viewBinding.image.setColorFilter(
//                itemView.context.resources.getColor(R.color.dark_green),
//                PorterDuff.Mode.SRC_IN
//            )
        }
    }

    inner class EventViewHolder(
        itemView: View,
        private val clickListener: ItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        val viewBinding: ItemEventViewPagerBinding by viewBinding()
        fun bind(event: HomeEventModel) {
            if (isLoading){
                enableSkeleton(viewBinding)
                return
            } else {
                disableSkeleton(viewBinding)
            }
            itemView.setOnClickListener {
                clickListener.onItemClicked(bindingAdapterPosition)
            }
            viewBinding.city.text = event.city
            viewBinding.title.text = event.title
            viewBinding.type.text = itemView.resources.getString(event.type.titleRes)
            event.activePromptsCount.let {
                viewBinding.responseTitle.text = itemView.resources.getQuantityString(R.plurals.prompts, it, it)
            }
        }
    }
}
