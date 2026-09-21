package com.example.core.model

/**
 * The official 7 Class 12 Commerce subjects for Gujarat Board.
 * Chapters will be loaded from official textbook sources in subsequent steps.
 */
data class Subject(
    val id: String,
    val name: String,
    val code: String,
    val iconName: String = "book"
) {
    companion object {
        val OFFICIAL_SUBJECTS = listOf(
            Subject(id = "gujarati", name = "Gujarati", code = "001"),
            Subject(id = "english", name = "English", code = "013"),
            Subject(id = "sp_cc", name = "SP & CC", code = "337"),
            Subject(id = "ba", name = "B.A.", code = "154"),
            Subject(id = "stat", name = "Statistics", code = "135"),
            Subject(id = "accounts", name = "Elements of Accounts", code = "153"),
            Subject(id = "economics", name = "Economics", code = "022")
        )
    }
}
