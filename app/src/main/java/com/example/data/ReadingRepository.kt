package com.example.data

import kotlinx.coroutines.flow.Flow

class ReadingRepository(
    private val readingDao: ReadingDao,
    private val readingSessionDao: ReadingSessionDao? = null
) {
    val allLogs: Flow<List<ReadingLog>> = readingDao.getAllLogs()
    val allSessions: Flow<List<ReadingSession>> =
        readingSessionDao?.getAllSessions() ?: readingDao.getAllSessions()
    val allBooks: Flow<List<Book>> = readingDao.getAllBooks()

    fun getSessionsByDate(dateString: String): Flow<List<ReadingSession>> {
        return readingSessionDao?.getSessionsByDate(dateString)
            ?: readingDao.getSessionsByDate(dateString)
    }

    fun getSessionsByBook(bookTitle: String): Flow<List<ReadingSession>> {
        return readingSessionDao?.getSessionsByBook(bookTitle)
            ?: readingDao.getSessionsByBook(bookTitle)
    }

    suspend fun insertSession(session: ReadingSession): Long {
        val insertedId = readingSessionDao?.insertSession(session)
            ?: readingDao.insertSession(session)

        // Keep daily aggregate ReadingLog in sync for streak & heatmap tracking
        val existingLog = readingDao.getLogForDate(session.dateString)
        val updatedMinutes = (existingLog?.minutesRead ?: 0) + session.minutesRead
        val updatedPages = (existingLog?.pagesRead ?: 0) + session.pagesRead
        val updatedNotes = if (session.notes.isNotBlank()) {
            session.notes
        } else {
            existingLog?.notes ?: ""
        }
        readingDao.insertLog(
            ReadingLog(
                id = existingLog?.id ?: 0,
                dateString = session.dateString,
                minutesRead = updatedMinutes,
                pagesRead = updatedPages,
                bookTitle = session.bookTitle,
                notes = updatedNotes,
                timestamp = session.timestamp
            )
        )

        // Automatically advance book page progress if the book exists in the library
        val existingBook = readingDao.getBookByTitle(session.bookTitle)
        if (existingBook != null && session.pagesRead > 0) {
            val newPageCount = (existingBook.currentPage + session.pagesRead)
                .coerceIn(0, existingBook.totalPages)
            readingDao.updateBook(
                existingBook.copy(
                    currentPage = newPageCount,
                    isCompleted = newPageCount >= existingBook.totalPages
                )
            )
        }

        return insertedId
    }

    suspend fun updateLatestSessionNote(note: String) {
        val latest = readingSessionDao?.getLatestSession() ?: readingDao.getLatestSession()
        if (latest != null) {
            val updated = latest.copy(notes = note)
            if (readingSessionDao != null) {
                readingSessionDao.updateSession(updated)
            } else {
                readingDao.updateSession(updated)
            }
            val existingLog = readingDao.getLogForDate(latest.dateString)
            if (existingLog != null) {
                readingDao.insertLog(existingLog.copy(notes = note))
            }
        }
    }

    suspend fun deleteSession(session: ReadingSession) {
        if (readingSessionDao != null) {
            readingSessionDao.deleteSession(session)
        } else {
            readingDao.deleteSession(session)
        }
        syncDailyLogAfterSessionRemoval(session)
    }

    suspend fun deleteSessionById(id: Int) {
        val target = readingSessionDao?.getSessionById(id) ?: readingDao.getSessionById(id)
        if (readingSessionDao != null) {
            readingSessionDao.deleteSessionById(id)
        } else {
            readingDao.deleteSessionById(id)
        }
        if (target != null) {
            syncDailyLogAfterSessionRemoval(target)
        }
    }

    private suspend fun syncDailyLogAfterSessionRemoval(removed: ReadingSession) {
        val existingLog = readingDao.getLogForDate(removed.dateString) ?: return
        val remainingMinutes = (existingLog.minutesRead - removed.minutesRead).coerceAtLeast(0)
        val remainingPages = (existingLog.pagesRead - removed.pagesRead).coerceAtLeast(0)
        if (remainingMinutes <= 0) {
            readingDao.deleteLogForDate(removed.dateString)
        } else {
            readingDao.insertLog(
                existingLog.copy(
                    minutesRead = remainingMinutes,
                    pagesRead = remainingPages
                )
            )
        }
    }

    suspend fun insertLog(log: ReadingLog) {
        readingDao.insertLog(log)
    }

    suspend fun getLogForDate(dateString: String): ReadingLog? {
        return readingDao.getLogForDate(dateString)
    }

    suspend fun deleteAllLogs() {
        readingDao.deleteAllLogs()
        if (readingSessionDao != null) {
            readingSessionDao.deleteAllSessions()
        } else {
            readingDao.deleteAllSessions()
        }
    }

    suspend fun insertBook(book: Book) {
        readingDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) {
        readingDao.updateBook(book)
    }

    suspend fun deleteBook(id: Int) {
        readingDao.deleteBookById(id)
    }
}
