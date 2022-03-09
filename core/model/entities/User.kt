package com.dev.podo.core.model.entities

import android.os.Build
import com.dev.podo.R
import com.dev.podo.common.model.entities.Media
import com.dev.podo.common.model.entities.Sex
import com.dev.podo.common.utils.DateFormatter
import com.dev.podo.common.utils.exceptions.ResourceException
import com.dev.podo.event.model.entities.inputDateFormat
import com.dev.podo.home.model.dto.serverDateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*

data class User(
    var id: Long? = null,
    var name: String? = null,
    var phone: String? = null,
    var birthDate: String? = null,
    var age: Int? = null,
    var sex: Sex? = null,
    var cityId: Int? = null,
    var city: String? = null,
    var description: String? = null,
    var registered: Boolean? = false,
    var approved: Boolean? = null,
    var isVip: Boolean? = null,
    var subscription: Subscription? = null,
    val media: List<Media>? = null,
    val deletedAt: String? = null
) {

    val hasSubscription: Boolean
        get() = subscription != null

    data class Subscription(val until: Date)

    companion object {
        fun isPhoneValid(phone: String): Boolean {
            if (phone.isEmpty() || phone.length != 11) {
                return false
            }
            return true
        }

        fun calculateAgeByBirthDate(birthDate: String): Int {
            var date: Date? = null
            val dtStart = "11/08/2013 08:48:10"
            val format = SimpleDateFormat("yyyy-MM-dd")
            try {
                date = format.parse(dtStart)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            require(date != null)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Period.between(
                    LocalDate.of(date.year, date.month, date.day),
                    LocalDate.now()
                ).years
            } else {
                return 0
                TODO("VERSION.SDK_INT < O")
            }
        }
    }

    @Throws(ResourceException::class)
    fun validate(
        nameValidate: Boolean = true,
        birthdateValidate: Boolean = true,
        sexValidate: Boolean = true,
        cityValidate: Boolean = true
    ) {
        if (nameValidate && name.isNullOrEmpty()) {
            throw ResourceException(stringId = R.string.name_not_entered)
        }
        if (birthdateValidate && birthDate.isNullOrEmpty()) {
            throw ResourceException(stringId = R.string.birthdate_not_entered)
        }
        if (birthdateValidate && isBirthValid() == false) {
            throw ResourceException(stringId = R.string.birthdate_incorrect)
        }
        if (sexValidate && sex == null) {
            throw ResourceException(stringId = R.string.sex_not_chosen)
        }
        if (cityValidate && isCityValid() == false) {
            throw ResourceException(stringId = R.string.city_not_chosen)
        }
    }

    fun isRegistered(): Boolean {
        if (registered != true) {
            if (name.isNullOrEmpty() || phone.isNullOrEmpty() || age == null ||
                sex == null || city.isNullOrEmpty() || description.isNullOrEmpty()
            ) {
                return false
            }
            return true
        }
        return registered ?: false
    }

    fun isDataValid(): Boolean {
        if (name.isNullOrEmpty() || isBirthValid()) {
            return false
        }
        return true
    }

    fun isCityValid(): Boolean {
        if (cityId == null) {
            return false
        }
        return true
    }

    private fun isBirthValid(): Boolean {
        return DateFormatter.isBirthDateValid("dd.MM.yyyy", birthDate ?: "")
    }

    fun convertBirthDateToServerFormat() {
        val formattedDate = birthDate?.let {
            DateFormatter.convert("dd.MM.yyyy", "yyyy-MM-dd", it)
        }
        birthDate = formattedDate
    }

    fun convertBirthDateToClientFormat() {
        val formattedDate = birthDate?.let {
            DateFormatter.convert("yyyy-MM-dd", "dd.MM.yyyy", it)
        }
        birthDate = formattedDate
    }

    fun convertedDeletedAt(): String? {
        return deletedAt?.let { DateFormatter.convert(serverDateFormat, inputDateFormat, it) }
    }
}
