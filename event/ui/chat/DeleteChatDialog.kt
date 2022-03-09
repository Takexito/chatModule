package com.dev.podo.event.ui.chat

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.dev.podo.R
import com.dev.podo.common.ui.CustomDialogAction
import com.dev.podo.common.ui.CustomDialogBuilder
import com.dev.podo.common.ui.CustomDialogFragment
import com.dev.podo.common.utils.ModalViewHelper

class DeleteChatDialog(
    resources: Resources,
    onAction: CustomDialogAction,
) {
    val dialog: CustomDialogFragment = CustomDialogBuilder()
        .title(resources.getString(R.string.delete_chat_question))
        .message(resources.getString(R.string.chat_will_be_deleted))
        .actionButtonTitle(resources.getString(R.string.delete))
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
        const val fragmentTag = "ChatDelete"
    }
}
