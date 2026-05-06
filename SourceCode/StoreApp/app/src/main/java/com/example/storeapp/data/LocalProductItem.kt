package com.example.storeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_products")
data class LocalProductItem(
    @PrimaryKey val id: Int,
    val category: String,
    val description: String,
    val image: String,
    val price: Double,
    val title: String
)