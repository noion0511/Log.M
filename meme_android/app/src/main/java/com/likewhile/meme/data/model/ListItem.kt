package com.likewhile.meme.data.model

data class ListItem(
    val title: String,
    val priority: Int,
    val isCompleted: Boolean = false
)