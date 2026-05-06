package com.example.storeapp.local

import androidx.room.*
import com.example.storeapp.data.BookmarkItem

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkItem)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkItem)

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarks(): List<BookmarkItem>

    @Query("SELECT EXISTS(SELECT * FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: Int): Boolean

    @Update
    suspend fun updateBookmark(bookmark: BookmarkItem)
}