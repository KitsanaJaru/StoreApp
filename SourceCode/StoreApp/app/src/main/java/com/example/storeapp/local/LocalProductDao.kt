package com.example.storeapp.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storeapp.data.LocalProductItem

@Dao
interface LocalProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalProduct(product: LocalProductItem)

    @Query("SELECT * FROM local_products")
    suspend fun getAllLocalProducts(): List<LocalProductItem>

    @Delete
    suspend fun deleteLocalProduct(product: LocalProductItem)
}