package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: PortfolioSnapshotEntity)

    @Query("SELECT * FROM portfolio_snapshots ORDER BY timestamp ASC")
    fun getAllSnapshots(): Flow<List<PortfolioSnapshotEntity>>

    @Query("SELECT * FROM portfolio_snapshots WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getSnapshotsSince(startTime: Long): Flow<List<PortfolioSnapshotEntity>>

    @Query("DELETE FROM portfolio_snapshots WHERE timestamp < :olderThan")
    suspend fun deleteOldSnapshots(olderThan: Long)
}
