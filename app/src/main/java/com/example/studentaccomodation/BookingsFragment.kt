package com.example.studentaccomodation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccomodation.databinding.FragmentBookingsBinding

class BookingsFragment : Fragment() {

    private var _b: FragmentBookingsBinding? = null
    private val b get() = _b!!
    private val vm: BookingViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBookingsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = BookingsAdapter()
        b.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        b.rvBookings.adapter = adapter

        val uid = SessionManager.uid(requireContext())
        vm.getUserReservations(uid).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.rvBookings.visibility = if (list.isEmpty()) View.GONE   else View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
