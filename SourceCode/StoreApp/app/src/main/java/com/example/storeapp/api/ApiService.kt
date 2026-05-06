package com.example.storeapp.api

import com.example.storeapp.data.ProductsItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("products")
    fun getProducts(): Call<List<ProductsItem>>

    @POST("products")
    fun addProduct(@Body product: ProductsItem): Call<ProductsItem>

    @DELETE("products/{id}")
    fun deleteProduct(@Path("id") id: Int): Call<okhttp3.ResponseBody>

    @PUT("products/{id}")
    fun updateProduct(@Path("id") id: Int, @Body product: ProductsItem): Call<ProductsItem>
}