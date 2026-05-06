package com.example.storeapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storeapp.R
import com.example.storeapp.data.BookmarkItem
import com.example.storeapp.data.ProductsItem
import com.example.storeapp.local.AppDatabase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.storeapp.api.RetrofitClient
import com.example.storeapp.data.LocalProductItem

// Bottom Sheet
class ProductDetail(
    private val product: ProductsItem?,
    private val origin: String,
    private val onProductDeleted: ((Int) -> Unit)? = null,
    private val onProductEdited: ((ProductsItem) -> Unit)? = null
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivImage = view.findViewById<ImageView>(R.id.ivDetailImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvCategory = view.findViewById<TextView>(R.id.tvDetailCategory)
        val tvPrice = view.findViewById<TextView>(R.id.tvDetailPrice)
        val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
        val ibBookmark = view.findViewById<ImageButton>(R.id.ibBookmark)
        val tvLocation = view.findViewById<TextView>(R.id.tvDetailLocation)
        val layoutHomeActions = view.findViewById<LinearLayout>(R.id.llEditDelete)

        product?.let { currentProduct ->
            tvTitle.text = currentProduct.title
            tvCategory.text = currentProduct.category
            tvPrice.text = "$ ${currentProduct.price}"
            tvDesc.text = currentProduct.description

            Glide.with(this).load(currentProduct.image).into(ivImage)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val isAlreadyBookmarked = db.bookmarkDao().isBookmarked(currentProduct.id)

                withContext(Dispatchers.Main) {
                    if (isAlreadyBookmarked) {
                        ibBookmark.setColorFilter(resources.getColor(R.color.red, null))
                    } else {
                        ibBookmark.setColorFilter(resources.getColor(R.color.black, null))
                    }
                }
            }

            ibBookmark.setOnClickListener {
                handleBookmark(currentProduct, ibBookmark)
            }
        }

        view.findViewById<ImageButton>(R.id.ibBack).setOnClickListener {
            dismiss()
        }

        if (origin == "BOOKMARK") {
            tvLocation.visibility = View.VISIBLE
            layoutHomeActions.visibility = View.GONE

            val lat = arguments?.getDouble("LAT") ?: 0.0
            val lon = arguments?.getDouble("LON") ?: 0.0
            tvLocation.text = "📍 Saved Location: $lat, $lon"

        } else {
            tvLocation.visibility = View.GONE
            layoutHomeActions.visibility = View.VISIBLE

            val btnEdit = view.findViewById<Button>(R.id.btnEdit)
            val btnDelete = view.findViewById<Button>(R.id.btnDelete)

            btnDelete.setOnClickListener {
                val productId = product?.id ?: 0

                RetrofitClient.instance.deleteProduct(productId)
                    .enqueue(object : Callback<okhttp3.ResponseBody> {
                        override fun onResponse(
                            call: Call<okhttp3.ResponseBody>,
                            response: Response<okhttp3.ResponseBody>
                        ) {

                            lifecycleScope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(requireContext())

                                val localItem = db.localProductDao().getAllLocalProducts()
                                    .find { it.id == productId }
                                localItem?.let { db.localProductDao().deleteLocalProduct(it) }

                                val bookmarkItem =
                                    db.bookmarkDao().getAllBookmarks().find { it.id == productId }
                                bookmarkItem?.let { db.bookmarkDao().deleteBookmark(it) }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Product Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onProductDeleted?.invoke(productId)
                                    dismiss()
                                }
                            }
                        }

                        override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "Network Error, but removing locally...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            btnEdit.setOnClickListener {
                val dialogView =
                    LayoutInflater.from(requireContext()).inflate(R.layout.product_edit, null)

                val builder = android.app.AlertDialog.Builder(requireContext())
                builder.setView(dialogView)
                val dialog = builder.create()

                val etImageUrl = dialogView.findViewById<EditText>(R.id.etEditImageUrl)
                val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
                val etCategory = dialogView.findViewById<EditText>(R.id.etEditCategory)
                val etPrice = dialogView.findViewById<EditText>(R.id.etEditPrice)
                val etDescription = dialogView.findViewById<EditText>(R.id.etEditDescription)
                val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelEdit)
                val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEdit)

                etImageUrl.setText(product?.image)
                etTitle.setText(product?.title)
                etCategory.setText(product?.category)
                etPrice.setText(product?.price?.toString())
                etDescription.setText(product?.description)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnSave.setOnClickListener {
                    val newImageUrl = etImageUrl.text.toString()
                    val newTitle = etTitle.text.toString()
                    val newCategory = etCategory.text.toString()
                    val newPrice = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                    val newDescription = etDescription.text.toString()

                    if (newTitle.isEmpty()) {
                        Toast.makeText(
                            requireContext(), "Title cannot be empty!", Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val updatedProduct = ProductsItem(
                        id = product?.id ?: 0,
                        title = newTitle,
                        category = newCategory,
                        price = newPrice,
                        description = newDescription,
                        image = newImageUrl
                    )

                    RetrofitClient.instance.updateProduct(updatedProduct.id, updatedProduct)
                        .enqueue(object : Callback<ProductsItem> {
                            override fun onResponse(
                                call: Call<ProductsItem>, response: Response<ProductsItem>
                            ) {
                                if (response.isSuccessful) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val db = AppDatabase.getDatabase(requireContext())

                                        val localItem = db.localProductDao().getAllLocalProducts()
                                            .find { it.id == updatedProduct.id }
                                        if (localItem != null) {
                                            val newLocalItem = LocalProductItem(
                                                id = updatedProduct.id,
                                                title = updatedProduct.title,
                                                price = updatedProduct.price,
                                                description = updatedProduct.description,
                                                category = updatedProduct.category,
                                                image = updatedProduct.image
                                            )
                                            db.localProductDao().insertLocalProduct(newLocalItem)
                                        }

                                        val bookmarkItem = db.bookmarkDao().getAllBookmarks()
                                            .find { it.id == updatedProduct.id }
                                        if (bookmarkItem != null) {
                                            bookmarkItem.title = updatedProduct.title
                                            bookmarkItem.price = updatedProduct.price
                                            bookmarkItem.category = updatedProduct.category
                                            bookmarkItem.description = updatedProduct.description
                                            bookmarkItem.image = updatedProduct.image

                                            db.bookmarkDao().updateBookmark(bookmarkItem)
                                        }

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Updated successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onProductEdited?.invoke(updatedProduct)
                                            dialog.dismiss()
                                            dismiss()
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<ProductsItem>, t: Throwable) {
                                Toast.makeText(
                                    requireContext(),
                                    "Update failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                dialog.show()
            }
        }
    }

    private fun handleBookmark(product: ProductsItem, ibBookmark: ImageButton) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val hasPermission = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    saveToDatabase(
                        product, currentDate, location.latitude, location.longitude, ibBookmark
                    )
                } else {
                    Toast.makeText(
                        requireContext(), "Location not found, please try again", Toast.LENGTH_SHORT
                    ).show()
                    saveToDatabase(product, currentDate, 0.0, 0.0, ibBookmark)
                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
            Toast.makeText(
                requireContext(),
                "Please allow location permission and click bookmark again!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveToDatabase(
        product: ProductsItem, date: String, lat: Double, lon: Double, ibBookmark: ImageButton
    ) {
        val bookmark = BookmarkItem(
            id = product.id,
            category = product.category,
            description = product.description,
            image = product.image,
            price = product.price,
            title = product.title,
            date = date,
            latitude = lat,
            longitude = lon
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())

            if (db.bookmarkDao().isBookmarked(product.id)) {

                db.bookmarkDao().deleteBookmark(bookmark)
                withContext(Dispatchers.Main) {
                    ibBookmark.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black))
                    Toast.makeText(
                        requireContext(), "Removed from Bookmarks", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

                db.bookmarkDao().insertBookmark(bookmark)
                withContext(Dispatchers.Main) {
                    ibBookmark.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red))
                    Toast.makeText(requireContext(), "Added to Bookmarks", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}