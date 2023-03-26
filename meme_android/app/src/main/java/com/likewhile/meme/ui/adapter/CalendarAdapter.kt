package com.likewhile.meme.ui.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likewhile.meme.data.model.CalendarItem
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.ItemCalendarBinding
import com.likewhile.meme.util.CalendarUtil
import java.util.*

private const val TAG = "CalendarAdapter"
@SuppressLint("NotifyDataSetChanged")
class CalendarAdapter(private val onMonthChangeListener: OnMonthChangeListener? = null) : RecyclerView.Adapter<CalendarAdapter.CalendarItemViewHolder>() {

    private val baseCalendar = CalendarUtil()
    private lateinit var itemClickListener: OnItemClickListener
    private var feedList: List<CalendarItem>? = null

    init {
        baseCalendar.initBaseCalendar {
            onMonthChangeListener?.onMonthChanged(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCalendarBinding.inflate(layoutInflater, parent, false)
        return CalendarItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return CalendarUtil.LOW_OF_CALENDAR * CalendarUtil.DAYS_OF_WEEK
    }

    fun changeToPrevMonth() {
        baseCalendar.changeToPrevMonth {
            removeItem()
            onMonthChangeListener?.onMonthChanged(it)
            notifyDataSetChanged()
        }
    }

    fun changeToNextMonth() {
        baseCalendar.changeToNextMonth {
            removeItem()
            onMonthChangeListener?.onMonthChanged(it)
            notifyDataSetChanged()
        }
    }

    interface OnMonthChangeListener {
        fun onMonthChanged(calendar : Calendar)
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int, day: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class CalendarItemViewHolder(private val binding: ItemCalendarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: Int, position: Int) {
            binding.tvDate.text = date.toString()

            if (position < baseCalendar.preMonth
                || position >= baseCalendar.preMonth + baseCalendar.currentMonth) {
                binding.item.visibility = View.GONE
            } else {
                binding.item.visibility = View.VISIBLE
            }

            if (!feedList.isNullOrEmpty()) {
                for (feed in feedList!!) {
                    if (feed.day == date) {
                        Log.d(TAG, "bind: $feed , $date")
                        binding.point.visibility = View.VISIBLE
                        itemView.setOnClickListener {
                            itemClickListener.onClick(itemView, position, feed.day)
                        }
                    }
                }
            } else {
                binding.point.visibility = View.GONE
            }
        }
    }
    fun setItems(items: List<CalendarItem>) {
        feedList = items
        notifyDataSetChanged()
    }

    private fun removeItem() {
        feedList = null
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CalendarItemViewHolder, position: Int) {
        holder.bind(baseCalendar.data[position], position)
    }

    fun getDateForPosition(position: Int): Date {
        val date = baseCalendar.data[position]
        val currentMonthCalendar = baseCalendar.calendar.clone() as Calendar
        currentMonthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        currentMonthCalendar.add(Calendar.DAY_OF_MONTH, - baseCalendar.preMonth)
        currentMonthCalendar.add(Calendar.DAY_OF_MONTH, date - 1)

        return currentMonthCalendar.time
    }
}