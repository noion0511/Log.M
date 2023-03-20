package com.likewhile.meme.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.ItemMemoBinding
import com.likewhile.meme.databinding.ItemMemoDetailBinding

class MemoAdapter(private val memoItems: MutableList<MemoItem>, private val onItemClickListener: OnItemClickListener, private val onItemLongClickListener: OnItemLongClickListener
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var currentViewType: Int = TYPE_ITEM_SIMPLE
    interface OnItemClickListener {
        fun onItemClick(memoItem: MemoItem)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(memoItem: MemoItem) : Boolean
    }

    inner class ViewHolderSimple(val binding: ItemMemoBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ViewHolderDetail(val binding: ItemMemoDetailBinding) : RecyclerView.ViewHolder(binding.root)



    override fun getItemViewType(position: Int): Int {
        return currentViewType
    }

    fun setViewType(viewType: Int) {
        currentViewType = viewType
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM_SIMPLE -> {
                val binding = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolderSimple(binding)
            }
            TYPE_ITEM_DETAIL -> {
                val binding = ItemMemoDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolderDetail(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val memoItem = memoItems[position]
        when (holder) {
            is ViewHolderSimple -> {
                holder.binding.memo = memoItem
                holder.binding.executePendingBindings()

                holder.itemView.setOnClickListener {
                    onItemClickListener.onItemClick(memoItem)
                }

                holder.itemView.setOnLongClickListener {
                    onItemLongClickListener.onItemLongClick(memoItem)
                }
            }
            is ViewHolderDetail -> {
                holder.binding.memo = memoItem
                holder.binding.executePendingBindings()

                holder.itemView.setOnClickListener {
                    onItemClickListener.onItemClick(memoItem)
                }

                holder.itemView.setOnLongClickListener {
                    onItemLongClickListener.onItemLongClick(memoItem)
                }
            }
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

    companion object {
        const val TYPE_ITEM_SIMPLE = 1
        const val TYPE_ITEM_DETAIL = 2
    }
}