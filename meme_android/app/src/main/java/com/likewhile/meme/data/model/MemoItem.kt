package com.likewhile.meme.data.model

data class MemoItem(
    var id: Long = -1L,
    var title: String = "meme",
    var content: String,
    var date: String,
    var isFixed: Boolean
)