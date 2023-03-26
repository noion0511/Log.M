package com.likewhile.meme.util

import java.util.*

class CalendarUtil {
    companion object {
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    }

    val calendar = Calendar.getInstance()
    var preMonth = 0
    var nextMonth = 0
    var currentMonth = 0
    var data = arrayListOf<Int>()

    init {
        calendar.time = Date()
    }


    fun initBaseCalendar(refreshCallback: (Calendar) -> Unit) {
        makeMonthDate(refreshCallback)
    }


    fun changeToPrevMonth(refreshCallback: (Calendar) -> Unit) {
        if(calendar.get(Calendar.MONTH) == 0){
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        }
        makeMonthDate(refreshCallback)
    }


    fun changeToNextMonth(refreshCallback: (Calendar) -> Unit) {
        if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
            calendar.set(Calendar.MONTH, 0)
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
        }
        makeMonthDate(refreshCallback)
    }


    private fun makeMonthDate(refreshCallback: (Calendar) -> Unit) {
        data.clear()

        calendar.set(Calendar.DATE, 1)

        currentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        preMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1

        makePrevMonthTail(calendar.clone() as Calendar)
        makeCurrentMonth(calendar)

        nextMonth = LOW_OF_CALENDAR * DAYS_OF_WEEK - (preMonth + currentMonth)
        makeNextMonthHead()

        refreshCallback(calendar)
    }


    private fun makePrevMonthTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        var maxOffsetDate = maxDate - preMonth

        for (i in 1..preMonth) data.add(++maxOffsetDate)
    }


    private fun makeCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) data.add(i)
    }


    private fun makeNextMonthHead() {
        var date = 1

        for (i in 1..nextMonth) data.add(date++)
    }
}