package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import java.lang.Exception

class RepresentativeViewModel : ViewModel() {

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */
    fun getRepresentativesList(address: Address) {
        viewModelScope.launch {
            try {
                val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(getAddressFormattedForApi())
                _representatives.value =
                    offices.flatMap { office -> office.getRepresentatives(officials) }

            } catch (e: Exception) {
                _representatives.value = ArrayList()
            }
        }
    }

    private fun getAddressFormattedForApi(): String {
        if (address.value != null) {
            val line1 = address.value!!.line1
            val line2 = address.value!!.line2
            val city = address.value!!.city
            val state = address.value!!.state
            val zip = address.value!!.zip
            return if (line2.isNullOrEmpty()) {
                "$line1, $city, $state, $zip"
            } else {
                "$line1 $line2, $city, $state, $zip"
            }
        }
        return ""
    }

    fun setAddressFromFields(line1 : String, line2 : String?, city : String, state : String, zip : String) {
        _address.value = Address(line1, line2, city, state, zip)
    }


}
