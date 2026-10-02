package com.pomodoro.focus.data.local

import androidx.room.*
import com.pomodoro.focus.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records WHERE dateKey = :dateKey")
    suspend fun getByDate(dateKey: String): DailyRecordEntity?

    @Query("SELECT * FROM daily_records ORDER BY dateKey DESC")
    fun getAll(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY dateKey ASC")
    fun getRange(startDate: String, endDate: String): Flow<List<DailyRecordEntity>>

    @Upsert
    suspend fun upsert(record: DailyRecordEntity)
}
