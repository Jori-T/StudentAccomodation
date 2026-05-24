package com.example.studentaccomodation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.studentaccomodation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, ListingsFragment())
                .commit()
        }

        // Handle navigation from receipt
        intent.getStringExtra("navigate_to")?.let { dest ->
            if (dest == "bookings") {
                loadFragment(BookingsFragment())
                binding.bottomNavView.selectedItemId = R.id.nav_bookings
            }
        }

        binding.bottomNavView.setOnItemSelectedListener { item ->
            val frag: Fragment = when (item.itemId) {
                R.id.nav_listings -> ListingsFragment()
                R.id.nav_search   -> SearchFragment()
                R.id.nav_bookings -> BookingsFragment()
                R.id.nav_messages -> MessagesFragment()
                R.id.nav_profile  -> ProfileFragment()
                else              -> ListingsFragment()
            }
            loadFragment(frag)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .commit()
    }
}
