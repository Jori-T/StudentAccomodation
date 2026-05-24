package com.example.studentaccomodation

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentaccomodation.NotificationHelper
import com.example.studentaccomodation.SessionManager
import com.example.studentaccomodation.BookingViewModel
import com.example.studentaccomodation.ViewModelFactory
import com.example.studentaccomodation.databinding.ActivityPaymentBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var b: ActivityPaymentBinding
    private val vm: BookingViewModel by viewModels { ViewModelFactory() }
    private val fmt = NumberFormat.getNumberInstance(Locale.US)
    private var moveInDate = ""

    private var listingId    = ""
    private var listingTitle = ""
    private var listingLoc   = ""
    private var listingPrice = 0.0
    private var deposit      = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(b.root)

        listingId    = intent.getStringExtra("listing_id")       ?: ""
        listingTitle = intent.getStringExtra("listing_title")    ?: ""
        listingLoc   = intent.getStringExtra("listing_location") ?: ""
        listingPrice = intent.getDoubleExtra("listing_price",    0.0)
        deposit      = intent.getDoubleExtra("deposit_amount",   0.0)

        setSupportActionBar(b.toolbar)
        b.toolbar.setNavigationOnClickListener { finish() }

        populateSummary()
        setupCardPreview()
        setupDatePicker()
        setupPaymentToggle()
        setupObservers()
        setupPayButton()
    }

    private fun populateSummary() {
        b.tvSummaryProperty.text = listingTitle
        b.tvSummaryLocation.text = listingLoc
        b.tvSummaryRent.text     = "BWP ${fmt.format(listingPrice.toInt())}"
        
        // Ensure deposit shown is not zero, fallback to 25% if something went wrong
        val displayDeposit = if (deposit <= 0) listingPrice * 0.25 else deposit
        b.tvSummaryDeposit.text  = "BWP ${fmt.format(displayDeposit.toInt())}"
        
        // Update the global deposit variable if it was zero
        if (deposit <= 0) deposit = displayDeposit
    }

    private fun setupCardPreview() {
        b.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val raw = s.toString().replace(" ", "")
                val formatted = raw.chunked(4).joinToString(" ")
                if (formatted != s.toString()) {
                    b.etCardNumber.removeTextChangedListener(this)
                    b.etCardNumber.setText(formatted)
                    b.etCardNumber.setSelection(formatted.length)
                    b.etCardNumber.addTextChangedListener(this)
                }
                b.tvCardPreviewNumber.text =
                    if (raw.length >= 4) "•••• •••• •••• ${raw.takeLast(4)}" else "•••• •••• •••• ••••"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        b.etCardName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                b.tvCardPreviewName.text = s.toString().uppercase().ifEmpty { "YOUR NAME" }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        b.etExpiry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val raw = s.toString().replace("/", "")
                if (raw.length >= 2) {
                    val f = "${raw.take(2)}/${raw.drop(2)}"
                    if (f != s.toString()) {
                        b.etExpiry.removeTextChangedListener(this)
                        b.etExpiry.setText(f); b.etExpiry.setSelection(f.length)
                        b.etExpiry.addTextChangedListener(this)
                    }
                    b.tvCardPreviewExpiry.text = f
                } else b.tvCardPreviewExpiry.text = "MM/YY"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDatePicker() {
        val show = View.OnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                moveInDate = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    .format(Calendar.getInstance().apply { set(y, m, d) }.time)
                b.etMoveIn.setText(moveInDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .also { it.datePicker.minDate = System.currentTimeMillis() }.show()
        }
        b.etMoveIn.setOnClickListener(show)
        b.tilMoveIn.setEndIconOnClickListener(show)
    }

    private fun setupPaymentToggle() {
        b.rgPaymentMethod.setOnCheckedChangeListener { _, id ->
            b.cardDetailsView.visibility =
                if (id == com.example.studentaccomodation.R.id.rbCard) View.VISIBLE else View.GONE
        }
    }

    private fun setupObservers() {
        vm.loading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            b.btnPay.isEnabled = !loading
            b.btnPay.text = if (loading) "Processing…" else "Confirm Payment"
        }
        vm.reservationResult.observe(this) { result ->
            result.onSuccess { res ->
                NotificationHelper.sendReservationConfirmed(this, res.referenceNumber, listingTitle)
                startActivity(Intent(this, ReceiptActivity::class.java).apply {
                    putExtra("ref_number",       res.referenceNumber)
                    putExtra("listing_title",    listingTitle)
                    putExtra("listing_location", listingLoc)
                    putExtra("amount_paid",      res.amountPaid)
                    putExtra("move_in_date",     res.moveInDate)
                    putExtra("payment_method",   res.paymentMethod)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            result.onFailure { e ->
                b.tvError.visibility = View.VISIBLE
                b.tvError.text = e.message
            }
        }
    }

    private fun setupPayButton() {
        b.btnPay.setOnClickListener {
            b.tvError.visibility = View.GONE
            if (moveInDate.isEmpty()) {
                Toast.makeText(this, "Please select a move-in date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isCard = b.rbCard.isChecked
            if (isCard) {
                val num  = b.etCardNumber.text.toString().replace(" ", "")
                val name = b.etCardName.text.toString().trim()
                val exp  = b.etExpiry.text.toString().trim()
                val cvv  = b.etCvv.text.toString().trim()
                if (num.length  < 16) { b.tilCardNumber.error = "Valid card required"; return@setOnClickListener }
                if (name.isEmpty())   { b.tilCardName.error = "Name required"; return@setOnClickListener }
                if (exp.length  < 5)  { b.tilExpiry.error = "Valid expiry"; return@setOnClickListener }
                if (cvv.length  < 3)  { b.tilCvv.error = "Valid CVV"; return@setOnClickListener }
                listOf(b.tilCardNumber, b.tilCardName, b.tilExpiry, b.tilCvv).forEach { it.error = null }
                pay("Credit/Debit Card", num.takeLast(4))
            } else {
                pay(if (b.rbMobileMoney.isChecked) "Mobile Money" else "Bank Transfer (EFT)", "")
            }
        }
    }

    private fun pay(method: String, last4: String) {
        val uid  = SessionManager.uid(this)
        val name = SessionManager.name(this)
        
        // Use total amount (Rent + Deposit) for the reservation
        val totalAmount = listingPrice + deposit
        vm.makeReservation(listingId, listingTitle, listingLoc, uid, name, listingPrice, totalAmount, method, last4, moveInDate)
    }
}
