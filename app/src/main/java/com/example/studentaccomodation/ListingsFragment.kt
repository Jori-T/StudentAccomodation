package com.example.studentaccomodation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccomodation.databinding.FragmentListingsBinding
import java.util.Calendar

class ListingsFragment : Fragment() {

    private var _b: FragmentListingsBinding? = null
    private val b get() = _b!!
    
    private val vm: ListingViewModel by activityViewModels { ViewModelFactory() }
    
    private lateinit var adapter: ListingsAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListingsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting()
        setupRecycler()
        setupObservers()
        setupSearch()
        setupChips()
        setupFab()
    }

    private fun setupGreeting() {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        b.tvGreeting.text = when { h < 12 -> "Good morning,"; h < 17 -> "Good afternoon,"; else -> "Good evening," }
        b.tvUserName.text = SessionManager.name(requireContext()).split(" ").firstOrNull() ?: "Student"
    }

    private fun setupRecycler() {
        adapter = ListingsAdapter { listing ->
            startActivity(Intent(requireContext(), ListingDetailActivity::class.java)
                .putExtra("listing_id", listing.id))
        }
        b.rvListings.layoutManager = LinearLayoutManager(requireContext())
        b.rvListings.adapter = adapter
    }

    private fun setupObservers() {
        b.pbLoading.visibility = View.VISIBLE

        // Observe the unified filtered stream from ViewModel
        vm.filteredListings.observe(viewLifecycleOwner) { filtered ->
            b.pbLoading.visibility = View.GONE
            adapter.submitList(filtered)
            
            b.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            b.rvListings.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            b.swipeRefresh.isRefreshing = false
            
            // Trigger alerts for the full set if needed (optional optimization)
            vm.availableListings.value?.let { checkSmartAlerts(it) }
        }

        // Keep UI elements (search bar, chips) in sync with ViewModel state
        vm.filterParams.observe(viewLifecycleOwner) { params ->
            if (b.etSearch.text.toString() != params.query) {
                b.etSearch.setText(params.query)
            }
            updateChipUI(params.type)
        }

        b.swipeRefresh.setOnRefreshListener { 
            // Swipe refresh just triggers a re-fetch in the repository usually,
            // but here we just hide it since the DataBase listeners are active.
            b.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateChipUI(selectedType: String) {
        val chipMap = mapOf(
            "" to b.chipAll,
            "Single Room" to b.chipSingle,
            "Double Room" to b.chipDouble,
            "En-suite Room" to b.chipEnsuite,
            "Self-contained Flat" to b.chipFlat
        )
        chipMap.forEach { (type, chip) ->
            val isSelected = type == selectedType
            chip.chipBackgroundColor = ColorStateList.valueOf(
                Color.parseColor(if (isSelected) "#0E7C86" else "#33FFFFFF")
            )
        }
    }

    private fun setupSearch() {
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                vm.updateQuery(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        b.btnFilter.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavView
            ).selectedItemId = R.id.nav_search
        }
    }

    private fun setupChips() {
        val chipMap = mapOf(
            b.chipAll     to "",
            b.chipSingle  to "Single Room",
            b.chipDouble  to "Double Room",
            b.chipEnsuite to "En-suite Room",
            b.chipFlat    to "Self-contained Flat"
        )
        chipMap.forEach { (chip, type) ->
            chip.setOnClickListener {
                val cur = vm.filterParams.value ?: ListingViewModel.FilterParams()
                vm.setFilterParams(cur.query, cur.minPrice, cur.maxPrice, cur.location, type, cur.date)
            }
        }
    }

    private fun setupFab() {
        if (SessionManager.role(requireContext()) == "PROVIDER") {
            b.fabAddListing.visibility = View.VISIBLE
            b.fabAddListing.setOnClickListener {
                startActivity(Intent(requireContext(), AddListingActivity::class.java))
            }
        }
    }

    private fun checkSmartAlerts(listings: List<Listing>) {
        val uid = SessionManager.uid(requireContext())
        vm.getPreferencesLive(uid).observe(viewLifecycleOwner) { pref ->
            if (pref != null && pref.notificationsEnabled) {
                val matched = listings.firstOrNull { l ->
                    l.price >= pref.minPrice && l.price <= pref.maxPrice &&
                            (pref.preferredLocation.isEmpty() || l.area.contains(pref.preferredLocation, true))
                }
                matched?.let {
                    NotificationHelper.sendMatchNotification(requireContext(), it.title, it.price, it.area)
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
