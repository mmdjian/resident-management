package com.resident.app.data.database

import androidx.room.*
import com.resident.app.data.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY createdAt DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): Memo?

    @Query("SELECT * FROM memos WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE isImmediate = 1 AND isCompleted = 0")
    fun getImmediateMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE remindTime > 0 AND remindTime <= :currentTime AND isCompleted = 0")
    suspend fun getDueMemos(currentTime: Long): List<Memo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Delete
    suspend fun deleteMemo(memo: Memo)

    @Query("DELETE FROM memos")
    suspend fun deleteAllMemos()

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemoById(id: Long)
}
