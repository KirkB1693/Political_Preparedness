package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    /**
     * Insert an election in the database. If the election already exists, replace it.
     *
     * @param election the election to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveElection(election: Election)

    /**
     * @return all elections.
     */
    @Query("SELECT * FROM election_table")
    suspend fun getAllElections(): List<Election>

    /**
     * @param electionId the id of the election
     * @return the election object with the electionId
     */
    @Query("SELECT * FROM election_table where id = :electionId")
    suspend fun getElectionById(electionId: Long): Election?

    /**
     * Delete all elections.
     */
    @Query("DELETE FROM election_table")
    suspend fun deleteAllElections()

    /**
     * @param electionId the id of the election
     * @return the election object with the electionId
     */
    @Query("DELETE FROM election_table where id = :electionId")
    suspend fun clearElectionById(electionId: Int)

}