package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.MemoItem
import java.util.*

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


    fun deleteMemo(id: Long) {
        memoDBHelper.deleteMemo(id)
        refreshMemos()
    }

    fun deleteAllMemos() {
        memoDBHelper.deleteAllMemos()
        refreshMemos()
    }

    fun closeDB() {
        memoDBHelper.close()
    }
}