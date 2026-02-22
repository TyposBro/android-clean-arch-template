package com.example.app.app.navigation

object AppDestinations {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val MAIN_HUB = "main_hub"
    const val NOTES_LIST = "notes_list"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val SETTINGS = "settings"

    fun noteDetail(noteId: String) = "note_detail/$noteId"
}
