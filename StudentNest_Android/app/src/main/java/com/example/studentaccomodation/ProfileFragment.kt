package com.example.studentaccomodation

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.studentaccomodation.LoginActivity
import com.example.studentaccomodation.SessionManager
import com.example.studentaccomodation.BookingViewModel
import com.example.studentaccomodation.ViewModelFactory
import com.example.studentaccomodation.databinding.FragmentProfileBinding
import kotlin.text.clear

class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: BookingViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProfileBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProfile()
        setupClicks()
    }

    private fun setupProfile() {
        val ctx  = requireContext()
        val name = SessionManager.name(ctx)
        val role = SessionManager.role(ctx)
        val uid  = SessionManager.uid(ctx)

        b.tvProfileName.text  = name
        b.tvProfileEmail.text = SessionManager.email(ctx)
        b.tvProfileRole.text  = role
        b.tvInitial.text      = name.firstOrNull()?.uppercase() ?: "S"
        b.tvStatId.text       = uid.take(6).uppercase()

        if (role == "PROVIDER") b.btnMyListings.visibility = View.VISIBLE

        vm.getUserReservations(uid).observe(viewLifecycleOwner) { list ->
            b.tvStatBookings.text = list.size.toString()
        }
    }

    private fun setupClicks() {
        b.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    SessionManager.clear(requireContext())
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        b.btnPreferences.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.example.studentaccomodation.R.id.navHostFragment, SearchFragment())
                .addToBackStack(null)
                .commit()
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.example.studentaccomodation.R.id.bottomNavView
            ).selectedItemId = com.example.studentaccomodation.R.id.nav_search
        }

        b.btnMyListings.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.example.studentaccomodation.R.id.navHostFragment, ListingsFragment())
                .commit()
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.example.studentaccomodation.R.id.bottomNavView
            ).selectedItemId = com.example.studentaccomodation.R.id.nav_listings
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
