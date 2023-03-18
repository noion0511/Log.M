package com.ssafy.memo.util

import java.text.SimpleDateFormat
import java.util.*

class Utils {
    companion object {
        fun formatDate(date: Date, format: String): String {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            return formatter.format(date)
        }
    }
}