package ru.coursefinder.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import ru.coursefinder.domain.model.User

object UserListConverter {
    @TypeConverter
    fun List<User>.convertToString(): String {
        return Json.encodeToString(this)
    }

    @TypeConverter
    fun String.convertToUserList(): List<User> {
        return Json.decodeFromString(this)
    }
}