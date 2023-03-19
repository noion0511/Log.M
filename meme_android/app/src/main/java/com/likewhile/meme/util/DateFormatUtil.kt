package com.likewhile.meme.util

import java.text.SimpleDateFormat
import java.util.*

class DateFormatUtil {
    companion object {
        fun formatDate(date: Date, format: String): String {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            return formatter.format(date)
        }
    }
}