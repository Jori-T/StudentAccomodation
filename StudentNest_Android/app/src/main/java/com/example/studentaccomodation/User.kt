package com.example.studentaccomodation

data class User(
    val uid: String = "",
    val studentId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "STUDENT",       // STUDENT | PROVIDER
    val institution: String = "",
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Firestore requires no-arg constructor
    constructor() : this("", "", "", "", "", "STUDENT", "", "", "", 0L)
}