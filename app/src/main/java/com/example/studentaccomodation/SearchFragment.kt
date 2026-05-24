package com.example.studentaccomodation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studentaccomodation.UserPreference
import com.example.studentaccomodation.SessionManager
import com.example.studentaccomodation.ListingViewModel
import com.example.studentaccomodation.ViewModelFactory
import com.example.studentaccomodation.databinding.FragmentSearchBinding
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {

    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!
    
    // Use activityViewModels to share filter state with ListingsFragment
    private val vm: ListingViewModel by activityViewModels { ViewModelFactory() }
    
    private var selectedDate = ""

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSearchBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        loadSavedPrefs()
        setupListeners()
        
        // Populate current filter state if exists
        vm.filterParams.value?.let { params ->
            if (params.minPrice > 0) b.etMinPrice.setText(params.minPrice.toInt().toString())
            if (params.maxPrice < Double.MAX_VALUE) b.etMaxPrice.setText(params.maxPrice.toInt().toString())
            if (params.location.isNotEmpty()) b.actvLocation.setText(params.location, false)
            if (params.type.isNotEmpty()) b.actvType.setText(params.type, false)
            if (params.date.isNotEmpty()) {
                selectedDate = params.date
                try {
                    val p = params.date.split("-")
                    // Show as DD/MM/YYYY
                    b.etDate.setText("${p[2]}/${p[1]}/${p[0]}")
                } catch (_: Exception) { b.etDate.setText(params.date) }
            }
        }
    }

    private fun setupDropdowns() {
        val areas = listOf("Any Area","Gaborone West","Gaborone North","Broadhurst","Old Naledi",
            "Tlokweng","Mogoditshane","Block 3","Block 6","Block 7","Block 8","Block 9","Block 10",
            "Extension 2","Extension 3","Extension 4","Extension 5","Extension 6","Extension 7",
            "Phakalane","Sebele","Ledumang")
        b.actvLocation.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, areas))

        val types = listOf("Any Type","Single Room","Double Room","En-suite Room","Bachelor Flat","Cottage","Self-contained Flat")
        b.actvType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))
    }

    private fun loadSavedPrefs() {
        val uid = SessionManager.uid(requireContext())
        vm.getPreferencesLive(uid).observe(viewLifecycleOwner) { pref ->
            // Only load if current fields are empty to avoid overwriting user's active search
            if (pref != null && b.etMinPrice.text.isNullOrEmpty() && b.actvLocation.text.isNullOrEmpty()) {
                if (pref.minPrice > 0)    b.etMinPrice.setText(pref.minPrice.toInt().toString())
                if (pref.maxPrice < 5000) b.etMaxPrice.setText(pref.maxPrice.toInt().toString())
                if (pref.preferredLocation.isNotEmpty()) b.actvLocation.setText(pref.preferredLocation, false)
                if (pref.preferredType.isNotEmpty())     b.actvType.setText(pref.preferredType, false)
                b.switchAlerts.isChecked = pref.notificationsEnabled
                updatePriceLabel()
            }
        }
    }

    private fun setupListeners() {
        b.etDate.setOnClickListener { showDatePicker() }
        b.tilDate.setEndIconOnClickListener { showDatePicker() }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updatePriceLabel()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        b.etMinPrice.addTextChangedListener(watcher)
        b.etMaxPrice.addTextChangedListener(watcher)

        b.btnApplyFilter.setOnClickListener { applyFilter() }
        b.btnSavePreferences.setOnClickListener { savePrefs() }
        b.btnClear.setOnClickListener { clearAll() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal2 = Calendar.getInstance().apply { set(y, m, d) }
            selectedDate = sdf.format(cal2.time)
            b.etDate.setText(String.format("%02d/%02d/%d", d, m + 1, y))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updatePriceLabel() {
        val min = b.etMinPrice.text.toString().toDoubleOrNull()
        val max = b.etMaxPrice.text.toString().toDoubleOrNull()
        b.tvPriceLabel.text = when {
            min != null && max != null -> "Budget: BWP ${min.toInt()} – ${max.toInt()}"
            min != null -> "From BWP ${min.toInt()}"
            max != null -> "Up to BWP ${max.toInt()}"
            else -> "Budget: Any"
        }
    }

    private fun applyFilter() {
        val min  = b.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0
        val max  = b.etMaxPrice.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
        val loc  = b.actvLocation.text.toString().let { if (it == "Any Area" || it.isEmpty())  "" else it }
        val type = b.actvType.text.toString().let    { if (it == "Any Type" || it.isEmpty())   "" else it }

        // Store filter state in shared ViewModel. Keep the existing search query if any.
        val currentQuery = vm.filterParams.value?.query ?: ""
        vm.setFilterParams(currentQuery, min, max, loc, type, selectedDate)

        // Navigate back to Listings using BottomNav
        requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            com.example.studentaccomodation.R.id.bottomNavView
        ).selectedItemId = com.example.studentaccomodation.R.id.nav_listings

        Toast.makeText(requireContext(), "Filter applied", Toast.LENGTH_SHORT).show()
    }

    private fun savePrefs() {
        val uid  = SessionManager.uid(requireContext())
        val min  = b.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0
        val max  = b.etMaxPrice.text.toString().toDoubleOrNull() ?: 5000.0
        val loc  = b.actvLocation.text.toString().let { if (it == "Any Area") "" else it }
        val type = b.actvType.text.toString().let    { if (it == "Any Type")  "" else it }
        vm.savePreferences(UserPreference(userId = uid, minPrice = min, maxPrice = max,
            preferredLocation = loc, preferredType = type, notificationsEnabled = b.switchAlerts.isChecked))
        Toast.makeText(requireContext(), "✅ Preferences saved to Firestore!", Toast.LENGTH_LONG).show()
    }

    private fun clearAll() {
        b.etMinPrice.text?.clear(); b.etMaxPrice.text?.clear()
        b.actvLocation.setText(""); b.actvType.setText("")
        b.etDate.text?.clear(); selectedDate = ""; b.tvPriceLabel.text = "Budget: Any"
        vm.clearFilters()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
