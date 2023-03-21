package com.likewhile.meme.data.local

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.likewhile.meme.MemeApplication
import com.likewhile.meme.data.model.*
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider

class MemoDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)
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
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
                val memoType = MemoType.fromInt(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_TYPE)))

                val memoItem = when (memoType) {
                    MemoType.LIST -> ListMemoItem(id, title, deserializeListContent(cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST_CONTENT))), date, isFixed)
                    MemoType.DRAWING-> DrawingMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_HANDWRITTEN_DATA)), date, isFixed)
                    MemoType.TEXT -> TextMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)), date, isFixed)
                }
                memoItems.add(memoItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return memoItems
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
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
            val isFixed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FIXED)) == 1
            val memoType = MemoType.fromInt(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_TYPE)))

            memoItem = when (memoType) {
                MemoType.LIST -> ListMemoItem(id, title, deserializeListContent(cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST_CONTENT))), date, isFixed)
                MemoType.DRAWING -> DrawingMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_HANDWRITTEN_DATA)), date, isFixed)
                MemoType.TEXT -> TextMemoItem(id, title, cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)), date, isFixed)
            }
        }

        cursor.close()
        db.close()
        return memoItem
    }

    fun insertMemo(memoItem: MemoItem) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)
        values.put(COLUMN_DATE, memoItem.date)
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
            is DrawingMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.DRAWING.typeValue)
                values.put(COLUMN_HANDWRITTEN_DATA, serializeDrawingContent(memoItem.drawingPath))
            }
        }

        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateMemo(memoItem: MemoItem) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, memoItem.title)
        values.put(COLUMN_DATE, memoItem.date)
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
            is DrawingMemoItem -> {
                values.put(COLUMN_MEMO_TYPE, MemoType.DRAWING.typeValue)
                values.put(COLUMN_HANDWRITTEN_DATA, serializeDrawingContent(memoItem.drawingPath))
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

    companion object {
        const val DATABASE_NAME = "meme.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "meme"
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
            CREATE TABLE TABLE_NAME (
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