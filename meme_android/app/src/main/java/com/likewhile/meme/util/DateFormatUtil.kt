package com.likewhile.meme.util
import java.text.SimpleDateFormat
import java.util.*

class DateFormatUtil {
    companion object {
        fun formatDate(date: Date): String {
            val locale = Locale.getDefault()
            val format = getFormatForLocale(locale)
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(date)
        }

        private fun getFormatForLocale(locale: Locale): String {
            return when (locale.language) {
                "en" -> "MMMM d, yyyy"
                "ko" -> "yyyy년 MM월 dd일"
                "zh" -> {
                    if (locale.country == "CN") "yyyy年MM月dd日" // Simplified Chinese
                    else "yyyy年MM月dd日" // Traditional Chinese
                }
                "ja" -> "yyyy年MM月dd日"
                "es" -> "d 'de' MMMM 'de' yyyy"
                else -> "yyyy-MM-dd" // Default format
            }
        }

        fun dateToString(date: Date): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun stringToDate(dateString: String): Date {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.parse(dateString) ?: Date()
        }

        fun getDayFromDate(dateString: String): Int {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString)
            return if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.DAY_OF_MONTH)
            } else {
                throw IllegalArgumentException("Invalid date format")
            }
        }
    }
}