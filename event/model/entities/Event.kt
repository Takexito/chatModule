package com.dev.podo.event.model.entities

import com.dev.podo.R
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.utils.Constants
import com.dev.podo.common.utils.DateFormatter
import com.dev.podo.common.utils.exceptions.ResourceException
import com.dev.podo.event.model.dto.CreateEventDto
import com.dev.podo.event.model.dto.EventType
import java.text.ParseException
import java.util.*

const val inputDateFormat = "dd.MM.yyyy"
const val inputTimeFormat = "HH:mm"
const val serverDateFormat = "yyyy-MM-dd HH:mm:ss"

data class Event(
    var title: String? = null,
    var description: String? = null,
    var type: EventType? = null,
    var startAt: String = "",
    var time: String = "",
    var preferred: String? = null,
    var place: String? = null,
    var tags: List<Choice> = arrayListOf(),
    var club: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var videoId: String? = null
) {
    fun asDto(): CreateEventDto {

        var time: String? = null
        if (type == EventType.EVENT) {
            time = convertTime()
        }

        return CreateEventDto(
            title = title,
            description = description,
            type = convertType(),
            startAt = time,
            preferred = convertPreferred(),
            place = place,
            tags = getTagIds(),
            club = club,
            video = videoId
        )
    }

    @Throws(ResourceException::class)
    fun validate() {
        if (type == null) {
            throw ResourceException(stringId = R.string.event_type_is_empty)
        }
        if (title.isNullOrEmpty()) {
            throw ResourceException(stringId = R.string.title_is_empty)
        }
        if (type == EventType.EVENT) {
            if (description.isNullOrEmpty()) {
                throw ResourceException(stringId = R.string.description_is_empty)
            }
            if (isTimeInvalid()) {
                throw ResourceException(stringId = R.string.time_is_invalid)
            }
            if (isDateInvalid()) {
                throw ResourceException(stringId = R.string.date_is_invalid)
            }
        }
        if (preferred == null) {
            throw ResourceException(stringId = R.string.sex_is_not_chosen)
        }
        if (tags.isEmpty()) {
            throw ResourceException(stringId = R.string.tag_is_empty)
        }
    }

    private fun isDateInvalid(): Boolean {
        if (startAt.matches(Constants.dateRegex()) &&
            DateFormatter.isDateValid(inputDateFormat, startAt)
        ) {
            var timeAsDate: Date? = null
            if (time.isNotEmpty()) {
                timeAsDate = DateFormatter.getDateByRaw(inputTimeFormat, time)
            }
            val date = DateFormatter.getDateByRaw(inputDateFormat, startAt)
            if (DateFormatter.isEventDateValid(date, timeAsDate)) {
                return false
            }
        }
        return true
    }

    private fun isTimeInvalid(): Boolean {
        if (time.isEmpty()) {
            return false
        }
        if (time.matches(Constants.timeRegex()) &&
            DateFormatter.isDateValid(inputTimeFormat, time)
        ) {
            return false
        }
        return true
    }

    fun convertPreferred(): String {
        return preferred ?: ""
    }

    fun convertTime(): String? {
        var convertedDate: String? = null
        try {
            if (time.isEmpty()) {
                convertedDate = DateFormatter.convert(
                    inputDateFormat,
                    serverDateFormat,
                    startAt
                )
                return convertedDate
            }
            if (startAt.isEmpty() || time.isEmpty()) {
                return convertedDate
            }
            convertedDate = DateFormatter.getDateWithTime(
                inputDateFormat,
                inputTimeFormat,
                startAt,
                time,
                serverDateFormat
            )
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
        return convertedDate
    }

    fun convertType(): String? {
        var typeTitle: String? = null
        type?.let {
            typeTitle = it.name.lowercase()
        }
        return typeTitle
    }

    fun getTagIds(): List<Int> {
        return tags.map { it.id }
    }
}
