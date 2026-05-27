package com.imnotesminimal.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val colorIndex: Int = 0, // Yellow, Blue, Green, Red, Purple, Orange, Gray
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Fields prepared for future offline-first cloud sync.
    // Existing offline notes keep these defaults until real sync is connected.
    val userId: String? = null,
    val remoteId: String? = null,
    val syncStatus: String = "LOCAL",
    val deletedAt: Long? = null
)
