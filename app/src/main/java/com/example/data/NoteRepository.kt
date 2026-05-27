package com.imnotesminimal.app.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotesFlow: Flow<List<Note>> = noteDao.getAllNotesFlow()

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun getNoteByRemoteId(remoteId: String): Note? {
        return noteDao.getNoteByRemoteId(remoteId)
    }

    suspend fun getNotesNeedingSync(): List<Note> {
        return noteDao.getNotesNeedingSync()
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note): Boolean {
        return noteDao.updateNote(note) > 0
    }

    suspend fun markNoteSynced(localId: Int, remoteId: String, userId: String) {
        noteDao.markNoteSynced(localId, remoteId, userId)
    }

    suspend fun softDeleteNote(localId: Int, deletedAt: Long, updatedAt: Long, syncStatus: String) {
        noteDao.softDeleteNote(
            localId = localId,
            deletedAt = deletedAt,
            updatedAt = updatedAt,
            syncStatus = syncStatus
        )
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }
}
