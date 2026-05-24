package com.example.studentaccomodation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.studentaccomodation.databinding.FragmentMessagesBinding


class MessagesFragment : Fragment() {
    private var _b: FragmentMessagesBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMessagesBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Chat conversations are opened directly from ListingDetailActivity
        b.emptyState.visibility    = View.VISIBLE
        b.rvConversations.visibility = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
