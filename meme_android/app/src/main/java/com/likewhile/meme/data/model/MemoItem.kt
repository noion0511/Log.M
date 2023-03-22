package com.likewhile.meme.data.model

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File

abstract class MemoItem(
    open var id: Long = -1L,
    open var title: String = "",
    open var date: String = "",
    open var isFixed: Boolean = false
) {
    abstract val contentType: String
}

data class TextMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var content: String = "",
    override var date: String = "",
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "TEXT"
}

data class ListMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var listItems: List<ListItem> = listOf(),
    override var date: String = "",
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "LIST"
}

data class DrawingMemoItem(
    override var id: Long = -1L,
    override var title: String = "",
    var drawingPath: String = "",
    override var date: String = "",
    override var isFixed: Boolean = false
) : MemoItem() {
    override val contentType: String = "DRAWING"
}

fun serializeListContent(listItems: List<ListItem>): ByteArray {
    val jsonString = Gson().toJson(listItems)
    return jsonString.toByteArray(Charsets.UTF_8)
}

fun deserializeListContent(data: ByteArray): List<ListItem> {
    val jsonString = String(data, Charsets.UTF_8)
    return Gson().fromJson(jsonString, object : TypeToken<List<ListItem>>() {}.type)
}

fun serializeDrawingContent(drawingPath: String): ByteArray {
    val file = File(drawingPath)
    val bytes = file.readBytes()
    return bytes
}

fun deserializeDrawingContent(data: ByteArray, filePath: String): String {
    val file = File(filePath)
    file.writeBytes(data)
    return filePath
}

