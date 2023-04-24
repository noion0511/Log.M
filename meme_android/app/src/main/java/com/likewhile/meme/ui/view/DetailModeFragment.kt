package com.likewhile.meme.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.likewhile.meme.R
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.SortTypeChangeEvent
import com.likewhile.meme.databinding.FragmentDetailModeBinding
import com.likewhile.meme.ui.adapter.MemoAdapter
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.ui.viewmodel.MainViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DetailModeFragment : Fragment() {

    private lateinit var binding: FragmentDetailModeBinding
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var memoAdapter: MemoAdapter
    private lateinit var selectedMemoItem: MemoItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailModeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initContextMenu()
        initAdapter()

        return binding.root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_delete -> {
                viewModel.deleteMemo(selectedMemoItem.id)
                val intent = Intent(MemoWidgetProvider.ACTION_MEMO_DELETED)
                requireActivity().sendBroadcast(intent)
                true
            }
            R.id.button_fix -> {
                selectedMemoItem.isFixed = !selectedMemoItem.isFixed
                viewModel.updateMemo(selectedMemoItem)
                memoAdapter.notifyDataSetChanged()
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    private fun initAdapter() {
        viewModel.memos.observe(viewLifecycleOwner) { memos ->
            memos?.let {
                memoAdapter.clear()
                memoAdapter.addAll(it)
            }
        }

        memoAdapter = MemoAdapter(viewModel.memos.value ?: mutableListOf(), object : MemoAdapter.OnItemClickListener {
            override fun onItemClick(memoItem: MemoItem) {
                val targetActivity = when (memoItem.contentType) {
                    "LIST" -> ListMemoEditActivity::class.java
                    else -> MemoEditActivity::class.java
                }
                val intent = Intent(requireActivity(), targetActivity)
                intent.putExtra(MemoWidgetProvider.EXTRA_MEMO_ID, memoItem.id)
                startActivity(intent)
            }
        },
            object : MemoAdapter.OnItemLongClickListener {
                override fun onItemLongClick(memoItem: MemoItem): Boolean {
                    selectedMemoItem = memoItem

                    binding.recyclerViewDetail.setOnCreateContextMenuListener { menu, v, menuInfo ->
                        requireActivity().menuInflater.inflate(R.menu.menu_long_click, menu)
                        menu.getItem(1).title = if (selectedMemoItem.isFixed) getString(R.string.unfixed) else getString(R.string.fixed)
                    }

                    requireActivity().openContextMenu(binding.recyclerViewDetail)
                    return true
                }
            }
        )
        binding.recyclerViewDetail.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerViewDetail.adapter = memoAdapter
        memoAdapter.setViewType(MemoAdapter.TYPE_ITEM_DETAIL)
    }

    private fun initContextMenu() {
        registerForContextMenu(binding.recyclerViewDetail)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangedEvent(event: SortTypeChangeEvent) {
        viewModel.setSortType(event.type)
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshMemos()
    }


    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeDB()
    }

    companion object {
        fun newInstance(): DetailModeFragment {
            return DetailModeFragment()
        }
    }
}
