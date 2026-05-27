package com.imnotesminimal.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL")
    fun getAllNotesFlow(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getNoteByRemoteId(remoteId: String): Note?

    @Query("""
        SELECT * FROM notes
        WHERE
            (deletedAt IS NULL AND (remoteId IS NULL OR syncStatus != 'SYNCED'))
            OR
            (deletedAt IS NOT NULL AND remoteId IS NOT NULL AND syncStatus != 'SYNCED')
    """)
    suspend fun getNotesNeedingSync(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note): Int

    @Query("UPDATE notes SET remoteId = :remoteId, userId = :userId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun markNoteSynced(localId: Int, remoteId: String, userId: String)

    @Query("UPDATE notes SET deletedAt = :deletedAt, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :localId")
    suspend fun softDeleteNote(localId: Int, deletedAt: Long, updatedAt: Long, syncStatus: String)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
