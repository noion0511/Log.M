package com.likewhile.meme

data class MemoItem(
    var id: Long = -1L,
    var title: String = "com.likewhile.com.likewhile.meme",
    var content: String,
    var date: String,
    var isFixed: Boolean
)