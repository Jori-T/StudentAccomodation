package com.example.studentaccomodation

data class Message(
    val id: String = "",
    val listingId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val content: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", false, 0L)
}

data class UserPreference(
    val id: String = "",
    val userId: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 5000.0,
    val preferredLocation: String = "",
    val preferredType: String = "",
    val notificationsEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", 0.0, 5000.0, "", "", true, 0L)
}
