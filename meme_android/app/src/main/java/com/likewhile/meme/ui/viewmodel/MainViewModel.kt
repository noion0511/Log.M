package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.MemoItem

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memos = MutableLiveData<MutableList<MemoItem>>()
    val memos: LiveData<MutableList<MemoItem>> get() = _memos

    private var sortType = 1
    init {
        memoDBHelper = MemoDBHelper(application)
        refreshMemos()
    }

    fun refreshMemos() {
        _memos.value = memoDBHelper.selectAllMemos(sortType)
    }

    fun setSortType(newSortType: Int) {
        sortType = newSortType
        refreshMemos()
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