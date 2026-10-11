package com.wanfile.binplus.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wanfile.binplus.data.model.ConvertRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ConvertRecordDao {

    @Query("SELECT * FROM convert_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ConvertRecord>>

    @Query("SELECT * FROM convert_records WHERE id = :id")
    suspend fun getById(id: Long): ConvertRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ConvertRecord): Long

    @Delete
    suspend fun delete(record: ConvertRecord)

    @Query("DELETE FROM convert_records")
    suspend fun clearAll()
}
