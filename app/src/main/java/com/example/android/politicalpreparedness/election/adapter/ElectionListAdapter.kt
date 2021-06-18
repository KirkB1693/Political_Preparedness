package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ElectionListItemBinding
import com.example.android.politicalpreparedness.network.models.Election

class ElectionListAdapter(private val clickListener: ElectionListener): ListAdapter<Election, ElectionViewHolder>(ElectionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder(ElectionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val election = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener.onElectionClick(election)
        }
        holder.bind(election)
    }


    //**
    // * Allows the RecyclerView to determine which items have changed when the [List] of [Asteroid]
    // * has been updated.
    // */
    companion object ElectionDiffCallback : DiffUtil.ItemCallback<Election>() {
        override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
            return oldItem == newItem
        }
    }
}


class ElectionViewHolder(private var binding: ElectionListItemBinding):
    RecyclerView.ViewHolder(binding.root) {
    fun bind(election: Election) {
        binding.election = election
        // This is important, because it forces the data binding to execute immediately,
        // which allows the RecyclerView to make the correct view size measurements
        binding.executePendingBindings()
    }
}

/**
 * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Election]
 * associated with the current item to the [onElectionClick] function.
 * @param clickListener lambda that will be called with the current [Election]
 */
class ElectionListener(val clickListener: (election: Election) -> Unit) {
    fun onElectionClick(election: Election) = clickListener(election)
}