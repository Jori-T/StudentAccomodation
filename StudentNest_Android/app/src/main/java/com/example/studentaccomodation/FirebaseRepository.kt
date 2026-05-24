package com.example.studentaccomodation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth       = FirebaseAuth.getInstance()
    private val firestore  = FirebaseFirestore.getInstance()
    private val rtdb       = FirebaseDatabase.getInstance()

    private val usersCol        = firestore.collection("users")
    private val reservationsCol = firestore.collection("reservations")
    private val prefsCol        = firestore.collection("preferences")
    private val db = FirebaseFirestore.getInstance()

    private val messagesRef = rtdb.reference.child("messages")
    private val listingsRef = rtdb.reference.child("listings")

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun registerWithEmail(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Registration failed")
    }

    suspend fun loginWithEmail(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    fun logout() = auth.signOut()

    suspend fun createUser(user: User) {
        usersCol.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        val snap = usersCol.document(uid).get().await()
        return snap.toObject(User::class.java)
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        usersCol.document(uid).set(mapOf("fcmToken" to token), SetOptions.merge()).await()
    }

    fun getAllListings(): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Listing::class.java) }
                    .sortedByDescending { it.createdAt }
                liveData.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error: ${error.message}")
            }
        })
        return liveData
    }

    fun getAvailableListings(): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Listing::class.java) }
                    .filter { it.status.equals("AVAILABLE", ignoreCase = true) }
                    .sortedByDescending { it.createdAt }
                liveData.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error: ${error.message}")
            }
        })
        return liveData
    }

    fun getListingsByProvider(providerId: String): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Listing::class.java) }
                    .filter { it.providerId == providerId }
                    .sortedByDescending { it.createdAt }
                liveData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    suspend fun addListing(listing: Listing): String {
        val ref = if (listing.id.isEmpty()) listingsRef.push() else listingsRef.child(listing.id)
        val withId = listing.copy(id = ref.key ?: "")
        ref.setValue(withId).await()
        return withId.id
    }

    suspend fun getListing(id: String): Listing? {
        val snap = listingsRef.child(id).get().await()
        return snap.getValue(Listing::class.java)
    }

    suspend fun seedListingsIfEmpty() {
        val snap = listingsRef.get().await()
        // If data exists, we don't overwrite, but we ensure images are present for existing items is harder
        // So we only seed if it's truly empty or low count
        //if (snap.exists() && snap.childrenCount >= 10) return

        val areas = listOf("Gaborone West", "Phakalane", "Tlokweng", "Broadhurst", "Mogoditshane", "Block 3", "Block 6", "Block 8", "Block 9", "Extension 2", "Sebele", "Phakalane Golf Estate", "Maruapula")
        val types = listOf("Single Room", "Double Room", "En-suite Room", "Bachelor Flat", "Cottage", "Self-contained Flat", "Shared Apartment")
        
        // High-quality house images
        val imagePool = listOf(
            "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800",
            "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800",
            "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800",
            "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800",
            "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800",
            "https://images.unsplash.com/photo-1554995207-c18c203602cb?w=800",
            "https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=800",
            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800",
            "https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800",
            "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=800",
            "https://images.unsplash.com/photo-1448630360428-65ff24c95ec2?w=800",
            "https://images.unsplash.com/photo-1513584684374-8bdb74838a0f?w=800"
        )

        val updates = mutableMapOf<String, Any>()
        for (i in 1..50) {
            val key = listingsRef.push().key ?: continue
            val area = areas[i % areas.size]
            val type = types[i % types.size]
            val price = 1500.0 + (i * 150)
            
            val listing = Listing(
                id = key,
                title = "$type in $area",
                description = "Modern and secure $type located in the heart of $area. Includes essential amenities for students.",
                price = price,
                area = area,
                location = "$area, Gaborone",
                type = type,
                status = "AVAILABLE",
                amenities = listOf("WiFi", "Water", "Parking", "Security"),
                depositAmount = price * 0.5,
                availabilityDate = "2026-0${(1..9).random()}-01",
                landlordName = "Landlord $i",
                landlordPhone = "7${(1000000..9999999).random()}",
                providerId = "provider_${i % 5}",
                imageUrls = listOf(imagePool[i % imagePool.size]),
                latitude = -24.65 + (i * 0.002),
                longitude = 25.90 + (i * 0.002),
                createdAt = System.currentTimeMillis() - (i * 100000)
            )
            updates[key] = listing
        }
        listingsRef.updateChildren(updates).await()
    }

    suspend fun createReservation(reservation: Reservation): Reservation {
        val ref = reservationsCol.document()
        val created = reservation.copy(id = ref.id)
        ref.set(created).await()
        listingsRef.child(reservation.listingId).child("status").setValue("RESERVED").await()
        return created
    }

    fun getUserReservations(userId: String): LiveData<List<Reservation>> {
        val liveData = MutableLiveData<List<Reservation>>()
        reservationsCol.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, _ ->
                liveData.value = snap?.documents?.mapNotNull { it.toObject(Reservation::class.java) } ?: emptyList()
            }
        return liveData
    }

    fun getConversation(listingId: String, userId: String, providerId: String): LiveData<List<Message>> {
        val sorted = listOf(userId, providerId).sorted()
        val chatKey = "${listingId}_${sorted[0]}_${sorted[1]}"
        val liveData = MutableLiveData<List<Message>>()
        messagesRef.child(chatKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                liveData.value = snapshot.children.mapNotNull { it.getValue(Message::class.java) }.sortedBy { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return liveData
    }

    fun sendMessage(message: Message, listingId: String, userId: String, providerId: String) {
        val sorted = listOf(userId, providerId).sorted()
        val chatKey = "${listingId}_${sorted[0]}_${sorted[1]}"
        val ref = messagesRef.child(chatKey).push()
        ref.setValue(message.copy(id = ref.key ?: ""))
    }

    suspend fun savePreferences(pref: UserPreference) {
        val ref = if (pref.id.isEmpty()) prefsCol.document() else prefsCol.document(pref.id)
        ref.set(pref.copy(id = ref.id), SetOptions.merge()).await()
    }

    fun getPreferencesLive(userId: String): LiveData<UserPreference?> {
        val liveData = MutableLiveData<UserPreference?>()
        prefsCol.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, _ ->
                liveData.value = snap?.documents?.firstOrNull()?.toObject(UserPreference::class.java)
            }
        return liveData
    }

    suspend fun getReservationForListing(listingId: String): Reservation? {
        return try {
            val querySnapshot = db.collection("reservations")
                .whereEqualTo("listingId", listingId)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.toObject(Reservation::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPreferences(userId: String): UserPreference? {
        val snap = prefsCol.whereEqualTo("userId", userId).get().await()
        return snap.documents.firstOrNull()?.toObject(UserPreference::class.java)
    }
}
