package com.example.studentaccomodation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccomodation.R

class AmenitiesAdapter(private val items: List<String>) :
    RecyclerView.Adapter<AmenitiesAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_amenity_detail, parent, false)
    )

    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])

    class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView  = v.findViewById(R.id.tvAmenityName)
        private val iv: ImageView = v.findViewById(R.id.ivAmenityIcon)

        fun bind(amenity: String) {
            tv.text = amenity
            iv.setImageResource(when (amenity.lowercase()) {
                "wifi"        -> R.drawable.ic_wifi
                "water"       -> R.drawable.ic_water
                "electricity" -> R.drawable.ic_electricity
                "parking"     -> R.drawable.ic_parking
                "security"    -> R.drawable.ic_security
                "kitchen"     -> R.drawable.ic_kitchen
                "laundry"     -> R.drawable.ic_laundry
                "dstv"        -> R.drawable.ic_dstv
                "garden"      -> R.drawable.ic_garden
                else          -> R.drawable.ic_check_circle
            })
        }
    }
}
