package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

enum class VoterInfoApiStatus { LOADING, ERROR, DONE }

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _voterInfoStatus = MutableLiveData<VoterInfoApiStatus>()
    val voterInfoStatus: LiveData<VoterInfoApiStatus>
        get() = _voterInfoStatus

    fun getVoterInfo(address: String, electionId: Long) {
        _voterInfoStatus.value = VoterInfoApiStatus.LOADING
        viewModelScope.launch {
            try {
                _voterInfo.value = CivicsApi.retrofitService.getVoterInfo(address, electionId)
                _voterInfoStatus.value = VoterInfoApiStatus.DONE
                _votingLocationsUrl.value = extractVotingLocationsUrl()
                _ballotInformationUrl.value = extractBallotInformationUrl()
            } catch (e: Exception) {
                _voterInfoStatus.value = VoterInfoApiStatus.ERROR
                _voterInfo.value = null
                Log.e("VoterInfoViewModel", "Error loading voter info : $e")
            }
        }

    }

    private val _votingLocationsUrl = MutableLiveData<String>()
    val votingLocationsUrl: LiveData<String>
    get() = _votingLocationsUrl

    private fun extractVotingLocationsUrl(): String? {
        if (_voterInfoStatus.value == VoterInfoApiStatus.DONE && _voterInfo.value != null) {
            return voterInfo.value?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl
        }
        return null
    }

    private val _ballotInformationUrl = MutableLiveData<String>()
    val ballotInformationUrl: LiveData<String>
    get() = _ballotInformationUrl

    private fun extractBallotInformationUrl(): String? {
        if (_voterInfoStatus.value == VoterInfoApiStatus.DONE && _voterInfo.value != null) {
            return voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
        }
        return null
    }


    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    private val _isElectionSaved = MutableLiveData<Boolean>()
    val isElectionSaved: LiveData<Boolean>
        get() = _isElectionSaved

    fun initializeIsElectionSaved(electionId: Long) {
        viewModelScope.launch {
            _isElectionSaved.value = dataSource.getElectionById(electionId) != null
        }
    }

    fun saveElection() {
        viewModelScope.launch {
            val voterInfo = _voterInfo.value
            if (voterInfo != null) {
                dataSource.saveElection(voterInfo.election)
                _isElectionSaved.value = true
            }
        }
    }

    fun removeElection(electionId: Long) {
        viewModelScope.launch {
            dataSource.clearElectionById(electionId.toInt())
            _isElectionSaved.value = false
        }
    }

}