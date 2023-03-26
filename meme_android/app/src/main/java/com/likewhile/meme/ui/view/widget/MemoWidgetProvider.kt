package com.likewhile.meme.ui.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.likewhile.meme.R
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.TextMemoItem
import com.likewhile.meme.ui.view.MemoEditActivity
import com.likewhile.meme.util.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MemoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 위젯이 여러 개인 경우를 처리하기 위해 모든 위젯 ID에 대해 업데이트를 수행합니다.
        for (appWidgetId in appWidgetIds) {
            val memoId = getMemoIdForWidget(context, appWidgetId)

            // 위젯 인스턴스와 관련된 메모가 없는 경우 새로운 메모를 생성합니다.
            if (memoId == null) {
                val memo = createNewMemo(context)
                saveMemoIdForWidget(context, appWidgetId, memo.id)
            }

            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_MEMO_DELETED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    MemoWidgetProvider::class.java
                )
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }


    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            removeMemoIdForWidget(context, appWidgetId)
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // 메모 데이터를 로드하는 코드를 작성해야 합니다.
        GlobalScope.launch {
            val memo = if (appWidgetId != -1) loadMemo(context, appWidgetId) else null

            // 위젯 레이아웃을 업데이트합니다.
            val views = RemoteViews(context.packageName, R.layout.widget_memo)

            if (memo != null) {
                views.setTextViewText(R.id.textViewTitle, memo.title)
                views.setTextViewText(R.id.textViewContent, (memo as TextMemoItem).content)
                views.setTextViewText(R.id.textViewDate, DateFormatUtil.formatDate(memo.date))
                setWidgetClickEvent(context, views, appWidgetId, memo.id)
            } else {
                views.setTextViewText(R.id.textViewTitle, "메모가 없음")
                views.setTextViewText(R.id.textViewContent, "연결된 메모가 삭제되었습니다.")
                setWidgetClickEvent(context, views, appWidgetId, -1L)
            }
            // 위젯을 업데이트합니다.
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun loadMemo(context: Context, widgetId: Int): MemoItem? {
        val memoId = loadMemoIdForWidget(context, widgetId)
        if (memoId == -1L) return null

        val dbHelper = MemoDBHelper(context)
        return dbHelper.selectMemo(memoId)
    }

    private fun createNewMemo(context: Context): MemoItem {
        val dbHelper = MemoDBHelper(context)
        val memo = TextMemoItem(
            0L, // ID는 데이터베이스에서 자동으로 생성됩니다.
            "new memo", // 제목
            "", // 내용
            Date(), // 날짜
            false // 고정 여부
        )

        dbHelper.insertMemo(memo)
        val memoItems = dbHelper.selectAllMemos(3)
        return memoItems.last() // 가장 최근에 저장된 메모를 반환합니다.
    }

    private fun setWidgetClickEvent(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        memoId: Long
    ) {
        if (memoId == -1L) {
            views.setOnClickPendingIntent(R.id.widget_memo, null)
            return
        }
        // 인텐트를 생성하여 EditMemoActivity를 시작하도록 설정합니다.
        val intent = Intent(context, MemoEditActivity::class.java)

        // 위젯 인스턴스에 대한 정보를 전달하기 위해 인텐트에 extra 데이터를 추가합니다.
        intent.putExtra(EXTRA_WIDGET_ID, appWidgetId)
        intent.putExtra(EXTRA_MEMO_ID, memoId)

        // PendingIntent를 사용하여 인텐트를 설정합니다.
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 위젯 뷰의 클릭 리스너를 설정합니다.
        views.setOnClickPendingIntent(R.id.widget_memo, pendingIntent)

    }

    companion object {
        const val EXTRA_MEMO_ID = "memoId"
        const val EXTRA_WIDGET_ID = "widgetId"
        const val ACTION_MEMO_DELETED = "MEMO_DELETED"
        const val ACTION_MEMO_DELETED_ALL = "ACTION_MEMO_DELETED_ALL"
    }
}
