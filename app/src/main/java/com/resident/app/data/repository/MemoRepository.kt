package com.resident.app.data.repository

import com.resident.app.data.database.MemoDao
import com.resident.app.data.entity.Memo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val memoDao: MemoDao
) {
    fun getAllMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    suspend fun getMemoById(id: Long): Memo? = memoDao.getMemoById(id)

    fun getActiveMemos(): Flow<List<Memo>> = memoDao.getActiveMemos()

    fun getImmediateMemos(): Flow<List<Memo>> = memoDao.getImmediateMemos()

    suspend fun getDueMemos(): List<Memo> = memoDao.getDueMemos(System.currentTimeMillis())

    suspend fun insertMemo(memo: Memo): Long = memoDao.insertMemo(memo)

    suspend fun updateMemo(memo: Memo) = memoDao.updateMemo(memo)

    suspend fun deleteMemo(memo: Memo) = memoDao.deleteMemo(memo)

    suspend fun deleteMemoById(id: Long) = memoDao.deleteMemoById(id)

    suspend fun deleteAllMemos() = memoDao.deleteAllMemos()
}
