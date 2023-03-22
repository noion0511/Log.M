package com.likewhile.meme.data.model

enum class MemoType(val typeValue: Int) {
    TEXT(0),
    LIST(1),
    DRAWING(2);
    companion object {
        fun fromInt(value: Int): MemoType {
            return when (value) {
                1 -> LIST
                2 -> DRAWING
                else -> TEXT
            }
        }
    }
}