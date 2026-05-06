package com.example.storeapp.data

import java.io.Serializable

data class ProductsItem(
    val category: String,
    val description: String,
    val id: Int,
    val image: String,
    val price: Double,
    val title: String
) : Serializable