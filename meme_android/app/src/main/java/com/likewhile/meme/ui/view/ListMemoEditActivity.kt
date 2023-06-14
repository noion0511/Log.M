package com.likewhile.meme.ui.view

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.likewhile.meme.R
import com.likewhile.meme.data.model.ListItem
import com.likewhile.meme.data.model.ListMemoItem
import com.likewhile.meme.databinding.ActivityMemoListEditBinding
import com.likewhile.meme.ui.adapter.ListAdapter
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.ui.viewmodel.ListMemoViewModel
import com.likewhile.meme.util.ListItemTouchHelperCallback
import java.util.*

class ListMemoEditActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMemoListEditBinding.inflate(layoutInflater) }
    private val itemId by lazy { intent.getLongExtra(MemoWidgetProvider.EXTRA_MEMO_ID, -1) }
    private var insertedId: Long = 0L

    private lateinit var memoViewModel: ListMemoViewModel
    private lateinit var listAdapter: ListAdapter
    private lateinit var itemTouchHelperCallback: ListItemTouchHelperCallback

    private var isMenuVisible = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        memoViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ListMemoViewModel::class.java
            )

        initRecyclerView()
        initMemoData()
        initSave()
        initCancel()
        initToolbar()
    }

    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, MemoWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        // 위젯 업데이트를 요청합니다.
        val intent = Intent(this, MemoWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        sendBroadcast(intent)
    }


    private fun initRecyclerView(itemList: List<ListItem>? = null) {
        val initialList = if (itemList == null || itemList.isEmpty()) {
            val initialItem = ListItem(priority = 1, title = "")
            mutableListOf(initialItem)
        } else {
            itemList.toMutableList()
        }

        listAdapter = ListAdapter(initialList) {
            val newItem = ListItem(priority = listAdapter.itemCount, title = "")
            initialList.add(initialList.lastIndex + 1, newItem)
            listAdapter.notifyItemInserted(initialList.lastIndex + 1)
            binding.contentRecyclerview.scrollToPosition(initialList.lastIndex)
        }
        binding.contentRecyclerview.adapter = listAdapter
        binding.contentRecyclerview.layoutManager = LinearLayoutManager(this)

        itemTouchHelperCallback = ListItemTouchHelperCallback(listAdapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.contentRecyclerview)
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


    private fun initSave() {
        binding.bottomBtnEdit.buttonSave.setOnClickListener {
            val title = binding.title.editTextTitle.text.toString()
            val contentList = listAdapter.getItems()
            val isPinned = binding.bottomBtnEdit.checkBoxPinned.isChecked
            val isStarred = binding.bottomBtnEdit.checkBoxStarred.isChecked

            val listItems = contentList.filterIndexed { index, item ->
                listAdapter.getItemViewType(index) == ListAdapter.ITEM_TYPE_NORMAL
            }.map {
                ListItem(
                    priority = it.priority,
                    title = it.title,
                )
            }

            val memoItem = ListMemoItem(
                id = itemId,
                title = title,
                listItems = listItems,
                date = Date(),
                isPinned = isPinned,
                isStarred = isStarred
            )

            if (title.isBlank() || contentList.isEmpty())
                Toast.makeText(this, getString(R.string.toast_empty_fields), Toast.LENGTH_SHORT)
                    .show()
            else {
                if (itemId != -1L) {
                    memoViewModel.updateMemo(memoItem)
                    updateWidget()
                } else if (insertedId != 0L) {
                    memoViewModel.setItemId(insertedId)
                    memoItem.id = insertedId
                    memoViewModel.updateMemo(memoItem)
                    updateWidget()
                } else {
                    insertedId = memoViewModel.insertMemo(memoItem)
                }
                setReadMode()
                val focusedView = currentFocus
                focusedView?.clearFocus()

                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(focusedView?.windowToken, 0)

                Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initCancel() {
        binding.bottomBtnEdit.buttonCancel.setOnClickListener { finish() }
    }


    private fun initMemoData() {
        if (itemId != -1L) {
            memoViewModel.setItemId(itemId)
            setReadMode()
        } else {
            setEditMode()
        }

        memoViewModel.memo.observe(this) { memo ->
            if (memo != null) {
                binding.title.editTextTitle.setText(memo.title)
                binding.bottomBtnEdit.checkBoxPinned.isChecked = memo.isPinned
                binding.bottomBtnEdit.checkBoxStarred.isChecked = memo.isStarred
                listAdapter.clear()
                listAdapter.addAll(memo.listItems)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.button_edit_mode -> {
                setEditMode()
                return true
            }

            R.id.button_memo_delete -> {
                val deleteResult = memoViewModel.deleteMemo(itemId)
                if (deleteResult) {
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val editMenu = menu.findItem(R.id.button_edit_mode)
        val deleteMenu = menu.findItem(R.id.button_memo_delete)
        editMenu.isVisible = isMenuVisible
        deleteMenu.isVisible = isMenuVisible
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onDestroy() {
        super.onDestroy()
        memoViewModel.closeDB()
    }


    private fun setReadMode() {
        binding.title.editTextTitle.isEnabled = false
        binding.title.editTextTitle.alpha = 0.8f
        binding.title.editTextTitle.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.checkBoxPinned.isEnabled = false
        binding.bottomBtnEdit.checkBoxPinned.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.checkBoxStarred.isEnabled = false
        binding.bottomBtnEdit.checkBoxStarred.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.buttonSave.visibility = View.GONE
        binding.bottomBtnEdit.buttonCancel.visibility = View.GONE
        listAdapter.setItemsClickable(false)
        listAdapter.setItemEditable(false)
        itemTouchHelperCallback.setEnabled(false)

        // 메뉴를 무효화하여 onPrepareOptionsMenu()를 다시 호출
        isMenuVisible = true
        invalidateOptionsMenu()
    }


    private fun setEditMode() {
        binding.title.editTextTitle.isEnabled = true
        binding.bottomBtnEdit.checkBoxPinned.isEnabled = true
        binding.bottomBtnEdit.checkBoxStarred.isEnabled = true
        binding.bottomBtnEdit.buttonSave.visibility = View.VISIBLE
        binding.bottomBtnEdit.buttonCancel.visibility = View.VISIBLE
        listAdapter.setItemsClickable(true)
        listAdapter.setItemEditable(true)
        itemTouchHelperCallback.setEnabled(true)

        // 메뉴를 무효화하여 onPrepareOptionsMenu()를 다시 호출
        isMenuVisible = false
        invalidateOptionsMenu()
    }
}