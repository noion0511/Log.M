package com.ssafy.memo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ssafy.memo.databinding.ActivityMemoEditBinding
import com.ssafy.memo.util.Utils
import java.util.*

private const val TAG = "MemoEditActivity"
class MemoEditActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMemoEditBinding.inflate(layoutInflater) }
    private val itemId by lazy { intent.getLongExtra("itemId", -1) }
    private val appWidgetId by lazy { intent.getIntExtra("appWidgetId", -1) }
    private val memoDBHelper by lazy { MemoDBHelper(this) }

    lateinit var memo: MemoItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initMemoData()
        initMemoForm()
        initSave()
        initCancel()
    }

    private fun initMemoForm() {
        if (::memo.isInitialized) {
            binding.editTextTitle.setText(memo.title)
            binding.editTextContent.setText(memo.content)
            binding.checkBoxFix.isChecked = memo.isFixed

        }
    }

    private fun initCancel() {
        binding.buttonCancel.setOnClickListener { finish() }
    }

    private fun initSave() {
        binding.buttonSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val content = binding.editTextContent.text.toString()
            val time = Utils.formatDate(Date(), "yyyy-MM-dd HH:mm")
            val isFixed = binding.checkBoxFix.isChecked

            val memoItem = MemoItem(
                id = itemId,
                title = title,
                content = content,
                date = time,
                isFixed = isFixed,
            )

            if (title.isBlank() || content.isBlank())
                Toast.makeText(this, "제목과 상세내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                if (::memo.isInitialized) {
                    Log.d(TAG, "onDataSetChanged: initSave ${memoItem.id}")
                    Log.d(TAG, "onDataSetChanged: initSave ${appWidgetId}")
                    memoDBHelper.updateMemo(memoItem, appWidgetId)
                } else {
                    memoDBHelper.insertMemo(memoItem)
                }
                finish()
            }
        }
    }


    private fun initMemoData() {
        if (itemId != -1L && memoDBHelper.selectMemo(itemId) != null) {
            memo = memoDBHelper.selectMemo(itemId)!!
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoDBHelper.close()
    }
}