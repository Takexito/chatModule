package com.dev.podo.core.ui.dialog

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.dev.podo.R
import com.dev.podo.common.ui.CustomInputDialogAction
import com.dev.podo.common.ui.CustomInputDialogBuilder
import com.dev.podo.common.ui.CustomInputDialogFragment
import com.dev.podo.common.utils.ModalViewHelper

class EventComplainDialog(
    resources: Resources,
    title: String,
    onAction: CustomInputDialogAction,
) {

    val dialog: CustomInputDialogFragment =
        CustomInputDialogBuilder()
            .title(title)
            .message(resources.getString(R.string.event_compain_dialog_message))
            .inputHint(resources.getString(R.string.description))
            .actionButtonTitle(resources.getString(R.string.event_complain_dialog_action))
            .actionButtonIfInputEmptyTitle(resources.getString(R.string.event_complain_dialog_if_message_empty_action))
            .cancelButtonTitle(resources.getString(R.string.cancel))
            .onAction(onAction)
            .build()

    fun show(fragmentManager: FragmentManager) {
        ModalViewHelper.safelyShow(
            dialog,
            fragmentManager,
            fragmentTag
        )
    }

    companion object {
        const val fragmentTag = "EventComplainDialog"
    }
}
