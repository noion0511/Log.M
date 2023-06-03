package com.likewhile.meme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.CalendarItem
import com.likewhile.meme.data.model.MemoItem
import java.util.*

class CalendarModeViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDBHelper: MemoDBHelper

    private var _memos = MutableLiveData<MutableList<CalendarItem>>()
    val memos: LiveData<MutableList<CalendarItem>> get() = _memos

    private var _memosDate = MutableLiveData<List<MemoItem>>()
    val memosDate: LiveData<List<MemoItem>> get() = _memosDate

    private var sortType = 1
    private lateinit var calendarData: Calendar

    init {
        memoDBHelper = MemoDBHelper(application)
    }

    fun setSortType(newSortType: Int) {
        sortType = newSortType
    }

    fun setCalendarMonthData(calendar: Calendar? = null) {
        if (calendar != null) {
            calendarData = calendar
        }
        if (::calendarData.isInitialized) {
            val year = calendarData.get(Calendar.YEAR)
            val month = calendarData.get(Calendar.MONTH) + 1
            _memos.value = memoDBHelper.getMemosByMonth(year, month)
        }
    }

    fun setCalendarDateData(date: Int) {
        val year = calendarData.get(Calendar.YEAR)
        val month = calendarData.get(Calendar.MONTH) + 1

        _memosDate.value = memoDBHelper.getMemosByDate(year, month, date, sortType)
    }

    fun closeDB() {
        memoDBHelper.close()
    }
}