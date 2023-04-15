package com.likewhile.meme.data.local

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.likewhile.meme.MemeApplication
import com.likewhile.meme.data.model.*
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.util.DateFormatUtil

class MemoDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)

        val memoItem = TextMemoItem(title = "first Memo", content = "hello, world")
        insertMemo(memoItem, db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DELETE_TABLE_SQL)
        onCreate(db)
    }

    @SuppressLint("Range")
    fun selectAllMemos(sortOption: Int = 1): ArrayList<MemoItem> {
        val memoItems = ArrayList<MemoItem>()
        val selectQuery = when (sortOption) {
            1 -> "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_DATE DESC"
            2 -> "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_TITLE COLLATE NOCASE ASC"
            else -> "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_DATE ASC"
        }
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                val date = DateFormatUtil.stringToDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)))
                val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
                val memoType = MemoType.fromInt(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_TYPE)))

                val memoItem = when (memoType) {
                    MemoType.LIST -> ListMemoItem(id, title, deserializeListContent(cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST_CONTENT))), date, isFixed)
                    else -> TextMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)), date, isFixed)
                }
                memoItems.add(memoItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return memoItems
    }


    fun getMemosByMonth(year: Int, month: Int): MutableList<CalendarItem> {
        val memos = mutableListOf<CalendarItem>()
        val db = readableDatabase
        val monthStr = String.format("%02d", month)
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE strftime('%Y-%m', $COLUMN_DATE) = '$year-$monthStr'", null)

        while (cursor.moveToNext()) {
            val memoItem = createMemoItemFromCursor(cursor)
            val day = DateFormatUtil.getDayFromDate(DateFormatUtil.dateToString(memoItem.date))
            memos.add(CalendarItem(id = memoItem.id, day = day))
        }

        cursor.close()
        db.close()
        return memos
    }

    fun getMemosByDate(year: Int, month: Int, day: Int, sortOption: Int): List<MemoItem> {
        val memos = mutableListOf<MemoItem>()
        val db = readableDatabase
        val monthStr = String.format("%02d", month)
        val dayStr = String.format("%02d", day)

        val orderBy = when (sortOption) {
            1 -> "ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_DATE DESC"
            2 -> "ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_TITLE COLLATE NOCASE ASC"
            else -> "ORDER BY $COLUMN_IS_FIXED DESC, $COLUMN_DATE ASC"
        }

        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_DATE = '$year-$monthStr-$dayStr' $orderBy", null)

        while (cursor.moveToNext()) {
            memos.add(createMemoItemFromCursor(cursor))
        }

        cursor.close()
        db.close()
        return memos
    }

    @SuppressLint("Range")
    fun selectMemo(itemId: Long): MemoItem? {
        var memoItem: MemoItem? = null
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(itemId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
            val date = DateFormatUtil.stringToDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)))
            val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
            val memoType = MemoType.fromInt(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_TYPE)))

            memoItem = when (memoType) {
                MemoType.LIST -> ListMemoItem(id, title, deserializeListContent(cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST_CONTENT))), date, isFixed)
                else -> TextMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)), date, isFixed)
            }
        }

        cursor.close()
        db.close()
        return memoItem
    }

    fun insertMemo(memoItem: MemoItem, db: SQLiteDatabase? = null) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)
        values.put(COLUMN_DATE, DateFormatUtil.dateToString(memoItem.date))
        values.put(COLUMN_IS_FIXED, memoItem.isFixed)
        when (memoItem) {
            is TextMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.TEXT.typeValue)
                values.put(COLUMN_CONTENT, memoItem.content)
            }
            is ListMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.LIST.typeValue)
                values.put(COLUMN_LIST_CONTENT, serializeListContent(memoItem.listItems))
            }
        }

        val database = db ?: writableDatabase
        database.insert(TABLE_NAME, null, values)
        if (db == null) {
            database.close()
        }
    }

    fun updateMemo(memoItem: MemoItem) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)

        values.put(COLUMN_DATE, DateFormatUtil.dateToString(memoItem.date))
        values.put(COLUMN_IS_FIXED, memoItem.isFixed)
        when (memoItem) {
            is TextMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.TEXT.typeValue)
                values.put(COLUMN_CONTENT, memoItem.content)
            }
            is ListMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.LIST.typeValue)
                values.put(COLUMN_LIST_CONTENT, serializeListContent(memoItem.listItems))
            }
        }

        val db = writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(memoItem.id.toString()))
        db.close()
    }

    fun deleteMemo(memoItemId: Long) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(memoItemId.toString()))
        db.close()

        val intent = Intent(MemeApplication.instance, MemoWidgetProvider::class.java).apply {
            action = MemoWidgetProvider.ACTION_MEMO_DELETED
            putExtra(MemoWidgetProvider.EXTRA_MEMO_ID, memoItemId)
        }
        MemeApplication.instance.sendBroadcast(intent)
    }

    fun deleteAllMemos() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()

        val intent = Intent(MemeApplication.instance, MemoWidgetProvider::class.java).apply {
            action = MemoWidgetProvider.ACTION_MEMO_DELETED_ALL
        }
        MemeApplication.instance.sendBroadcast(intent)
    }


    @SuppressLint("Range")
    private fun createMemoItemFromCursor(cursor: Cursor): MemoItem {
        val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
        val date = DateFormatUtil.stringToDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)))
        val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
        val contentType = MemoType.fromInt(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_TYPE)))

        return when (contentType) {
            MemoType.TEXT -> {
                val content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT))
                TextMemoItem(id, title, content, date, isFixed)
            }
            MemoType.LIST -> {
                val listItems = deserializeListContent(cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST_CONTENT)))
                ListMemoItem(id, title, listItems, date, isFixed)
            }
            else -> throw IllegalArgumentException("Invalid memo type")
        }
    }

    companion object {
        const val DATABASE_NAME = "logm.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "logm"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_LIST_CONTENT = "list"
        const val COLUMN_HANDWRITTEN_DATA = "drawing"
        const val COLUMN_MEMO_TYPE = "type"
        const val COLUMN_DATE = "date"
        const val COLUMN_IS_FIXED = "fixed"

        const val DELETE_TABLE_SQL = "DROP TABLE if exists $TABLE_NAME"
        const val CREATE_TABLE_SQL = """
            CREATE TABLE $TABLE_NAME (
        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_TITLE TEXT,
        $COLUMN_CONTENT TEXT,
        $COLUMN_DATE TEXT,
        $COLUMN_IS_FIXED INTEGER,
        $COLUMN_MEMO_TYPE INTEGER,
        $COLUMN_LIST_CONTENT TEXT,
        $COLUMN_HANDWRITTEN_DATA BLOB
            );
        """
    }
}