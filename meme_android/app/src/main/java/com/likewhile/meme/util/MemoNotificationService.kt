package com.likewhile.meme.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.likewhile.meme.R
import com.likewhile.meme.data.local.MemoDBHelper
import com.likewhile.meme.data.model.ListMemoItem
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.TextMemoItem
import com.likewhile.meme.ui.view.MainActivity

private const val TAG = "MemoNotificationService"
class MemoNotificationService : Service() {
    private val channelId = "starredMemoChannelId"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val memoList: List<MemoItem> = readImportantMemosFromDatabase()

        for (memo in memoList) {
            val notification = createNotification(memo)
            val notificationId = memo.id.hashCode()
            Log.d(TAG, "onStartCommand: ${memo.id} hash ${memo.id.hashCode()}")
            showNotification(notificationId, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Channel for Memo notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(memo: MemoItem): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent =
            PendingIntent.getService(this, memo.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder =  NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_app)
            .setContentTitle(memo.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        when (memo) {
            is TextMemoItem -> {
                notificationBuilder.setContentText(memo.content)
            }
            is ListMemoItem -> {
                val sortedListItems = memo.listItems.sortedBy { it.priority }
                val listText = sortedListItems.joinToString("  ") { "${it.priority}. ${it.title}" }
                notificationBuilder.setContentText(listText)
            }
        }

        return notificationBuilder.build()
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun cancelNotification(notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }


    private fun readImportantMemosFromDatabase(): List<MemoItem> {
        val dbHelper = MemoDBHelper(this)
        return dbHelper.selectStarredMemo()
    }
}