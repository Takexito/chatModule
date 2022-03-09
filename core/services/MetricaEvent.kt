package com.dev.podo.core.services

import com.dev.podo.common.model.entities.Sex
import com.dev.podo.common.utils.DateUtil
import com.dev.podo.event.ui.create_event.CreateEventFragment.NavigationPlace as NavigationPlace
import com.dev.podo.home.model.entities.Tag
import com.dev.podo.podoplus.model.SubscriptionCode
import com.yandex.metrica.YandexMetrica
import java.util.*

open class MetricaEvent(var data: Map<String, Any?>? = null) {
    open val name: String = "PodoPlus_SubscribeButton"
    open fun reportEvent() {
        YandexMetrica.reportEvent(name, data)
    }

    protected fun isFilledConvert(text: String?): String {
        if (text.isNullOrEmpty()) {
            return "unfilled"
        }
        return "filled"
    }
}

class PodoPlusSubscribeButtonClickEvent : MetricaEvent() {
    override val name: String = "Страница PodoPlus_SubscribeButton"
}

class PodoPlusSubscribeSuccessEvent(
    private val subscriptionCode: SubscriptionCode,
) : MetricaEvent() {
    override val name: String = "Страница PodoPlus_SubscribeSuccess"

    fun reportEvent(
        createdEventCount: Int?,
        responsedEventCount: Int?
    ) {
        data = mapOf(
            "SubscribePeriod" to subscriptionCode.parseSubPeriod(),
            "CreatedEventCount" to createdEventCount,
            "ResponsedEventCount" to responsedEventCount
        )
        super.reportEvent()
    }

    private fun SubscriptionCode.parseSubPeriod(): String {
        return when (this) {
            SubscriptionCode.ONE_MONTH -> "month"
            SubscriptionCode.SIX_MONTHS -> "6month"
            SubscriptionCode.ONE_YEAR -> "year"
        }
    }
}

class EventCreateButtonClickEvent(
    private val place: NavigationPlace,
) : MetricaEvent() {
    override val name: String = "EventCreation"

    fun reportEvent(
        createdEventCount: Int?,
        responsedEventCount: Int?,
        podoPlusStatus: Boolean?,
        activeEventCount: Int?
    ) {
        data = mapOf(
            "CreatedEventCount" to createdEventCount,
            "ResponsedEventCount" to responsedEventCount,
            "PodoPlusStatus" to podoPlusStatus,
            "Place" to parsePlaceTitle(place),
            "ActiveEventCount" to activeEventCount
        )
        super.reportEvent()
    }

    private fun parsePlaceTitle(place: NavigationPlace): String {
        return when (place) {
            NavigationPlace.MAIN_SCREEN_PODONOW_CAROUSEL -> "MainScreen_PodoNowCarousel"
            NavigationPlace.MAIN_SCREEN_PODONOW_PLACEHOLDER -> "MainScreen_PodoNowPlaceholder"
            NavigationPlace.MAIN_SCREEN_DELAYED_EVENT_PLACEHOLDER -> "MainScreen_PlannedEventPlaceholder"
            NavigationPlace.MANAGE_EVENTS_CREATE_BUTTON -> "MessagesScreen_EventCreateButton"
        }
    }

}

class DelayedEventAddResponseEvent : MetricaEvent() {
    override val name: String = "PlannedEventResponse"

    fun reportEvent(
        respononerSex: Sex?,
        creatorSex: Sex?,
        creatorDescriptionIsEmpty: Boolean?,
        creatorPhotoCount: Int?,
        tags: Iterable<Tag>?,
        place: String?,
        city: String?,
        eventDate: Date?,
    ) {
        val daysToEvent = DateUtil.daysBetweenDates(Date(), eventDate)
        val tagsText = tags?.joinToString()

        data = mapOf(
            "RespononerSex" to (respononerSex?.title ?: ""),
            "CreatorSex" to (creatorSex?.title ?: ""),
            "CreatorDiscription" to creatorDescriptionIsEmpty.toString(),
            "CreatorPhoto" to creatorPhotoCount,
            "Tags" to tagsText,
            "Place" to place,
            "PlaceState" to (!place.isNullOrEmpty()).toString(),
            "City" to city,
//            "Time" to isFilledConvert(time),
            "TimeToEvent" to daysToEvent
        )
        super.reportEvent()
    }
}

class PodoNowEventAddResponseEvent : MetricaEvent() {
    override val name: String = "PodoNowResponse"

    fun reportEvent(
        respononerSex: Sex?,
        creatorSex: Sex?,
        creatorDescriptionIsEmpty: Boolean?,
        creatorPhotoCount: Int?,
        tag: String?,
        city: String?
    ) {
        data = mapOf(
            "RespononerSex" to (respononerSex?.title ?: ""),
            "CreatorSex" to (creatorSex?.title ?: ""),
            "CreatorDiscription" to creatorDescriptionIsEmpty.toString(),
            "CreatorPhoto" to creatorPhotoCount,
            "Tag" to tag,
            "City" to city
        )
        super.reportEvent()
    }
}

class EventResponseModalSendMessageEvent : MetricaEvent() {
    override val name: String = "ResponeModal_SendResponse"

    fun reportEvent(messageIsEmpty: Boolean) {
        val filled = if (messageIsEmpty) "unfilled" else "filled"
        data = mapOf("Message" to filled)
        super.reportEvent()
    }
}

class EventResponseModalCloseEvent : MetricaEvent() {
    override val name: String = "ResponeModal_Leave"
}

class SubscriptionIsNeededModalEvent : MetricaEvent() {
    override val name: String = "PodoPlusModal_Appear"

    fun reportEvent(place: String) {
        data = mapOf("ReasonAppear" to place)
        super.reportEvent()
    }
}

class SubscriptionIsNeededModalAcceptButtonClickEvent : MetricaEvent() {
    override val name: String = "PodoPlusModal_IWantPodoPlus"
}

class OnPromptAcceptEvent : MetricaEvent() {
    override val name: String = "Страница с откликами_ResponseAccept"

    fun reportEvent(
        messageIsEmpty: Boolean,
        respononerSex: Sex?,
        creatorSex: Sex?,
        respononerDiscriptionIsEmpty: Boolean,
        respononerPhotoCount: Int,
        eventId: Long
    ) {
        data = mapOf(
            "Message" to messageIsEmpty.toString(),
            "RespononerSex" to (respononerSex?.title ?: ""),
            "CreatorSex" to (creatorSex?.title ?: ""),
            "RespononerDiscription" to respononerDiscriptionIsEmpty.toString(),
            "RespononerPhoto" to respononerPhotoCount,
            "EventId" to eventId
        )
        super.reportEvent()
    }
}