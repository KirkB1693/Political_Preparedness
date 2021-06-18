package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding
    private lateinit var viewModel: VoterInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this
        val electionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId.toLong()
        val division = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision
        val viewModelFactory =
            VoterInfoViewModelFactory(ElectionDatabase.getInstance(requireContext()).electionDao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(VoterInfoViewModel::class.java)
        binding.viewModel = viewModel

        if (division.state.isNotEmpty()) {
            viewModel.getVoterInfo(division.state, electionId)
        } else {
            displayErrorMessage()
        }

        viewModel.voterInfoStatus.observe(viewLifecycleOwner, Observer {
            if (it.equals(VoterInfoApiStatus.DONE)) {
                val voterInfo = viewModel.voterInfo.value
                if (voterInfo != null) {
                    val election = voterInfo.election
                    binding.electionName.title = election.name
                    binding.electionDate.text = election.electionDay.toString()
                    displayElectionOfficialInfo(voterInfo)
                    displayVotingLocations(voterInfo)
                    displayBallotInformation(voterInfo)
                } else {
                    binding.electionName.title = ""
                    binding.electionDate.text = ""
                    binding.address.visibility = View.GONE
                    binding.stateCorrespondenceHeader.visibility = View.GONE
                    binding.stateLocations.visibility = View.GONE
                    binding.stateBallot.visibility = View.GONE
                    binding.followOrUnfollowElectionButton.visibility = View.GONE
                }

            }
        })


        //TODO: Handle loading of URLs

        viewModel.initializeIsElectionSaved(electionId)
        viewModel.isElectionSaved.observe(viewLifecycleOwner, Observer {
            if (it) {
                displayUnfollowButton()
            } else {
                displayFollowButton()
            }
        })
        binding.followOrUnfollowElectionButton.setOnClickListener {
            if (viewModel.isElectionSaved.value == true) {
                // Then we are unfollowing election, so remove it
                viewModel.removeElection(electionId)
            } else {
                // Follow the election by saving it
                viewModel.saveElection()
            }
        }

        return binding.root
    }

    private fun displayBallotInformation(voterInfo: VoterInfoResponse) {
        if (voterInfo.state != null) {
            if (voterInfo.state[0].electionAdministrationBody.ballotInfoUrl != null) {
                binding.stateBallot.visibility = View.VISIBLE
                binding.stateBallot.setOnClickListener{
                    voterInfo.state[0].electionAdministrationBody.ballotInfoUrl?.let { url ->
                        openWebPage(
                            url
                        )
                    }
                }
            } else {
                binding.stateBallot.visibility = View.GONE
            }
        }
    }




    private fun displayVotingLocations(voterInfo: VoterInfoResponse) {
        if (voterInfo.state != null) {
            if (voterInfo.state[0].electionAdministrationBody.votingLocationFinderUrl != null) {
                binding.stateLocations.visibility = View.VISIBLE
                binding.stateLocations.setOnClickListener{
                    voterInfo.state[0].electionAdministrationBody.votingLocationFinderUrl?.let { url ->
                        openWebPage(
                            url
                        )
                    }
                }
            } else {
                binding.stateLocations.visibility = View.GONE
            }
        }
    }

    private fun displayElectionOfficialInfo(voterInfo: VoterInfoResponse) {
        if (voterInfo.state != null) {
            binding.stateCorrespondenceHeader.visibility = View.VISIBLE
            binding.stateCorrespondenceHeader.text =
                voterInfo.state[0].electionAdministrationBody.name
            binding.address.visibility = View.VISIBLE
            binding.address.text =
                voterInfo.state[0].electionAdministrationBody.correspondenceAddress?.toFormattedString()
                    ?: ""
        } else {
            binding.stateCorrespondenceHeader.visibility = View.GONE
            binding.address.visibility = View.GONE
        }
    }

    private fun displayErrorMessage() {
        binding.electionName.title = ""
        binding.electionDate.text = ""
        binding.stateHeader.text = getString(R.string.no_state_for_voter_info_error)
        binding.address.visibility = View.GONE
        binding.stateCorrespondenceHeader.visibility = View.GONE
        binding.stateLocations.visibility = View.GONE
        binding.stateBallot.visibility = View.GONE
        binding.stateCorrespondenceHeader.visibility = View.GONE
        binding.address.visibility = View.GONE
        binding.followOrUnfollowElectionButton.visibility = View.GONE
    }

    private fun displayFollowButton() {
        binding.followOrUnfollowElectionButton.text = getString(R.string.follow_election_button)
    }

    private fun displayUnfollowButton() {
        binding.followOrUnfollowElectionButton.text = getString(R.string.unfollow_election_button)
    }

    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}