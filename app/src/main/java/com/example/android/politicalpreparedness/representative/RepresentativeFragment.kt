package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.LocationServices
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding
    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this).get(RepresentativeViewModel::class.java)
    }
    private lateinit var representativeAdapter: RepresentativeListAdapter

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        binding.viewModel = viewModel
        binding.address = viewModel.address.value
        binding.lifecycleOwner = this

        representativeAdapter = RepresentativeListAdapter()
        binding.representativesRecyclerview.adapter = representativeAdapter

        observeRepresentatives()
        observeAddress()

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            searchForRepresentatives()
        }

        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                hideKeyboard()
                getLocation()
            }
        }

        passRecyclerTouchToMotionLayout()

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun passRecyclerTouchToMotionLayout() {
        binding.representativesRecyclerview.setOnTouchListener{ _, event ->
            binding.representativeMotionlayoutContatiner.onTouchEvent(event)
            return@setOnTouchListener false
        }
    }

    private fun observeAddress() {
        viewModel.address.observe(viewLifecycleOwner,  {
            binding.address = it
        })
    }

    private fun searchForRepresentatives() {
        val line1 = binding.addressLine1.text.toString()
        val line2 = binding.addressLine2.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.selectedItem.toString()
        val zip = binding.zip.text.toString()
        if (line1.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.representative_serach_line1_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (city.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.representative_serach_city_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (state.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.representative_serach_state_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (zip.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.representative_serach_zip_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        viewModel.setAddressFromFields(line1, line2, city, state, zip)
        viewModel.address.value?.let { viewModel.getRepresentativesList() }
    }

    private fun observeRepresentatives() {
        viewModel.statusRepresentatives.observe(viewLifecycleOwner, { status ->
            when {
                status.equals(RepresentativesApiStatus.LOADING) -> {
                    binding.representativesLoading.visibility = View.VISIBLE
                    binding.representativesLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.loading_animation))
                    binding.representativesRecyclerview.visibility = View.INVISIBLE
                }
                status.equals(RepresentativesApiStatus.DONE) -> {
                    binding.representativesLoading.visibility = View.GONE
                    binding.representativesRecyclerview.visibility = View.VISIBLE
                }
                status.equals(RepresentativesApiStatus.ERROR) -> {
                    binding.representativesLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_connection_error))
                }
            }
        })

        viewModel.representatives.observe(viewLifecycleOwner, { representatives ->
            representativeAdapter.submitList(representatives)
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // If permissions not granted explain to user why they are necessary

        } else {
            // Permissions are granted so get location
            getLocation()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            false
        }
    }


    private fun isPermissionGranted(): Boolean {
        //Check if permission is already granted and return (true = granted, false = denied/other)
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val currentAddress = geoCodeLocation(location)
                    viewModel.setAddressFromFields(
                        currentAddress.line1,
                        currentAddress.line2,
                        currentAddress.city,
                        currentAddress.state,
                        currentAddress.zip
                    )
                    viewModel.getRepresentativesList()
                }
            }

    }


    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


}