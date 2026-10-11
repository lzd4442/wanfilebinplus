package com.wanfile.binplus.data.local

import com.wanfile.binplus.data.model.ConvertRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConvertRepository @Inject constructor(
    private val dao: ConvertRecordDao
) {

    fun observeHistory(): Flow<List<ConvertRecord>> = dao.observeAll()

    suspend fun saveRecord(record: ConvertRecord): Long = dao.insert(record)

    suspend fun deleteRecord(record: ConvertRecord) = dao.delete(record)

    suspend fun clearHistory() = dao.clearAll()
}
