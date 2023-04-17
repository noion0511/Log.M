package com.likewhile.meme.ui.adapter

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.likewhile.meme.data.model.ListItem
import com.likewhile.meme.databinding.ItemListAddBinding
import com.likewhile.meme.databinding.ItemListBinding
import java.util.*

private const val TAG = "ListAdapter"

class ListAdapter(
    private var listItems: MutableList<ListItem>,
    private val onAddButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_NORMAL = 0
        const val ITEM_TYPE_ADD_BUTTON = 1
    }

    private var clickable: Boolean = true
    private var editable: Boolean = true
    lateinit var recyclerView: RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_ADD_BUTTON -> {
                val binding = ItemListAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val viewHolder = ListAddViewHolder(binding)
                viewHolder
            }
            else -> {
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
                viewHolder
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_TYPE_ADD_BUTTON -> {
                val addButtonHolder = holder as ListAddViewHolder
                if (position == itemCount - 1 && editable) {
                    addButtonHolder.binding.addItemButton.visibility = View.VISIBLE
                    addButtonHolder.binding.addItemButton.setOnClickListener {
                        onAddButtonClick()
                        addButtonHolder.binding.addItemButton.visibility = View.GONE
                    }
                } else {
                    addButtonHolder.binding.addItemButton.visibility = View.GONE
                }

            }
            ITEM_TYPE_NORMAL -> {
                val normalViewHolder = holder as ListViewHolder
                val listItem = listItems[position]
                listItem.priority = position + 1
                normalViewHolder.binding.titleTextView.isEnabled = clickable
                normalViewHolder.binding.titleTextView.setTextColor(Color.BLACK)
                normalViewHolder.bind(listItem)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            ITEM_TYPE_ADD_BUTTON
        } else {
            ITEM_TYPE_NORMAL
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(listItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        notifyItemChanged(fromPosition)
        notifyItemChanged(toPosition)
    }

    override fun getItemCount() = listItems.size

    inner class ListViewHolder(val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItem: ListItem) {
            binding.listItem = listItem
        }
    }


    inner class ListAddViewHolder(val binding: ItemListAddBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.addItemButton.setOnClickListener {
                onAddButtonClick()
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun getItems(): List<ListItem> {
        return listItems
    }

    fun updateItem(position: Int, title: String) {
        listItems[position].title = title
    }

    fun removeItem(position: Int) {
        listItems.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setItemsClickable(clickable: Boolean) {
        this.clickable = clickable
        notifyDataSetChanged()
    }

    fun setItemEditable(editable: Boolean) {
        this.editable = editable
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