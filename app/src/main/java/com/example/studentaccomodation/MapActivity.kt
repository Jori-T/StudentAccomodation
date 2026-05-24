package com.example.studentaccomodation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentaccomodation.databinding.ActivityMapBinding


class MapActivity : AppCompatActivity() {

    private lateinit var b: ActivityMapBinding

    // Botswana Accountancy College coordinates
    private val BAC_LAT = -24.6543
    private val BAC_LNG = 25.9087

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMapBinding.inflate(layoutInflater)
        setContentView(b.root)

        val lat      = intent.getDoubleExtra("lat",      BAC_LAT)
        val lng      = intent.getDoubleExtra("lng",      BAC_LNG)
        val title    = intent.getStringExtra("title")    ?: "Listing"
        val distance = intent.getDoubleExtra("distance", 0.0)

        b.tvListingTitleMap.text = title
        b.tvDistanceValue.text   = String.format("%.1f km from BAC (Gaborone)", distance)

        b.btnDirections.setOnClickListener {
            openDirections(lat, lng)
        }

        b.btnOpenInMaps.setOnClickListener {
            val uri = Uri.parse("geo:$lat,$lng?q=${Uri.encode(title)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser Google Maps
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(title)}&query_place_id=$lat,$lng")
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }

        b.btnBack.setOnClickListener { finish() }
    }

    private fun openDirections(destLat: Double, destLng: Double) {
        // Try Google Maps navigation app first
        val gmapsUri = Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
        val gmapsIntent = Intent(Intent.ACTION_VIEW, gmapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (gmapsIntent.resolveActivity(packageManager) != null) {
            startActivity(gmapsIntent)
        } else {
            // Fallback: Google Maps in browser
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                        "&origin=$BAC_LAT,$BAC_LNG" +
                        "&destination=$destLat,$destLng" +
                        "&travelmode=driving"
            )
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}
