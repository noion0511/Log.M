package com.ssafy.memo


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.memo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var memoDBHelper: MemoDBHelper
    private lateinit var selectedMemoItem: MemoItem
    private var memos = mutableListOf<MemoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initMemoDBHelper()
        initAdapter()
        initCreateMemo()
        initContextMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_create -> {
                val intent = Intent(this, MemoEditActivity::class.java)
                startActivity(intent)
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
                memoAdapter.addAll(memoDBHelper.selectAllMemos())
                true
            }
            R.id.button_fix -> {
                selectedMemoItem.isFixed = !selectedMemoItem.isFixed
                memoDBHelper.updateMemo(selectedMemoItem)
                memoAdapter.clear()
                memoAdapter.addAll(memoDBHelper.selectAllMemos())
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    private fun initCreateMemo() {
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, MemoEditActivity::class.java)
            startActivity(intent)
        }
    }
    private fun initAdapter() {
        memos = memoDBHelper.selectAllMemos()
        memoAdapter = MemoAdapter(memos, object : MemoAdapter.OnItemClickListener {
            override fun onItemClick(memoItem: MemoItem) {
                val intent = Intent(applicationContext, MemoEditActivity::class.java)
                intent.putExtra("itemId", memoItem.id)
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
    private fun initContextMenu() {
        registerForContextMenu(binding.listViewMemo)
    }

    private fun initMemoDBHelper() {
        memoDBHelper = MemoDBHelper(this)
    }

    override fun onResume() {
        super.onResume()
        if(::memoAdapter.isInitialized && ::memoDBHelper.isInitialized) {
            memoAdapter.clear()
            memoAdapter.addAll(memoDBHelper.selectAllMemos())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoDBHelper.close()
    }
}