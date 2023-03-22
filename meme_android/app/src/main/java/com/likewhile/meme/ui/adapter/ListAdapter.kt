package com.likewhile.meme.ui.adapter

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.likewhile.meme.data.model.ListItem
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.ItemListBinding
import java.util.*

private const val TAG = "ListAdapter"
class ListAdapter(private var listItems: MutableList<ListItem>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
    private var clickable: Boolean = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ListViewHolder(binding)

        viewHolder.binding.titleTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val position = viewHolder.adapterPosition
                if (s != null && position != RecyclerView.NO_POSITION) {
                    updateItem(position, s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        return viewHolder
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listItem = listItems[position]
        holder.binding.titleTextView.isEnabled = clickable
        holder.binding.titleTextView.setTextColor(Color.BLACK)
        holder.bind(listItem)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(listItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
    override fun getItemCount() = listItems.size

    inner class ListViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listItem: ListItem) {
            binding.listItem = listItem
        }
    }

    fun getItems() : List<ListItem> {
        return listItems
    }


    fun addItem(listItem : ListItem) {
        listItems.add(listItem)
        notifyItemInserted(listItems.size - 1)
    }

    fun updateItem(position: Int, title: String) {
        listItems[position].title = title
    }

    fun setItemsClickable(clickable: Boolean) {
        this.clickable = clickable
        notifyDataSetChanged()
    }
    fun clear() {
        listItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(listItems: List<ListItem>) {
        this.listItems.addAll(listItems)
        notifyDataSetChanged()
    }
}