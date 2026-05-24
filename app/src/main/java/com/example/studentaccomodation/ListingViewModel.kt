package com.example.studentaccomodation

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ListingViewModel(private val repo: FirebaseRepository) : ViewModel() {

    data class FilterParams(
        val query: String = "",
        val minPrice: Double = 0.0, 
        val maxPrice: Double = Double.MAX_VALUE,
        val location: String = "", 
        val type: String = "", 
        val date: String = ""
    )
    
    private val _filterParams = MutableLiveData(FilterParams())
    val filterParams: LiveData<FilterParams> = _filterParams

    val availableListings: LiveData<List<Listing>> = repo.getAvailableListings()

    // Simplified combined stream
    val filteredListings: LiveData<List<Listing>> = MediatorLiveData<List<Listing>>().apply {
        val update = {
            val listings = availableListings.value ?: emptyList()
            val params = _filterParams.value ?: FilterParams()
            value = performFiltering(listings, params)
        }
        addSource(availableListings) { update() }
        addSource(_filterParams) { update() }
    }

    private fun performFiltering(listings: List<Listing>, params: FilterParams): List<Listing> {
        val q = params.query.trim().lowercase()
        return listings.filter { l ->
            val matchQ = q.isEmpty() || 
                    l.title.contains(q, true) || 
                    l.area.contains(q, true) || 
                    l.type.contains(q, true) ||
                    l.location.contains(q, true)
            
            val matchPrice = l.price >= params.minPrice && l.price <= params.maxPrice
            
            val matchLoc = params.location.isBlank() || 
                           params.location.equals("Any Area", true) ||
                           l.area.contains(params.location, true) || 
                           l.location.contains(params.location, true)
            
            val matchType = params.type.isBlank() || 
                            params.type.equals("Any Type", true) ||
                            l.type.equals(params.type, true)
            
            val matchDate = params.date.isBlank() || 
                            l.availabilityDate.isEmpty() || 
                            l.availabilityDate <= params.date

            matchQ && matchPrice && matchLoc && matchType && matchDate
        }
    }

    fun setFilterParams(query: String, min: Double, max: Double, loc: String, type: String, date: String) {
        _filterParams.value = FilterParams(query, min, max, loc.trim(), type.trim(), date)
    }

    fun updateQuery(q: String) {
        val current = _filterParams.value ?: FilterParams()
        if (current.query != q) {
            _filterParams.value = current.copy(query = q)
        }
    }

    fun clearFilters() {
        _filterParams.value = FilterParams()
    }

    val allListings: LiveData<List<Listing>> = repo.getAllListings()
    private val _selected = MutableLiveData<Listing?>()
    val selected: LiveData<Listing?> = _selected
    private val _addResult = MutableLiveData<Result<String>>()
    val addResult: LiveData<Result<String>> = _addResult
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadListing(id: String) = viewModelScope.launch { _selected.value = repo.getListing(id) }
    fun getProviderListings(uid: String): LiveData<List<Listing>> = repo.getListingsByProvider(uid)
    fun addListing(listing: Listing) = viewModelScope.launch {
        _loading.value = true
        try {
            val id = repo.addListing(listing)
            _addResult.value = Result.success(id)
        } catch (e: Exception) {
            _addResult.value = Result.failure(e)
        } finally { _loading.value = false }
    }
    fun savePreferences(pref: UserPreference) = viewModelScope.launch { repo.savePreferences(pref) }
    fun getPreferencesLive(uid: String): LiveData<UserPreference?> = repo.getPreferencesLive(uid)
    suspend fun getPreferences(uid: String): UserPreference? = repo.getPreferences(uid)
}
