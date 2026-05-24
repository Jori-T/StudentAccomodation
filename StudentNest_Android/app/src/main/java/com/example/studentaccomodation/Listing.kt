package com.example.studentaccomodation

data class Listing(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val location: String = "",
    val area: String = "",
    val type: String = "",
    val amenities: List<String> = emptyList(),
    val availabilityDate: String = "",
    val depositAmount: Double = 0.0,
    val imageUrls: List<String> = emptyList(),
    val landlordName: String = "",
    val landlordPhone: String = "",
    val providerId: String = "",
    val status: String = "AVAILABLE",   // AVAILABLE | RESERVED | OCCUPIED
    val latitude: Double = -24.6543,
    val longitude: Double = 25.9087,
    val distanceToBac: Double = 0.0,
    val rating: Float = 4.0f,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Safety getter to ensure deposit is never shown as zero if price exists
    val safeDepositAmount: Double
        get() = if (depositAmount <= 0 && price > 0) price * 0.25 else depositAmount

    constructor() : this("", "", "", 0.0, "", "", "", emptyList(), "",
        0.0, emptyList(), "", "", "", "AVAILABLE",
        -24.6543, 25.9087, 0.0, 4.0f, 0, 0L)
}
