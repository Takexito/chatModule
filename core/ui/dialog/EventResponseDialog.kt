package com.dev.podo.core.ui.dialog

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.dev.podo.R
import com.dev.podo.common.model.entities.EventType
import com.dev.podo.common.ui.CustomInputDialogAction
import com.dev.podo.common.ui.CustomInputDialogBuilder
import com.dev.podo.common.ui.CustomInputDialogCancel
import com.dev.podo.common.ui.CustomInputDialogFragment
import com.dev.podo.common.utils.ModalViewHelper
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.services.DelayedEventAddResponseEvent
import com.dev.podo.core.services.EventResponseModalCloseEvent
import com.dev.podo.core.services.PodoNowEventAddResponseEvent
import com.dev.podo.home.model.entities.HomeEventModel

class EventResponseDialog(
    private val event: HomeEventModel,
    resources: Resources,
    onAction: CustomInputDialogAction,
    onCancel: CustomInputDialogCancel = {},
) {
    val dialog: CustomInputDialogFragment =
        CustomInputDialogBuilder()
            .title(resources.getString(R.string.event_response_dialog_title))
            .message(resources.getString(R.string.event_response_dialog_message))
            .inputHint(resources.getString(R.string.message))
            .actionButtonTitle(resources.getString(R.string.event_response_dialog_action))
            .actionButtonIfInputEmptyTitle(resources.getString(R.string.event_response_dialog_if_message_empty_action))
            .cancelButtonTitle(resources.getString(R.string.cancel))
            .onCancel {
                EventResponseModalCloseEvent().reportEvent()
                onCancel.invoke()
            }
            .onAction(onAction)
            .build()

    fun show(fragmentManager: FragmentManager) {
        sendMetrics()
        ModalViewHelper.safelyShow(
            dialog,
            fragmentManager,
            fragmentTag
        )
    }

    private fun sendMetrics() {
        val creator = event.user
        when (event.type) {
            EventType.DELAYED_EVENT -> {
                DelayedEventAddResponseEvent().reportEvent(
                    Storage.user?.sex,
                    creator?.sex,
                    creator?.description?.isEmpty(),
                    creator?.media?.count(),
                    event.tags,
                    event.place,
                    event.city,
                    event.startAt
                )
            }
            EventType.LIVE_EVENT -> {
                PodoNowEventAddResponseEvent().reportEvent(
                    Storage.user?.sex,
                    creator?.sex,
                    creator?.description?.isEmpty(),
                    creator?.media?.count(),
                    event.tags.firstOrNull()?.title,
                    event.city
                )
            }
        }
    }

    companion object {
        const val fragmentTag = "EventResponseDialog"
    }
}
