package com.dev.podo.event.ui.adapter.prompt

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.core.model.entities.User
import com.dev.podo.core.ui.adapter.BasePagingAdapter
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.EventPromptItemBinding
import com.dev.podo.event.model.entities.prompt.Prompt
import com.eudycontreras.boneslibrary.bindings.addSkeletonLoader
import com.eudycontreras.boneslibrary.extensions.disableSkeletonLoading
import com.eudycontreras.boneslibrary.extensions.dp
import com.eudycontreras.boneslibrary.framework.skeletons.SkeletonDrawable
import com.eudycontreras.boneslibrary.properties.CornerRadii
import com.eudycontreras.boneslibrary.properties.MutableColor

typealias OnPromptButtonsClick = (prompt: Prompt, promptButton: Int, adapterPosition: Int) -> Unit
typealias OnUserClick = (prompt: Prompt) -> Unit

const val PROMPT_ACCEPT = 1
const val PROMPT_DECLINE = 2

class PromptAdapter(
    onItemClick: OnRecyclerItemClick,
    private val onPromptButtonClick: OnPromptButtonsClick,
    private val onUserClick: OnUserClick,
    diffUtil: DiffUtil.ItemCallback<Prompt>,
    viewBinging: ViewBindingGetter<EventPromptItemBinding>
) : BasePagingAdapter<EventPromptItemBinding, Prompt>(onItemClick, viewBinging, diffUtil) {

    fun removePrompt(position: Int) {
        notifyItemRemoved(position)
    }

    fun enableSkeleton() {
        viewBinding?.root?.let {
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

    fun disableSkeleton() {
        viewBinding?.root?.disableSkeletonLoading()
    }

    override fun bind(viewBinding: EventPromptItemBinding, position: Int) {
        if (data.size <= position) return
        val model = getItem(position)
        val context = viewBinding.root.context
        viewBinding.apply { bindViews(model, context, position) }
    }

    private fun EventPromptItemBinding.bindViews(
        model: Prompt?,
        context: Context,
        position: Int
    ) {
        model?.let {
            promptMessage.text = model.message
            bindUserInfoView(it.user, context)
            bindClicks(it, position)
        }
    }

    private fun EventPromptItemBinding.bindUserInfoView(user: User?, context: Context) {
        user?.let {
            user.age?.let {
                promptUserAge.text = user.age.toString()
            }
            promptUserCity.text = user.city
            promptUserName.text = user.name
            Glide.with(context)
                .load(user.media?.firstOrNull()?.url?.fullSize)
                .thumbnail(0.1f)
                .placeholder(R.drawable.podo_bench_image_short)
                .into(promptUserImage)
        }
    }

    private fun EventPromptItemBinding.bindClicks(model: Prompt, position: Int) {
        promptAcceptButton.setOnClickListener {
            onPromptButtonClick.invoke(model, PROMPT_ACCEPT, position)
        }
        promptDeclineButton.setOnClickListener {
            onPromptButtonClick.invoke(model, PROMPT_DECLINE, position)
        }
        promptUserDataContainer.setOnClickListener {
            onUserClick.invoke(model)
        }
    }
}
