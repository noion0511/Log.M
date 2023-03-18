package com.ssafy.memo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.memo.databinding.ItemMemoBinding

class MemoAdapter(private val memoItems: MutableList<MemoItem>, private val onItemClickListener: OnItemClickListener, private val onItemLongClickListener: OnItemLongClickListener
    ) :
    RecyclerView.Adapter<MemoAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(memoItem: MemoItem)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(memoItem: MemoItem) : Boolean
    }

    inner class ViewHolder(val binding: ItemMemoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memoItem = memoItems[position]
        holder.binding.memo = memoItem
        holder.binding.executePendingBindings()

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(memoItem)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener.onItemLongClick(memoItem)
        }
    }

    override fun getItemCount(): Int {
        return memoItems.size
    }

    fun clear() {
        memoItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(memoItems: List<MemoItem>) {
        this.memoItems.addAll(memoItems)
        notifyDataSetChanged()
    }

    fun getMemoItem(position: Int): MemoItem {
        return memoItems[position]
    }
}