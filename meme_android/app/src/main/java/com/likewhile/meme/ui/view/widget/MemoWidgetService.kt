package com.likewhile.meme.ui.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.likewhile.meme.ui.view.widget.MemoWidgetFactory

class MemoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return MemoWidgetFactory(applicationContext, intent)
    }
}