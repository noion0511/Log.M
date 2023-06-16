package com.likewhile.meme.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likewhile.meme.R
import com.likewhile.meme.data.model.ImageItem
import com.likewhile.meme.data.model.ImageType
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.MemoImageBinding

class ImageAdapter(
    private var imageItems:MutableList<ImageItem>,
    private val context:Context,
    private val itemClickListener:ClickListener,
    private var editMode: MemoMode
    ) : RecyclerView.Adapter<ImageAdapter.ViewHolder>(){

    inner class ViewHolder(var binding:MemoImageBinding):RecyclerView.ViewHolder(binding.root) {
        fun setData(data:ImageItem){

            if(data.type==ImageType.BITMAP){
                binding.imageView.setImageBitmap(data.bitmap)
            }else{
                Glide
                    .with(context)
                    .load(data.uri)
                    .error(R.drawable.baseline_hide_image_24)
                    .fitCenter()
                    .into(binding.imageView)
            }

            if(editMode==MemoMode.EDIT){
                binding.imageDeleteButton.visibility = View.VISIBLE
            }else{
                binding.imageDeleteButton.visibility = View.GONE
            }

            binding.imageDeleteButton.setOnClickListener {
                itemClickListener.deleteButtonClicked(data)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=MemoImageBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(imageItems[position])
    }

    fun setEditMode(mode : MemoMode){
        this.editMode = mode
    }

    interface ClickListener{
        fun deleteButtonClicked(data:ImageItem)
    }
}

enum class MemoMode (val typeValue : Int){
    READ(0),
    EDIT(1);
}
