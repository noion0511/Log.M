package com.likewhile.meme.util

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.likewhile.meme.R
import com.likewhile.meme.ui.adapter.ListAdapter

class ListItemTouchHelperCallback(private val adapter: ListAdapter) : ItemTouchHelper.Callback() {
    private var enabled: Boolean = true
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (enabled) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.END
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            makeMovementFlags(0, 0)
        }
    }
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMove(viewHolder.layoutPosition, target.layoutPosition)
        viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.teal_700))
        return true
    }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        viewHolder?.itemView?.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.teal_700))
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
        adapter.removeItem(position)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.setBackgroundColor(Color.WHITE)
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
