package com.example.storeapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeapp.R
import com.example.storeapp.adapter.BookmarkAdapter
import com.example.storeapp.data.ProductsItem
import com.example.storeapp.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkFragment : Fragment() {

    private lateinit var rvBookmarks: RecyclerView
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBookmarks = view.findViewById(R.id.rvProducts)
        rvBookmarks.layoutManager = LinearLayoutManager(requireContext())

        loadBookmarks()
    }

    override fun onResume() {
        super.onResume()
        loadBookmarks()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadBookmarks()
        }
    }

    private fun loadBookmarks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val bookmarkList = db.bookmarkDao().getAllBookmarks().toMutableList()

            withContext(Dispatchers.Main) {
                bookmarkAdapter = BookmarkAdapter(bookmarkList, onItemClick = { clickedItem ->
                    val productData = ProductsItem(
                        id = clickedItem.id,
                        category = clickedItem.category,
                        description = clickedItem.description,
                        image = clickedItem.image,
                        price = clickedItem.price,
                        title = clickedItem.title
                    )

                    val bottomSheet = ProductDetail(
                        product = productData, origin = "BOOKMARK"
                    )

                    val bundle = Bundle()
                    bundle.putSerializable("PRODUCT_DATA", productData)

                    bundle.putDouble("LAT", clickedItem.latitude)
                    bundle.putDouble("LON", clickedItem.longitude)
                    bundle.putString("DATE", clickedItem.date)
                    bundle.putBoolean(
                        "FROM_BOOKMARK", true
                    )
                    bundle.putString("ORIGIN", "BOOKMARK")

                    bottomSheet.arguments = bundle
                    bottomSheet.show(parentFragmentManager, "ProductDetail")
                }, onDeleteClick = { itemToDelete, position ->
                    deleteBookmark(itemToDelete, position)
                })
                rvBookmarks.adapter = bookmarkAdapter
            }
        }
    }

    private fun deleteBookmark(item: com.example.storeapp.data.BookmarkItem, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            db.bookmarkDao().deleteBookmark(item)

            withContext(Dispatchers.Main) {
                bookmarkAdapter.removeItem(position)
                Toast.makeText(
                    requireContext(), "Remove ${item.title} from Bookmark", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}