package com.example.studentaccomodation
import androidx.lifecycle.*
import com.example.studentaccomodation.Reservation
import com.example.studentaccomodation.FirebaseRepository
import kotlinx.coroutines.launch

class BookingViewModel(private val repo: FirebaseRepository) : ViewModel() {

    private val _reservationResult = MutableLiveData<Result<Reservation>>()
    val reservationResult: LiveData<Result<Reservation>> = _reservationResult

    private val _isReserved = MutableLiveData<Boolean>()
    val isReserved: LiveData<Boolean> = _isReserved

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun getUserReservations(uid: String): LiveData<List<Reservation>> = repo.getUserReservations(uid)

    fun checkIfReserved(listingId: String) = viewModelScope.launch {
        _isReserved.value = repo.getReservationForListing(listingId) != null
    }

    fun makeReservation(
        listingId: String, listingTitle: String, listingLocation: String,
        userId: String, userName: String, amount: Double, monthlyRent: Double,
        paymentMethod: String, cardLastFour: String, moveInDate: String
    ) = viewModelScope.launch {
        _loading.value = true
        try {
            val ref = "SN-${System.currentTimeMillis().toString().takeLast(8)}-${(1000..9999).random()}"
            val reservation = Reservation(
                listingId = listingId, listingTitle = listingTitle,
                listingLocation = listingLocation, userId = userId, userName = userName,
                referenceNumber = ref, amountPaid = amount, monthlyRent = monthlyRent,
                paymentMethod = paymentMethod, cardLastFour = cardLastFour, moveInDate = moveInDate
            )
            val created = repo.createReservation(reservation)
            _reservationResult.value = Result.success(created)
        } catch (e: Exception) {
            _reservationResult.value = Result.failure(e)
        } finally { _loading.value = false }
    }
}
