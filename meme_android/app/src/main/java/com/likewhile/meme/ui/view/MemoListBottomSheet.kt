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
import com.likewhile.meme.ui.viewmodel.MainViewModel
import com.likewhile.meme.util.DateFormatUtil

class MemoListBottomSheet(private val memoList: List<MemoItem>) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMemoBinding

    private lateinit var memoAdapter: MemoAdapter

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
                    dismiss()
                }
            },
            object : MemoAdapter.OnItemLongClickListener {
                override fun onItemLongClick(memoItem: MemoItem): Boolean {
                    return true
                }
            })
        binding.recyclerViewMemos.adapter = memoAdapter
        memoAdapter.setViewType(MemoAdapter.TYPE_ITEM_DETAIL)
    }
}
