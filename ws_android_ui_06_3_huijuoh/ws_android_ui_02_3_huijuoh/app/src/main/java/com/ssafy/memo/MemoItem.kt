package com.ssafy.memo

data class MemoItem(
    var id: Long = -1L,
    var title: String = "memo",
    var content: String,
    var date: String,
    var isFixed: Boolean
)