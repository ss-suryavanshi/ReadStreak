package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for storing and querying individual reading sessions.
 */
@Dao
interface ReadingSessionDao {
    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getSessionsByDate(dateString: String): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE bookTitle = :bookTitle ORDER BY timestamp DESC")
    fun getSessionsByBook(bookTitle: String): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Int): ReadingSession?

    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSession(): ReadingSession?

    @Query("SELECT COALESCE(SUM(minutesRead), 0) FROM reading_sessions")
    fun getTotalMinutesRead(): Flow<Int>

    @Query("SELECT COALESCE(SUM(pagesRead), 0) FROM reading_sessions")
    fun getTotalPagesRead(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSession): Long

    @Update
    suspend fun updateSession(session: ReadingSession)

    @Delete
    suspend fun deleteSession(session: ReadingSession)

    @Query("DELETE FROM reading_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM reading_sessions")
    suspend fun deleteAllSessions()
}
