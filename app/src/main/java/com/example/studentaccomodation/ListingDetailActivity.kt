package com.example.studentaccomodation

import android.net.Uri
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.studentaccomodation.R
import com.example.studentaccomodation.databinding.ActivityListingDetailBinding
import java.text.NumberFormat
import java.util.Locale

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityListingDetailBinding
    private val listingVm: ListingViewModel by viewModels { ViewModelFactory() }
    private val fmt = NumberFormat.getNumberInstance(Locale.US)
    private val months = listOf("","January","February","March","April","May","June",
        "July","August","September","October","November","December")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListingDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        val id = intent.getStringExtra("listing_id") ?: run { finish(); return }
        setSupportActionBar(b.collapsingToolbar)
        b.collapsingToolbar.setNavigationOnClickListener { finish() }

        listingVm.loadListing(id)
        listingVm.selected.observe(this) { it?.let { l -> populate(l) } }
    }

    private fun populate(l: Listing) {
        b.collapsingToolbar.title = l.title
        b.tvTitle.text     = l.title
        b.tvRating.text    = String.format("%.1f", l.rating)
        b.tvReviews.text   = "(${l.reviewCount} reviews)"
        b.tvLocation.text  = l.location
        b.tvDistance.text  = String.format("%.1f km from BAC", l.distanceToBac)
        b.tvPrice.text     = "BWP ${fmt.format(l.price.toInt())}"
        b.tvBottomPrice.text = "BWP ${fmt.format(l.price.toInt())}"
        b.tvDeposit.text   = "BWP ${fmt.format(l.depositAmount.toInt())}"
        b.tvDescription.text = l.description
        b.tvLandlordName.text  = l.landlordName
        b.tvLandlordPhone.text = "+267 ${l.landlordPhone}"
        b.tvLandlordInitial.text = l.landlordName.firstOrNull()?.uppercase() ?: "L"
        b.tvTypeBadge.text = l.type

        // Available date
        b.tvAvailableDate.text = try {
            val p = l.availabilityDate.split("-")
            "${p[2].toInt()} ${months[p[1].toInt()]} ${p[0]}"
        } catch (_: Exception) { l.availabilityDate }

        // Status
        when (l.status) {
            "AVAILABLE" -> {
                b.tvStatusBadge.text = "✓ Available"
                b.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available))
                b.tvStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_status_available)
                b.btnReserve.isEnabled = true
                b.btnReserve.text = "Reserve Now"
                b.btnReserve.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_teal))
            }
            else -> {
                b.tvStatusBadge.text = "✗ Reserved"
                b.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_reserved))
                b.tvStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_status_reserved)
                b.btnReserve.isEnabled = false
                b.btnReserve.text = "Already Reserved"
                b.btnReserve.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.medium_gray))
            }
        }

        // Hero image
        if (l.imageUrls.isNotEmpty()) {
            Glide.with(this).load(l.imageUrls[0])
                .placeholder(R.drawable.ic_house).centerCrop().into(b.ivListingImage)
        } else {
            b.ivListingImage.setImageResource(R.drawable.ic_house)
        }

        // Amenities
        b.rvAmenities.layoutManager = GridLayoutManager(this, 2)
        b.rvAmenities.adapter = AmenitiesAdapter(l.amenities)

        // Clicks
        b.tvViewMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java).apply {
                putExtra("lat",      l.latitude)
                putExtra("lng",      l.longitude)
                putExtra("title",    l.title)
                putExtra("distance", l.distanceToBac)
            })
        }

        b.btnCall.setOnClickListener {
            val phone = l.landlordPhone.removePrefix("+267").replace(" ", "")
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+267$phone")))
        }

        b.btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("listing_id",    l.id)
                putExtra("provider_id",   l.providerId)
                putExtra("landlord_name", l.landlordName)
                putExtra("landlord_phone", l.landlordPhone)
                putExtra("listing_title", l.title)
            })
        }

        b.btnReserve.setOnClickListener {
            if (l.status == "AVAILABLE") {
                startActivity(Intent(this, PaymentActivity::class.java).apply {
                    putExtra("listing_id",       l.id)
                    putExtra("listing_title",    l.title)
                    putExtra("listing_location", l.location)
                    putExtra("listing_price",    l.price)
                    putExtra("deposit_amount",   l.depositAmount)
                })
            }
        }

        b.btnShare.setOnClickListener {
            val text = "Check out this place: ${l.title} in ${l.area} — BWP ${l.price.toInt()}/month via StudentNest!"
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) },
                "Share Listing"
            ))
        }
    }
}
