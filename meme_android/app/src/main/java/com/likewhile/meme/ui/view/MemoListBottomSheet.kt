package com.likewhile.meme.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.likewhile.meme.R
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.databinding.BottomSheetMemoBinding
import com.likewhile.meme.ui.adapter.MemoAdapter
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.ui.viewmodel.DetailModeViewModel
import com.likewhile.meme.util.DateFormatUtil

class MemoListBottomSheet(private val memoList: List<MemoItem>) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMemoBinding
    private val viewModel: DetailModeViewModel by viewModels()

    private lateinit var memoAdapter: MemoAdapter
    private lateinit var selectedMemoItem: MemoItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initTitle()
    }


    private fun initTitle() {
        binding.title.text = DateFormatUtil.formatDate(memoList[0].date)
    }


    private fun initAdapter() {
        memoAdapter = MemoAdapter(
            memoList as MutableList<MemoItem>, object : MemoAdapter.OnItemClickListener {
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

                    binding.recyclerViewMemos.setOnCreateContextMenuListener { menu, v, menuInfo ->
                        requireActivity().menuInflater.inflate(R.menu.menu_long_click, menu)
                        menu.getItem(1).title = if (selectedMemoItem.isFixed) getString(R.string.unfixed) else getString(
                            R.string.fixed)
                    }

                    requireActivity().openContextMenu(binding.recyclerViewMemos)
                    return true
                }
            })
        binding.recyclerViewMemos.adapter = memoAdapter
        memoAdapter.setViewType(MemoAdapter.TYPE_ITEM_DETAIL)
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
}
