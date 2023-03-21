package com.likewhile.meme.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.likewhile.meme.data.model.ListItem
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.ItemListBinding

class ListAdapter : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
    private val listItems: MutableList<ListItem> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listItem = listItems[position]
        holder.bind(listItem)
    }

    override fun getItemCount() = listItems.size

    class ListViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: ListItem) {
            binding.titleTextView.text = listItem.title
            binding.priorityTextView.text = listItem.priority.toString()
            binding.completedCheckBox.isChecked = listItem.isCompleted
        }
    }

    fun getItems() : List<ListItem> {
        return listItems
    }

    fun clear() {
        listItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(memoItems: List<ListItem>) {
        this.listItems.addAll(memoItems)
        notifyDataSetChanged()
    }
}