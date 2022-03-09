package com.dev.podo.core.ui.dialog

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.dev.podo.R
import com.dev.podo.common.adapters.tag.TagChooseListener
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.Section
import com.dev.podo.common.ui.bottomSheetChoose.TagSingleChooseBottomSheet
import com.dev.podo.common.utils.ModalViewHelper

class EventComplainTypesDialog(private val config: Config) {
    val dialog: TagSingleChooseBottomSheet = TagSingleChooseBottomSheet().apply {
        message = config.resources.getString(R.string.promptSelectTypeMessage, config.userName)
        title = config.resources.getString(R.string.promptSelectTypeTitle)
        chooseListener = config.listener
        val section =
            config.sectionList.getOrNull(0)
        val list = section?.let { listOf(it) } ?: emptyList()
        submitList(list)
    }

    fun show(fragmentManager: FragmentManager) {
        ModalViewHelper.safelyShow(
            dialog,
            fragmentManager,
            fragmentTag
        )
    }

    class Config(
        val userName: String,
        val listener: TagChooseListener,
        val sectionList: List<Section<Choice>>,
        val resources: Resources,
    )

    companion object {
        const val fragmentTag = "EventComplainBottomDialog"
    }
}
