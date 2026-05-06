package com.example.storeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storeapp.R
import com.example.storeapp.data.ProductsItem

class ProductAdapter(
    private val productList: MutableList<ProductsItem>,
    private val onItemClick: (ProductsItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]

        holder.tvTitle.text = currentProduct.title
        holder.tvPrice.text = String.format(java.util.Locale.ENGLISH, "$%.2f", currentProduct.price)

        holder.itemView.setOnClickListener {
            onItemClick(currentProduct)
        }

        Glide.with(holder.itemView.context).load(currentProduct.image).into(holder.ivImage)
    }

    fun addProduct(newProduct: ProductsItem) {
        productList.add(0, newProduct)
        notifyItemInserted(0)
    }

    fun deleteProduct(productId: Int) {
        val index = productList.indexOfFirst { it.id == productId }
        if (index != -1) {
            productList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateProduct(updatedProduct: ProductsItem) {
        val index = productList.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            productList[index] = updatedProduct
            notifyItemChanged(index)
        }
    }
}