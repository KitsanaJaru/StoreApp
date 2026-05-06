package com.example.storeapp.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.storeapp.data.BookmarkItem
import com.example.storeapp.data.LocalProductItem

@Database(entities = [BookmarkItem::class, LocalProductItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun localProductDao(): LocalProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "store_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}