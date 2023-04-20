package com.likewhile.meme.data.model

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.*

abstract class MemoItem(
    open var id: Long = -1L,
    open var title: String = "",
    open var date: Date = Date(),
    open var isFixed: Boolean = false
) {
    abstract val contentType: String
}

data class TextMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var content: String = "",
    override var date: Date = Date(),
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "TEXT"
}

data class ListMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var listItems: List<ListItem> = listOf(),
    override var date: Date = Date(),
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "LIST"
}

data class ImageMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var content: String,
    var uri : String = "",
    override var date: Date = Date(),
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "IMAGE"
}

fun serializeListContent(listItems: List<ListItem>): ByteArray {
    val jsonString = Gson().toJson(listItems)
    return jsonString.toByteArray(Charsets.UTF_8)
}

fun deserializeListContent(data: ByteArray): List<ListItem> {
    val jsonString = String(data, Charsets.UTF_8)
    return Gson().fromJson(jsonString, object : TypeToken<List<ListItem>>() {}.type)
}

