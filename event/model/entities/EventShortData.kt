package com.dev.podo.event.model.entities

import android.os.Parcelable
import com.dev.podo.common.model.entities.EventType
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventShortData(
    val id: Long,
    val city: String? = "",
    val title: String? = "",
    val type: EventType = EventType.DELAYED_EVENT
) : Parcelable
