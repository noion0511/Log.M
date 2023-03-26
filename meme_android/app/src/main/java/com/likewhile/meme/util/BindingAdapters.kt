package com.likewhile.meme.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.*

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(date: Date?) {
    date?.let {
        text = DateFormatUtil.formatDate(it)
    } ?: run {
        text = ""
    }
}