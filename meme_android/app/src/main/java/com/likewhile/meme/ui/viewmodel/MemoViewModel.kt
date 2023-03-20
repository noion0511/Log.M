package com.likewhile.meme.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.MemoItem

class MemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memos = MutableLiveData<MutableList<MemoItem>>()
    val memos: LiveData<MutableList<MemoItem>> get() = _memos

    private var _memo = MediatorLiveData<MemoItem>()
    val memo: LiveData<MemoItem> get() = _memo

    private var sortType = 1
    private var itemId: Long = -1


    init {
        memoDBHelper = MemoDBHelper(application)
        refreshMemos()

        _memo.addSource(_memos) { memos ->
            val currentMemo = memos.firstOrNull { it.id == itemId }
            _memo.value = currentMemo
        }
    }

    fun refreshMemos() {
        _memos.value = memoDBHelper.selectAllMemos(sortType)
    }


    fun setItemId(id: Long) {
        itemId = id
    }


    fun setSortType(newSortType: Int) {
        sortType = newSortType
        refreshMemos()
    }


    fun insertMemo(memoItem: MemoItem) {
        memoDBHelper.insertMemo(memoItem)
    }


    fun updateMemo(memoItem: MemoItem) {
        memoDBHelper.updateMemo(memoItem)
    }

    fun deleteAllMemos() {
        memoDBHelper.deleteAllMemos()
        refreshMemos()
    }

    fun deleteMemo(id: Long) {
        memoDBHelper.deleteMemo(id)
        refreshMemos()
    }

    fun closeDB() {
        memoDBHelper.close()
    }
}