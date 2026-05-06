package com.example.storeapp.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeapp.R
import com.example.storeapp.adapter.ProductAdapter
import com.example.storeapp.api.RetrofitClient
import com.example.storeapp.data.LocalProductItem
import com.example.storeapp.data.ProductsItem
import com.example.storeapp.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private lateinit var homeAdapter: ProductAdapter
    private lateinit var rvProducts: RecyclerView
    private lateinit var progressBar: ProgressBar

    companion object {
        var savedProductList: MutableList<ProductsItem>? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rvProducts = view.findViewById(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(requireContext())

        if (savedProductList == null) {
            loadProductsFromAPI()
        } else {
            setupAdapter(savedProductList!!)
        }

        val fabAdd =
            view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                R.id.fabAddProduct
            )
        fabAdd.setOnClickListener {
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.product_edit, null)
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
            val dialog = builder.create()

            val tvTitle = dialogView.findViewById<TextView>(R.id.tvEditTitle)
            val etImageUrl = dialogView.findViewById<EditText>(R.id.etEditImageUrl)
            val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
            val etCategory = dialogView.findViewById<EditText>(R.id.etEditCategory)
            val etPrice = dialogView.findViewById<EditText>(R.id.etEditPrice)
            val etDescription = dialogView.findViewById<EditText>(R.id.etEditDescription)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelEdit)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEdit)

            tvTitle.text = "Add New Product"

            btnCancel.setOnClickListener { dialog.dismiss() }

            btnSave.setOnClickListener {
                val newCategory = etCategory.text.toString()
                val newDesc = etDescription.text.toString()
                val newImage = etImageUrl.text.toString().trim()
                val newPrice = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val newTitle = etTitle.text.toString().trim()

                if (newTitle.isEmpty() || newPrice == 0.0) {
                    Toast.makeText(
                        requireContext(), "Please fill in title and price!", Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val uniqueId = (System.currentTimeMillis() % 1000000).toInt()

                val newProduct = ProductsItem(
                    id = uniqueId,
                    title = newTitle,
                    price = newPrice,
                    description = newDesc,
                    category = newCategory,
                    image = newImage
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.localProductDao().insertLocalProduct(
                        LocalProductItem(
                            uniqueId, newCategory, newDesc, newImage, newPrice, newTitle
                        )
                    )
                }

                RetrofitClient.instance.addProduct(newProduct)
                    .enqueue(object : Callback<ProductsItem> {
                        override fun onResponse(
                            call: Call<ProductsItem>, response: Response<ProductsItem>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    requireContext(), "Added Successfully!", Toast.LENGTH_SHORT
                                ).show()
                                homeAdapter.addProduct(newProduct)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(requireContext())
                                    val localItem = LocalProductItem(
                                        id = newProduct.id,
                                        title = newProduct.title,
                                        price = newProduct.price,
                                        description = newProduct.description,
                                        category = newProduct.category,
                                        image = newProduct.image
                                    )
                                    db.localProductDao().insertLocalProduct(localItem)
                                }

                                dialog.dismiss()
                            }
                        }

                        override fun onFailure(call: Call<ProductsItem>, t: Throwable) {
                            Toast.makeText(
                                requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            dialog.show()
        }
    }

    private fun loadProductsFromAPI() {
        progressBar.visibility = View.VISIBLE
        rvProducts.visibility = View.GONE

        RetrofitClient.instance.getProducts().enqueue(object : Callback<List<ProductsItem>> {
            override fun onResponse(
                call: Call<List<ProductsItem>>, response: Response<List<ProductsItem>>
            ) {
                progressBar.visibility = View.GONE
                rvProducts.visibility = View.VISIBLE

                if (response.isSuccessful && response.body() != null) {

                    val apiProducts = response.body()!!

                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(requireContext())
                        val localProducts = db.localProductDao().getAllLocalProducts()

                        val convertedLocalList = localProducts.map {
                            ProductsItem(
                                id = it.id,
                                title = it.title,
                                price = it.price,
                                description = it.description,
                                category = it.category,
                                image = it.image
                            )
                        }

                        withContext(Dispatchers.Main) {
                            val combinedList = mutableListOf<ProductsItem>()
                            combinedList.addAll(convertedLocalList)
                            combinedList.addAll(apiProducts)

                            val finalUniqueList = combinedList.distinctBy { it.id }

                            savedProductList = finalUniqueList.toMutableList()
                            setupAdapter(savedProductList!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<ProductsItem>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("HomeFragment", "Error: ${t.message}")
            }
        })
    }

    private fun setupAdapter(list: MutableList<ProductsItem>) {
        homeAdapter = ProductAdapter(list) { clickedProduct ->
            val bottomSheet = ProductDetail(
                product = clickedProduct,
                origin = "HOME",
                onProductDeleted = { deletedId ->
                    homeAdapter.deleteProduct(deletedId)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(requireContext())
                        val bookmarkItem =
                            db.bookmarkDao().getAllBookmarks().find { it.id == deletedId }
                        bookmarkItem?.let {
                            db.bookmarkDao().deleteBookmark(it)
                        }
                        val localItem =
                            db.localProductDao().getAllLocalProducts().find { it.id == deletedId }
                        if (localItem != null) {
                            db.localProductDao().deleteLocalProduct(localItem)
                        }
                    }
                },
                onProductEdited = { updatedItem ->
                    homeAdapter.updateProduct(updatedItem)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(requireContext())
                        val bookmarkItem =
                            db.bookmarkDao().getAllBookmarks().find { it.id == updatedItem.id }

                        if (bookmarkItem != null) {
                            bookmarkItem.title = updatedItem.title
                            bookmarkItem.price = updatedItem.price
                            bookmarkItem.category = updatedItem.category
                            bookmarkItem.description = updatedItem.description
                            bookmarkItem.image = updatedItem.image

                            db.bookmarkDao().updateBookmark(bookmarkItem)
                        }
                    }
                })
            bottomSheet.show(parentFragmentManager, "ProductDetail")
        }
        rvProducts.adapter = homeAdapter
    }
}