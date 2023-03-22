package com.likewhile.meme.ui.view


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.likewhile.meme.*
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.MemoType
import com.likewhile.meme.databinding.ActivityMainBinding
import com.likewhile.meme.ui.adapter.MemoAdapter
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var selectedMemoItem: MemoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(MainViewModel::class.java)

        initButtonSetViewType()
        initAdapter()
        initCreateBtn()
        initContextMenu()
        initSortMemu()
        initToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_delete_all -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.delete_all_memos_title))
                builder.setMessage(getString(R.string.delete_all_memos_message))
                builder.setPositiveButton(getString(R.string.delete_all_memos_confirm)) { dialog, which ->
                    mainViewModel.deleteAllMemos()
                    memoAdapter.clear()
                }
                builder.setNegativeButton(getString(R.string.delete_all_memos_cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_delete -> {
                mainViewModel.deleteMemo(selectedMemoItem.id)
                val intent = Intent(MemoWidgetProvider.ACTION_MEMO_DELETED)
                sendBroadcast(intent)
                true
            }
            R.id.button_fix -> {
                selectedMemoItem.isFixed = !selectedMemoItem.isFixed
                mainViewModel.updateMemo(selectedMemoItem)
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.include.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initCreateBtn() {
        binding.floatingActionButton.setOnClickListener {
            val navigationOptions = arrayOf(
                getString(R.string.navigation_option_memo_edit),
                getString(R.string.navigation_option_list_memo_edit)
            )

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.choose_a_screen))
                .setItems(navigationOptions) { _, which ->
                    val intent = when (navigationOptions[which]) {
                        getString(R.string.navigation_option_memo_edit) -> Intent(this, MemoEditActivity::class.java)
                        getString(R.string.navigation_option_list_memo_edit) -> Intent(this, ListMemoEditActivity::class.java)
                        else -> null
                    }
                    intent?.let { startActivity(it) }
                }
            builder.create().show()
        }
    }

    private fun initSortMemu() {
        val items = listOf(
            getString(R.string.sort_by_latest),
            getString(R.string.sort_alphabetically),
            getString(R.string.sort_by_creation_date)
        )

        val adapter = ArrayAdapter(this, R.layout.item_menu_sort, items)
        binding.textviewSort.setAdapter(adapter)
        binding.textviewSort.setText(items[0], false)

        binding.textviewSort.setOnItemClickListener { _, _, position, _ ->
            when(position) {
                0 -> {
                    mainViewModel.setSortType(1)
                }
                1 -> {
                    mainViewModel.setSortType(2)
                }
                2 -> {
                    mainViewModel.setSortType(3)
                }
            }

        }
    }

    private fun initAdapter() {
        mainViewModel.memos.observe(this) { memos ->
            memos?.let {
                memoAdapter.clear()
                memoAdapter.addAll(it)
            }
        }

        memoAdapter = MemoAdapter(mainViewModel.memos.value ?: mutableListOf(), object : MemoAdapter.OnItemClickListener {
            override fun onItemClick(memoItem: MemoItem) {
                val targetActivity = when (memoItem.contentType) {
                    "LIST" -> ListMemoEditActivity::class.java
                    "DRAWING" -> MemoEditActivity::class.java
                    else -> MemoEditActivity::class.java
                }
                val intent = Intent(applicationContext, targetActivity)
                intent.putExtra(MemoWidgetProvider.EXTRA_MEMO_ID, memoItem.id)
                startActivity(intent)
            }
        },
            object : MemoAdapter.OnItemLongClickListener {
                override fun onItemLongClick(memoItem: MemoItem): Boolean {
                    selectedMemoItem = memoItem

                    binding.listViewMemo.setOnCreateContextMenuListener { menu, v, menuInfo ->
                        menuInflater.inflate(R.menu.menu_long_click, menu)
                        menu.getItem(1).title = if (selectedMemoItem.isFixed) "고정 해제" else "고정"
                    }

                    openContextMenu(binding.listViewMemo)
                    return true
                }
            }
        )
        binding.listViewMemo.layoutManager = LinearLayoutManager(this)
        binding.listViewMemo.adapter = memoAdapter
    }

    private fun initButtonSetViewType() {
        binding.btnSimpleView.isSelected = true
        binding.btnSimpleView.setOnClickListener {
            binding.listViewMemo.layoutManager = LinearLayoutManager(this)
            memoAdapter.setViewType(MemoAdapter.TYPE_ITEM_SIMPLE)
        }
        binding.btnDetailView.setOnClickListener {
            binding.listViewMemo.layoutManager = GridLayoutManager(this, 2)
            memoAdapter.setViewType(MemoAdapter.TYPE_ITEM_DETAIL)
        }
    }

    private fun initContextMenu() {
        registerForContextMenu(binding.listViewMemo)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshMemos()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.closeDB()
    }
}