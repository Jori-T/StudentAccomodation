package com.example.studentaccomodation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentaccomodation.R
import com.example.studentaccomodation.Listing
import com.example.studentaccomodation.databinding.ItemListingBinding
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.get
import kotlin.text.toInt

class ListingsAdapter(
    private val onItemClick: (Listing) -> Unit
) : ListAdapter<Listing, ListingsAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemListingBinding) : RecyclerView.ViewHolder(b.root) {

        private val fmt = NumberFormat.getNumberInstance(Locale.US)
        private val bgColors = listOf("#E8F4FD","#FEF9E7","#EAFAF1","#F9EBEA","#F0E6FF")
        private val months   = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

        fun bind(l: Listing) {
            b.tvTitle.text    = l.title
            b.tvLocation.text = l.area
            b.tvPrice.text    = "BWP ${fmt.format(l.price.toInt())}"
            b.tvType.text     = l.type
            b.tvRating.text   = String.format("%.1f", l.rating)
            b.tvDistance.text = String.format("%.1f km", l.distanceToBac)

            // Availability date
            b.tvAvailableDate.text = try {
                val p = l.availabilityDate.split("-")
                "${p[2].toInt()} ${months[p[1].toInt()]}"
            } catch (_: Exception) { l.availabilityDate }

            // Status
            when (l.status) {
                "AVAILABLE" -> {
                    b.tvStatus.text = "Available"
                    b.tvStatus.setTextColor(ContextCompat.getColor(b.root.context, R.color.status_available))
                    b.tvStatus.background = ContextCompat.getDrawable(b.root.context, R.drawable.bg_status_available)
                }
                "RESERVED" -> {
                    b.tvStatus.text = "Reserved"
                    b.tvStatus.setTextColor(ContextCompat.getColor(b.root.context, R.color.status_reserved))
                    b.tvStatus.background = ContextCompat.getDrawable(b.root.context, R.drawable.bg_status_reserved)
                }
                else -> {
                    b.tvStatus.text = "Occupied"
                    b.tvStatus.setTextColor(ContextCompat.getColor(b.root.context, R.color.status_occupied))
                }
            }

            // Load image from Firebase Storage URL or fallback to placeholder
            if (l.imageUrls.isNotEmpty()) {
                Glide.with(b.root.context)
                    .load(l.imageUrls[0])
                    .placeholder(R.drawable.ic_house)
                    .error(R.drawable.ic_house)
                    .centerCrop()
                    .into(b.ivListingImage)
            } else {
                b.ivListingImage.setBackgroundColor(
                    Color.parseColor(bgColors[absoluteAdapterPosition % bgColors.size])
                )
                b.ivListingImage.setImageResource(R.drawable.ic_house)
            }

            // Amenity chips
            b.amenitiesRow.removeAllViews()
            l.amenities.take(3).forEach { amenity ->
                val chip = LayoutInflater.from(b.root.context)
                    .inflate(R.layout.item_amenity_chip, b.amenitiesRow, false)
                val tv = chip.findViewById<android.widget.TextView>(R.id.tvAmenity)
                tv.text = emojiFor(amenity) + " " + amenity
                b.amenitiesRow.addView(chip)
            }

            b.root.setOnClickListener { onItemClick(l) }
        }

        private fun emojiFor(a: String) = when (a.lowercase()) {
            "wifi"        -> "📶"
            "water"       -> "💧"
            "electricity" -> "⚡"
            "parking"     -> "🚗"
            "security"    -> "🔒"
            "kitchen"     -> "🍳"
            "laundry"     -> "🧺"
            "dstv"        -> "📺"
            "garden"      -> "🌿"
            else          -> "✓"
        }
    }

    class Diff : DiffUtil.ItemCallback<Listing>() {
        override fun areItemsTheSame(a: Listing, b: Listing)    = a.id == b.id
        override fun areContentsTheSame(a: Listing, b: Listing) = a == b
    }
}
