package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    // Daily aggregated reading logs
    @Query("SELECT * FROM reading_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<ReadingLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReadingLog)

    @Query("SELECT * FROM reading_logs WHERE dateString = :dateString LIMIT 1")
    suspend fun getLogForDate(dateString: String): ReadingLog?

    @Query("DELETE FROM reading_logs WHERE dateString = :dateString")
    suspend fun deleteLogForDate(dateString: String)

    @Query("DELETE FROM reading_logs")
    suspend fun deleteAllLogs()

    // Individual reading sessions
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

    // Books library
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun getBookByTitle(title: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Int)
}
