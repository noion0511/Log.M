package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.ImageItem
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.TextMemoItem

class TextMemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memo = MediatorLiveData<TextMemoItem>()
    val memo: LiveData<TextMemoItem> get() = _memo

    private var itemId: Long = -1

    private val imageList = mutableListOf<ImageItem>()
    private var _imageListLiveData = MutableLiveData<MutableList<ImageItem>>()
    val imageListLiveData : LiveData<MutableList<ImageItem>>
        get() = _imageListLiveData

    init {
        memoDBHelper = MemoDBHelper(application)
        _imageListLiveData.postValue(imageList)
    }

    fun setItemId(id: Long) {
        itemId = id
        refreshMemo()
    }

    private fun refreshMemo() {
        _memo.value = memoDBHelper.selectMemo(itemId) as TextMemoItem
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

    fun addImageItem(item:ImageItem){
        imageList.add(item)
        _imageListLiveData.postValue(imageList)
    }
    fun removeImageItem(item:ImageItem){
        imageList.remove(item)
        _imageListLiveData.postValue(imageList)
    }

    fun closeDB() {
        memoDBHelper.close()
    }
}
