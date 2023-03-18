package com.ssafy.memo

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ssafy.memo.util.Utils
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MemoDBHelper"
class MemoDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DELETE_TABLE_SQL)
        onCreate(db)
    }

    @SuppressLint("Range")
    fun selectAllMemos(): ArrayList<MemoItem> {
        val memoItems = ArrayList<MemoItem>()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_DATE"
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT))
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1

                val memoItem = MemoItem(id, title, content, date, isFixed)
                memoItems.add(memoItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return memoItems
    }

    @SuppressLint("Range")
    fun selectMemo(itemId : Long): MemoItem? {
        var memoItem : MemoItem? = null
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(itemId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT))
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
            val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1

            memoItem = MemoItem(id, title, content, date, isFixed)
        }

        cursor.close()
        db.close()
        return memoItem
    }

    @SuppressLint("Range")
    fun selectMemoAt(memoItemId: Long): MemoItem? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID  =  ?",
            arrayOf(memoItemId.toString())
        )
        var memoItem: MemoItem? = null
        if (cursor.moveToNext()) {
            memoItem = MemoItem(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
            )
        }

        cursor.close()
        db.close()

        return memoItem
    }


    @SuppressLint("Range")
    fun selectLatestMemo(): MemoItem? {
        var memoItem: MemoItem? = null
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC LIMIT 1", null)
        if (cursor.moveToNext()) {
            memoItem = MemoItem(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
            )
        }

        cursor.close()
        db.close()


        val context = MemeApplication.instance.applicationContext
        val intent = Intent(context, MemoWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, MemoWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)

        return memoItem
    }
    fun insertMemo(memoItem: MemoItem) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)
        values.put(COLUMN_CONTENT, memoItem.content)
        values.put(COLUMN_DATE, memoItem.date)
        values.put(COLUMN_IS_FIXED, memoItem.isFixed)

        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }


    fun insertWidgetMemo() : MemoItem {
        val values = ContentValues()
        values.put(COLUMN_TITLE, "title")
        values.put(COLUMN_CONTENT, "content")
        values.put(COLUMN_DATE, Utils.formatDate(Date(), "yyyy-MM-dd HH:mm"))
        values.put(COLUMN_IS_FIXED, false)

        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()

        return selectLatestMemo()!!
    }

    fun updateMemo(memoItem: MemoItem, selectedWidgetId : Int = -1) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)
        values.put(COLUMN_CONTENT, memoItem.content)
        values.put(COLUMN_DATE, memoItem.date)
        values.put(COLUMN_IS_FIXED, memoItem.isFixed)

        val db = writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(memoItem.id.toString()))
        db.close()

        Log.d(TAG, "onDataSetChanged: updateMemo ${memoItem.id}")
        Log.d(TAG, "onDataSetChanged: updateMemo ${selectedWidgetId}")

        if(selectedWidgetId != -1) {
            val context = MemeApplication.instance.applicationContext
            val intent = Intent(context, MemoWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(MemoWidgetProvider.EXTRA_ITEM_POSITION, memoItem.id)
            intent.putExtra(MemoWidgetProvider.EXTRA_SELECTED_WIDGET_ID, selectedWidgetId)
            context.sendBroadcast(intent)
        }


//        val context = MemeApplication.instance.applicationContext
//        val appWidgetManager = AppWidgetManager.getInstance(context)
//        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, MemoWidgetProvider::class.java))
//        for (appWidgetId in appWidgetIds) {
//            val extras = appWidgetManager.getAppWidgetOptions(appWidgetId)
//            val memoItemId = extras.getLong(MemoWidgetProvider.EXTRA_ITEM_POSITION, -1L)
//            Log.d(TAG, "updateMemo: $memoItemId, ${memoItem.id}, $appWidgetId, $selectedWidgetId")
//            if (memoItemId != -1L && memoItemId == memoItem.id) {
//                MemoWidgetProvider().updateAppWidget(context, appWidgetManager, appWidgetId, memoItem.id)
//            }
//        }
    }

    fun deleteMemo(memoItemId: Long) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(memoItemId.toString()))
        db.close()
    }

    companion object {
        const val DATABASE_NAME = "memo.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "memo"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_DATE = "date"
        const val COLUMN_IS_FIXED = "fixed"

        const val DELETE_TABLE_SQL = "DROP TABLE if exists $TABLE_NAME"
        const val CREATE_TABLE_SQL = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_IS_FIXED INTEGER
            );
        """
    }
}