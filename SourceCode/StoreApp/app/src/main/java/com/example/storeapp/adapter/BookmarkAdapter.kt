package com.example.storeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storeapp.R
import com.example.storeapp.data.BookmarkItem

class BookmarkAdapter(
    private val bookmarkList: MutableList<BookmarkItem>,
    private val onItemClick: (BookmarkItem) -> Unit,
    private val onDeleteClick: (BookmarkItem, Int) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivBookmarkImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBookmarkTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvBookmarkPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvBookmarkDate)
        val ibDelete: ImageButton = itemView.findViewById(R.id.ibBookmarkDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val item = bookmarkList[position]

        holder.tvTitle.text = item.title
        holder.tvPrice.text = String.format(java.util.Locale.ENGLISH, "$%.2f", item.price)
        val displayDate = if (item.date.length >= 16) item.date.take(16) else item.date
        holder.tvDate.text = "Saved: $displayDate"

        Glide.with(holder.itemView.context).load(item.image).into(holder.ivImage)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.ibDelete.setOnClickListener {
            onDeleteClick(item, holder.adapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }

    fun removeItem(position: Int) {
        bookmarkList.removeAt(position)
        notifyItemRemoved(position)
    }
}