package com.ssafy.memo


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.memo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var memoDBHelper: MemoDBHelper
    private lateinit var selectedMemoItem: MemoItem
    private var memos = mutableListOf<MemoItem>()
    private var sortType = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initMemoDBHelper()
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
                builder.setTitle("모든 메모 삭제")
                builder.setMessage("정말 모든 메모를 삭제하시겠습니까? 이후에 삭제된 메모들을 복구되지 않습니다.")
                builder.setPositiveButton("예") { dialog, which ->
                    memoDBHelper.deleteAllMemos()
                    memoAdapter.clear()
                }
                builder.setNegativeButton("아니오") { dialog, which ->
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
                memoDBHelper.deleteMemo(selectedMemoItem.id)
                memoAdapter.clear()
                memoAdapter.addAll(memoDBHelper.selectAllMemos(sortType))
                val intent = Intent(MemoWidgetProvider.ACTION_MEMO_DELETED)
                sendBroadcast(intent)
                true
            }
            R.id.button_fix -> {
                selectedMemoItem.isFixed = !selectedMemoItem.isFixed
                memoDBHelper.updateMemo(selectedMemoItem)
                memoAdapter.clear()
                memoAdapter.addAll(memoDBHelper.selectAllMemos(sortType))
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
            val intent = Intent(this, MemoEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initSortMemu() {
        val items = listOf("최신 순", "사전 순", "생성 순")

        val adapter = ArrayAdapter(this, R.layout.item_menu_sort, items)
        binding.textviewSort.setAdapter(adapter)
        binding.textviewSort.setText(items[0], false)

        binding.textviewSort.setOnItemClickListener { _, _, position, _ ->
            when(position) {
                0 -> {
                    sortType = 1
                    memoAdapter.clear()
                    memoAdapter.addAll(memoDBHelper.selectAllMemos(1))
                }
                1 -> {
                    sortType = 2
                    memoAdapter.clear()
                    memoAdapter.addAll(memoDBHelper.selectAllMemos(2))
                }
                2 -> {
                    sortType = 3
                    memoAdapter.clear()
                    memoAdapter.addAll(memoDBHelper.selectAllMemos(3))
                }
            }

        }
    }

    private fun initAdapter() {
        memos = memoDBHelper.selectAllMemos(sortType)
        memoAdapter = MemoAdapter(memos, object : MemoAdapter.OnItemClickListener {
            override fun onItemClick(memoItem: MemoItem) {
                val intent = Intent(applicationContext, MemoEditActivity::class.java)
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

    private fun initMemoDBHelper() {
        memoDBHelper = MemoDBHelper(this)
    }

    override fun onResume() {
        super.onResume()
        if (::memoAdapter.isInitialized && ::memoDBHelper.isInitialized) {
            memoAdapter.clear()
            memoAdapter.addAll(memoDBHelper.selectAllMemos(sortType))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoDBHelper.close()
    }
}