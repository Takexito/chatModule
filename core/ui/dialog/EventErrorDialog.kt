package com.dev.podo.core.ui.dialog

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.dev.podo.R
import com.dev.podo.common.ui.CustomImageDialogAction
import com.dev.podo.common.ui.CustomImageDialogBuilder
import com.dev.podo.common.ui.CustomImageDialogFragment
import com.dev.podo.common.utils.ModalViewHelper

class EventErrorDialog(
    resources: Resources,
    exception: Exception,
    onAction: CustomImageDialogAction = {},
) {
    private val defaultMessage =
        resources.getString(R.string.event_response_error_dialog_message)

    val dialog: CustomImageDialogFragment =
        CustomImageDialogBuilder()
            .title(resources.getString(R.string.event_response_error_dialog_title))
            .message(exception.message ?: defaultMessage)
            .actionButtonTitle(resources.getString(R.string.event_response_error_dialog_action))
            .cancelButtonTitle(resources.getString(R.string.event_response_error_dialog_cancel))
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
