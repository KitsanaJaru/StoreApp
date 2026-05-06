package com.example.storeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkItem(
    @PrimaryKey val id: Int,
    var category: String,
    var description: String,
    var image: String,
    var price: Double,
    var title: String,
    val date: String,
    val latitude: Double,
    val longitude: Double
)