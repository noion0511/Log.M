package com.likewhile.meme.ui.view

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.likewhile.meme.R
import com.likewhile.meme.databinding.ActivityImageMemoEditBinding

class ImageMemoEditActivity : AppCompatActivity() {
    private val binding : ActivityImageMemoEditBinding by lazy {
        ActivityImageMemoEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imageAddButton.imageAddButton.setOnClickListener {
            binding.imageAddButton.root.visibility=View.GONE
            //binding.imageContent.imageView.setImageResource(R.drawable.testimage)
            binding.imageContent.root.visibility=View.VISIBLE
        }
    }
}