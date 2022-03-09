package com.dev.podo.core.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dev.podo.databinding.PodoNowHomeItemBinding
import com.dev.podo.home.model.entities.HomeEventModel
import com.eudycontreras.boneslibrary.bindings.addSkeletonLoader
import com.eudycontreras.boneslibrary.extensions.disableSkeletonLoading
import com.eudycontreras.boneslibrary.extensions.dp
import com.eudycontreras.boneslibrary.framework.skeletons.SkeletonDrawable
import com.eudycontreras.boneslibrary.properties.CornerRadii
import com.eudycontreras.boneslibrary.properties.MutableColor

typealias OnRecyclerItemClick = (pos: Int) -> Unit
typealias ViewBindingGetter<B> = (parent: ViewGroup) -> B

abstract class BaseAdapter<B : ViewBinding, D>(
    val onItemClick: OnRecyclerItemClick? = {},
    protected val getViewBinding: ViewBindingGetter<B>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var viewBinding: B? = null
    val context: Context?
        get() = viewBinding?.root?.context

    var data: List<D> = listOf()
        set(value) {
            field = value
            // TODO: create diff utils
            notifyDataSetChanged()
        }

    class BaseViewHolder<B : ViewBinding>(val viewBinding: B) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        viewBinding = getViewBinding.invoke(parent)
        return BaseViewHolder(viewBinding!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Todo: uncheck cast
        bind((holder as BaseViewHolder<B>).viewBinding, position)
        if (isLoading){
            enableSkeleton(holder.viewBinding)
            return
        } else {
            disableSkeleton(holder.viewBinding)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    abstract fun bind(viewBinding: B, position: Int)

    var emptyItem: D? = null
    var skeletonCount = 0

    private var isLoading = false

    fun changeState(isLoading: Boolean){
        this.isLoading = isLoading
        if(isLoading){
            data = emptyItem.createList(skeletonCount)
        } else {
            notifyDataSetChanged()
        }
    }

    private fun <T> Any?.createList(count: Int): List<T>{
        if(this == null) return emptyList()
        val arrayList = arrayListOf<T>()
        repeat(count){
            arrayList.add(this as T)
        }
        return arrayList
    }

    fun enableSkeleton(viewBinding: B) {
        (viewBinding.root as? ViewGroup)?.let {
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

    fun disableSkeleton(viewBinding: B) {
        (viewBinding.root as? ViewGroup)?.disableSkeletonLoading()
    }
}
