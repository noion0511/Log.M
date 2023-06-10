package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.ListMemoItem
import com.likewhile.meme.data.model.MemoItem

class ListMemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memo = MediatorLiveData<ListMemoItem>()
    val memo: LiveData<ListMemoItem> get() = _memo

    private var itemId: Long = -1

    init {
        memoDBHelper = MemoDBHelper(application)
    }

    fun setItemId(id: Long) {
        itemId = id
        refreshMemo()
    }

    private fun refreshMemo() {
        _memo.value = memoDBHelper.selectMemo(itemId) as ListMemoItem
    }

    fun insertMemo(memoItem: MemoItem) : Long {
        return memoDBHelper.insertMemo(memoItem)
    }

    fun updateMemo(memoItem: MemoItem) {
        memoDBHelper.updateMemo(memoItem)
        refreshMemo()
    }


    fun deleteMemo(memoItemId: Long) : Boolean {
        return memoDBHelper.deleteMemo(memoItemId)
    }


    fun closeDB() {
        memoDBHelper.close()
    }
}