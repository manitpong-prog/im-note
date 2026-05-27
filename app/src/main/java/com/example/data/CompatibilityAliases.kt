package com.example.data

/**
 * Temporary compatibility aliases while source files are gradually moved from
 * com.example.* to com.imnotesminimal.app.*.
 *
 * These aliases keep the existing Compose screens compiling while the core app
 * package has already been renamed.
 */
typealias Note = com.imnotesminimal.app.data.Note
typealias User = com.imnotesminimal.app.data.User
typealias NoteDao = com.imnotesminimal.app.data.NoteDao
typealias NoteRepository = com.imnotesminimal.app.data.NoteRepository
typealias AppDatabase = com.imnotesminimal.app.data.AppDatabase
