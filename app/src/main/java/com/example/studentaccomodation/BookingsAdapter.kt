package com.example.studentaccomodation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccomodation.databinding.ItemBookingBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BookingsAdapter : ListAdapter<Reservation, BookingsAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Explicitly define the binding to avoid 'File' confusion
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    // Use 'val' instead of 'private val' if needed,
    // but the main thing is ensuring the import above is correct.
    inner class VH(val b: ItemBookingBinding) : RecyclerView.ViewHolder(b.root) {
        private val fmt = NumberFormat.getNumberInstance(Locale.US)
        private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)

        fun bind(r: Reservation) {
            // Use r.listingTitle?.ifEmpty { ... } to handle nullability safely
            b.tvBookingTitle.text    = r.listingTitle.ifEmpty { "Listing #${r.listingId.take(6)}" }
            b.tvBookingRef.text      = "Ref: ${r.referenceNumber}"

            // Fixed the BWP formatting logic
            val amount = r.amountPaid.toInt()
            b.tvBookingAmount.text   = "BWP ${fmt.format(amount)}"

            b.tvBookingMoveIn.text   = r.moveInDate
            b.tvBookingStatus.text   = r.status

            // Ensure listingLocation isn't null/empty
            b.tvBookingLocation.text = r.listingLocation.ifEmpty { sdf.format(Date(r.createdAt)) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(a: Reservation, b: Reservation)    = a.id == b.id
        override fun areContentsTheSame(a: Reservation, b: Reservation) = a == b
    }
}