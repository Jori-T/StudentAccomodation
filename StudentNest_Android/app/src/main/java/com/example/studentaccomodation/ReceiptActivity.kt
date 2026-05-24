package com.example.studentaccomodation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentaccomodation.MainActivity
import com.example.studentaccomodation.databinding.ActivityReceiptBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReceiptActivity : AppCompatActivity() {

    private lateinit var b: ActivityReceiptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(b.root)

        val ref    = intent.getStringExtra("ref_number")       ?: ""
        val title  = intent.getStringExtra("listing_title")    ?: ""
        val loc    = intent.getStringExtra("listing_location") ?: ""
        val amount = intent.getDoubleExtra("amount_paid", 0.0)
        val moveIn = intent.getStringExtra("move_in_date")     ?: ""
        val method = intent.getStringExtra("payment_method")   ?: ""

        val fmt = NumberFormat.getNumberInstance(Locale.US)
        b.tvRefNumber.text        = ref
        b.tvReceiptProperty.text  = title
        b.tvReceiptLocation.text  = loc
        b.tvReceiptMoveIn.text    = moveIn
        b.tvReceiptMethod.text    = method
        b.tvReceiptAmount.text    = "BWP ${fmt.format(amount.toInt())}"
        b.tvReceiptDate.text      = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        b.btnViewBookings.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("navigate_to", "bookings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        b.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}