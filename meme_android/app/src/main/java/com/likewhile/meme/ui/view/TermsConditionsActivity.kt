package com.likewhile.meme.ui.view

import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import com.likewhile.meme.R
import com.likewhile.meme.databinding.ActivityMainBinding
import com.likewhile.meme.databinding.ActivityTermsConditionsBinding

class TermsConditionsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTermsConditionsBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initToolbar()
    }

    private fun initToolbar() {
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Gravity.START
        )
        setSupportActionBar(binding.toolbar.toolbar)
        val blackColor = ContextCompat.getColor(this, android.R.color.black)
        binding.toolbar.logo.setColorFilter(blackColor, PorterDuff.Mode.SRC_IN)
        binding.toolbar.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        binding.toolbar.toolbar.layoutParams = params
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}