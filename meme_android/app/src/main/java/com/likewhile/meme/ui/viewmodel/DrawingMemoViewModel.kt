package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.DrawingMemoItem
import com.likewhile.meme.data.model.MemoItem

class DrawingMemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memo = MediatorLiveData<DrawingMemoItem>()
    val memo: LiveData<DrawingMemoItem> get() = _memo

    private var itemId: Long = -1

    init {
        memoDBHelper = MemoDBHelper(application)
    }

    fun setItemId(id: Long) {
        itemId = id
        refreshMemo()
    }

    private fun refreshMemo() {
        _memo.value = memoDBHelper.selectMemo(itemId) as DrawingMemoItem
    }

    fun insertMemo(memoItem: MemoItem) {
        memoDBHelper.insertMemo(memoItem)
    }

    fun updateMemo(memoItem: MemoItem) {
        memoDBHelper.updateMemo(memoItem)
    }

    fun closeDB() {
        memoDBHelper.close()
    }
}