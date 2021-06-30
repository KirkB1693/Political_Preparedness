package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    private lateinit var binding: FragmentElectionBinding
    private lateinit var viewModel: ElectionsViewModel
    private lateinit var savedElectionsAdapter: ElectionListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this
        val viewModelFactory = ElectionsViewModelFactory(ElectionDatabase.getInstance(requireContext()).electionDao)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(ElectionsViewModel::class.java)

        savedElectionsAdapter = ElectionListAdapter(ElectionListener {
            binding.viewModel!!.displayElectionDetails(it)
        })

        val upcomingElectionsAdapter = ElectionListAdapter(ElectionListener {
            binding.viewModel!!.displayElectionDetails(it)
        })

        binding.viewModel = viewModel


        viewModel.navigateToSelectedElection.observe(viewLifecycleOwner, {
            if (null != it) {
                this.findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it.id, it.division))
                viewModel.displayElectionDetailsComplete()
            }
        })

        binding.upcomingElectionsRecyclerview.adapter = upcomingElectionsAdapter
        binding.savedElectionsRecyclerview.adapter = savedElectionsAdapter

        observeElections()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadElections()
    }


    private fun observeElections() {
        viewModel.statusUpcomingElections.observe(viewLifecycleOwner, { status ->
            when {
                status.equals(ElectionsApiStatus.LOADING) -> {
                    binding.upcomingElectionsLoading.visibility = View.VISIBLE
                    binding.upcomingElectionsLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.loading_animation))
                    binding.upcomingElectionsRecyclerview.visibility = View.INVISIBLE
                }
                status.equals(ElectionsApiStatus.DONE) -> {
                    binding.upcomingElectionsLoading.visibility = View.GONE
                    binding.upcomingElectionsRecyclerview.visibility = View.VISIBLE
                }
                status.equals(ElectionsApiStatus.ERROR) -> {
                    binding.upcomingElectionsLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_connection_error))
                }
            }
        })

        viewModel.statusSavedElections.observe(viewLifecycleOwner, { status ->
            when {
                status.equals(ElectionsApiStatus.LOADING) -> {
                    binding.savedElectionsLoading.visibility = View.VISIBLE
                    binding.savedElectionsLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.loading_animation))
                    binding.savedElectionsRecyclerview.visibility = View.INVISIBLE
                }
                status.equals(ElectionsApiStatus.DONE) -> {
                    binding.savedElectionsLoading.visibility = View.GONE
                    binding.savedElectionsRecyclerview.visibility = View.VISIBLE
                }
                status.equals(ElectionsApiStatus.ERROR) -> {
                    binding.savedElectionsLoading.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_broken_image))
                }
            }
        })

        viewModel.savedElections.observe(viewLifecycleOwner, { elections ->
            savedElectionsAdapter.submitList(elections)
        })
    }

}