package com.likewhile.meme.ui.view.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.likewhile.meme.R
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.MemoItem

class MemoWidgetFactory(private val context: Context, intent: Intent?) : RemoteViewsService.RemoteViewsFactory {
    private val memoDBHelper = MemoDBHelper(context)
    private var memoItems: List<MemoItem> = ArrayList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        memoItems = memoDBHelper.selectAllMemos()
    }

    override fun onDestroy() {}

    override fun getCount(): Int {
        return memoItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val memoItem = memoItems[position]

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_memo)
        remoteViews.setTextViewText(R.id.textViewTitle, memoItem.title)
        remoteViews.setTextViewText(R.id.textViewContent, memoItem.content)
        remoteViews.setTextViewText(R.id.textViewDate, memoItem.date)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return memoItems[position].id
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
