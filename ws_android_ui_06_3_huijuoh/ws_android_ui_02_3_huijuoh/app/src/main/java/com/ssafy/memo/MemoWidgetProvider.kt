package com.ssafy.memo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

private const val TAG = "MemoWidgetProvider"

class MemoWidgetProvider : AppWidgetProvider() {

    companion object {
        const val EXTRA_ITEM_POSITION = "com.ssafy.memo.EXTRA_ITEM_POSITION"
        const val EXTRA_SELECTED_WIDGET_ID = "com.ssafy.memo.EXTRA_SELECTED_WIDGET_ID"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("MemoWidgetProvider", "onUpdate called with appWidgetIds: ${appWidgetIds.contentToString()}")

//        for (appWidgetId in appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId)
//        }
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_memo)
            val extras = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val memoItemId = extras.getLong(EXTRA_ITEM_POSITION, -1L)
            val memoItem = MemoDBHelper(context).selectMemoAt(memoItemId)


            // PendingIntent를 사용하여 클릭 이벤트를 처리할 인텐트를 설정합니다.
            val clickIntent = Intent(context, MemoWidgetProvider::class.java).apply {
                action = "your_click_action"
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            remoteViews.setTextViewText(R.id.textViewTitle, memoItem?.title ?: "")
            remoteViews.setTextViewText(R.id.textViewContent, memoItem?.content ?: "")
            remoteViews.setTextViewText(R.id.textViewDate, memoItem?.date ?: "")

            // 원격 뷰에 클릭 이벤트를 설정합니다. 여기에서 R.id.your_widget_button은 클릭 이벤트를 처리할 위젯의 뷰 ID입니다.
            remoteViews.setOnClickPendingIntent(R.id.widget_memo, pendingIntent)

            // 위젯 업데이트
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

//
//
//            val extras = appWidgetManager.getAppWidgetOptions(appWidgetId)
//            val memoItemId = extras.getLong(EXTRA_ITEM_POSITION, -1L)
//
//            val intent = Intent(context, MemoEditActivity::class.java)
//            intent.putExtra("itemId", memoItemId)
//            intent.putExtra("appWidgetId", appWidgetId)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//            views.setOnClickPendingIntent(R.id.widget_memo, pendingIntent)
//
//            updateAppWidget(context, appWidgetManager, appWidgetId, memoItemId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val memoItemId = intent.getLongExtra(EXTRA_ITEM_POSITION, -1L)
            val selectedWidgetId = intent.getIntExtra(EXTRA_SELECTED_WIDGET_ID, -1)

            Log.d(TAG, "onDataSetChanged: onReceive ${memoItemId}")
            Log.d(TAG, "onDataSetChanged: onReceive ${selectedWidgetId}")

            updateAppWidget(context, appWidgetManager, selectedWidgetId, memoItemId)
        }

        Log.d(TAG, "onReceive: click~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        memoItemId: Long
    ) {
        Log.d(TAG, "onDataSetChanged: updateAppWidget ${memoItemId}")
        Log.d(TAG, "onDataSetChanged: updateAppWidget ${appWidgetId}")

        val memoItem = if (memoItemId != -1L) {
            MemoDBHelper(context).selectMemoAt(memoItemId)
        } else {
            null
        }

        val views = RemoteViews(context.packageName, R.layout.widget_memo)

        views.setTextViewText(R.id.textViewTitle, memoItem?.title ?: "")
        views.setTextViewText(R.id.textViewContent, memoItem?.content ?: "")
        views.setTextViewText(R.id.textViewDate, memoItem?.date ?: "")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
