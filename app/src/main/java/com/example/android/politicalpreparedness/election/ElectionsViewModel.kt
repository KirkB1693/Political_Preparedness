package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import java.lang.Exception


enum class ElectionsApiStatus { LOADING, ERROR, DONE }

class ElectionsViewModel(private val datasource: ElectionDao) : ViewModel() {

    private val _statusUpcomingElections = MutableLiveData<ElectionsApiStatus>()
    val statusUpcomingElections: LiveData<ElectionsApiStatus>
        get() = _statusUpcomingElections

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _statusSavedElections = MutableLiveData<ElectionsApiStatus>()
    val statusSavedElections: LiveData<ElectionsApiStatus>
        get() = _statusSavedElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections


    fun loadElections() {
        loadSavedElections()
        loadUpcomingElections()
    }

    private fun loadUpcomingElections() {
        viewModelScope.launch {
            _statusUpcomingElections.value = ElectionsApiStatus.LOADING
            try {
                val electionResponse = CivicsApi.retrofitService.getElections()
                _upcomingElections.value = electionResponse.elections
                _statusUpcomingElections.value = ElectionsApiStatus.DONE
            } catch (e: Exception) {
                _upcomingElections.value = ArrayList()
                _statusUpcomingElections.value = ElectionsApiStatus.ERROR
            }
        }
    }

    private fun loadSavedElections() {
        viewModelScope.launch {
            _statusSavedElections.value = ElectionsApiStatus.LOADING
            _savedElections.value = datasource.getAllElections()
            _statusSavedElections.value = ElectionsApiStatus.DONE
        }
    }


    private val _navigateToSelectedElection = MutableLiveData<Election?>()
    val navigateToSelectedElection: LiveData<Election?>
        get() = _navigateToSelectedElection

    fun displayElectionDetails(election: Election) {
        _navigateToSelectedElection.value = election
    }

    fun displayElectionDetailsComplete() {
        _navigateToSelectedElection.value = null
    }
}