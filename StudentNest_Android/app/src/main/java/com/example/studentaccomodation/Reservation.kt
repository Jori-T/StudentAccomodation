package com.example.studentaccomodation

data class Reservation(
    val id: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val listingLocation: String = "",
    val userId: String = "",
    val userName: String = "",
    val referenceNumber: String = "",
    val amountPaid: Double = 0.0,
    val monthlyRent: Double = 0.0,
    val paymentMethod: String = "",
    val cardLastFour: String = "",
    val status: String = "CONFIRMED",
    val moveInDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", "", 0.0, 0.0, "", "", "CONFIRMED", "", 0L)
}