package com.ssafy.memo.util

import android.content.Context

private const val PREFS_NAME = "MemoWidgetPrefs"
private const val PREFS_PREFIX = "widget_"

fun getMemoIdForWidget(context: Context, widgetId: Int): Long? {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val memoId = sharedPreferences.getLong(PREFS_PREFIX + widgetId, -1L)

    return if (memoId != -1L) memoId else null
}

fun saveMemoIdForWidget(context: Context, widgetId: Int, memoId: Long) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putLong(PREFS_PREFIX + widgetId, memoId)
    editor.apply()
}

fun loadMemoIdForWidget(context: Context, widgetId: Int): Long {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getLong(PREFS_PREFIX + widgetId, -1L)
}

fun removeMemoIdForWidget(context: Context, widgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.remove(PREFS_PREFIX + widgetId)
    editor.apply()
}