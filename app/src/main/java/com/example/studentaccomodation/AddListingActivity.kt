package com.example.studentaccomodation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentaccomodation.Listing
import com.example.studentaccomodation.databinding.ActivityAddListingBinding
import com.example.studentaccomodation.SessionManager
import com.example.studentaccomodation.ListingViewModel
import com.example.studentaccomodation.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddListingActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddListingBinding
    private val vm: ListingViewModel by viewModels { ViewModelFactory() }
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddListingBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        b.toolbar.setNavigationOnClickListener { finish() }

        // Dropdowns
        val types = listOf("Single Room","Double Room","En-suite Room","Bachelor Flat","Cottage","Self-contained Flat")
        b.actvType.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types))

        val areas = listOf("Gaborone West","Gaborone North","Broadhurst","Old Naledi","Tlokweng",
            "Mogoditshane","Block 3","Block 6","Block 7","Block 8","Block 9","Block 10",
            "Extension 2","Extension 3","Extension 4","Extension 5","Extension 6","Extension 7",
            "Phakalane","Sebele","Ledumang")
        b.actvArea.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, areas))

        // Date picker
        b.etAvailabilityDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(Calendar.getInstance().apply { set(y, m, d) }.time)
                b.etAvailabilityDate.setText("$d/${m + 1}/$y")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Observe
        vm.loading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            b.btnSubmit.isEnabled    = !loading
        }
        vm.addResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "✅ Listing published to Firebase!", Toast.LENGTH_LONG).show()
                finish()
            }
            result.onFailure { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnSubmit.setOnClickListener { submit() }

        setupPriceWatcher()
    }

    private fun setupPriceWatcher() {
        b.etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val price = s.toString().toDoubleOrNull() ?: 0.0
                if (price > 0 && b.etDeposit.text.isNullOrEmpty()) {
                    // Auto-suggest 25% deposit if deposit field is empty
                    val suggested = price * 0.25
                    b.etDeposit.setText(suggested.toInt().toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun submit() {
        val title  = b.etTitle.text.toString().trim()
        val desc   = b.etDescription.text.toString().trim()
        val price  = b.etPrice.text.toString().toDoubleOrNull()
        val deposit = b.etDeposit.text.toString().toDoubleOrNull()
        val type   = b.actvType.text.toString()
        val area   = b.actvArea.text.toString()
        val lName  = b.etLandlordName.text.toString().trim()
        val lPhone = b.etLandlordPhone.text.toString().trim()

        if (title.isEmpty())    { b.tilTitle.error = "Required"; return }
        if (price == null || price <= 0) { b.tilPrice.error = "Valid price required"; return }
        
        // Deposit Validation: At least 25% and at most 100%
        if (deposit == null) {
            b.tilDeposit.error = "Required"
            return
        }
        if (deposit < price * 0.25) {
            b.tilDeposit.error = "Deposit must be at least 25% (BWP ${(price * 0.25).toInt()})"
            return
        }
        if (deposit > price) {
            b.tilDeposit.error = "Deposit cannot exceed rent (BWP ${price.toInt()})"
            return
        }
        b.tilDeposit.error = null

        if (type.isEmpty())     { Toast.makeText(this, "Select room type", Toast.LENGTH_SHORT).show(); return }
        if (area.isEmpty())     { Toast.makeText(this, "Select area", Toast.LENGTH_SHORT).show(); return }
        if (selectedDate.isEmpty()) { Toast.makeText(this, "Select availability date", Toast.LENGTH_SHORT).show(); return }
        if (lName.isEmpty())    { b.tilLandlordName.error = "Required"; return }
        if (lPhone.isEmpty())   { b.tilLandlordPhone.error = "Required"; return }

        b.tilTitle.error = null; b.tilPrice.error = null

        val amenities = buildList {
            if (b.cbWifi.isChecked)        add("WiFi")
            if (b.cbWater.isChecked)       add("Water")
            if (b.cbElectricity.isChecked) add("Electricity")
            if (b.cbParking.isChecked)     add("Parking")
            if (b.cbSecurity.isChecked)    add("Security")
            if (b.cbKitchen.isChecked)     add("Kitchen")
        }

        val uid = SessionManager.uid(this)
        val listing = Listing(
            title = title,
            description = desc.ifEmpty { "A comfortable room available for students." },
            price = price,
            location = "$area, Gaborone",
            area = area,
            type = type,
            amenities = amenities.ifEmpty { listOf("Water","Electricity") },
            availabilityDate = selectedDate,
            depositAmount = deposit,
            imageUrls = emptyList(),
            landlordName = lName,
            landlordPhone = lPhone,
            providerId = uid
        )
        vm.addListing(listing)
    }
}