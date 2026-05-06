package com.example.storeapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.storeapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val bookmarkFragment = BookmarkFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, bookmarkFragment, "bookmark").hide(bookmarkFragment)
            add(R.id.fragmentContainer, homeFragment, "home")
            commit()
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment)
                        .show(homeFragment).commit()
                    activeFragment = homeFragment
                    true
                }

                R.id.nav_bookmark -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment)
                        .show(bookmarkFragment).commit()
                    activeFragment = bookmarkFragment
                    true
                }

                else -> false
            }
        }
    }
}