package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class NoteColorProfile(
    val colorIndex: Int,
    val name: String,
    val surface: Color,
    val tagColor: Color,
    val textOnSurface: Color,
    val textSecondaryOnSurface: Color
)

object NoteColors {
    val profiles = listOf(
        NoteColorProfile(
            colorIndex = 0,
            name = "เหลือง",
            surface = Color(0xFFFFF9C4), // Pastel Yellow
            tagColor = Color(0xFFFFF176),
            textOnSurface = Color(0xFF2E2B00),
            textSecondaryOnSurface = Color(0xFF6F6A30)
        ),
        NoteColorProfile(
            colorIndex = 1,
            name = "น้ำเงิน",
            surface = Color(0xFFE3F2FD), // Pastel Blue
            tagColor = Color(0xFF90CAF9),
            textOnSurface = Color(0xFF001E3C),
            textSecondaryOnSurface = Color(0xFF3B5D7E)
        ),
        NoteColorProfile(
            colorIndex = 2,
            name = "เขียว",
            surface = Color(0xFFE8F5E9), // Pastel Green
            tagColor = Color(0xFFA5D6A7),
            textOnSurface = Color(0xFF0D2C11),
            textSecondaryOnSurface = Color(0xFF3F6643)
        ),
        NoteColorProfile(
            colorIndex = 3,
            name = "แดงปะการัง",
            surface = Color(0xFFFFEBEE), // Pastel Red
            tagColor = Color(0xFFEF9A9A),
            textOnSurface = Color(0xFF3C070B),
            textSecondaryOnSurface = Color(0xFF7A3E42)
        ),
        NoteColorProfile(
            colorIndex = 4,
            name = "ม่วง",
            surface = Color(0xFFF3E5F5), // Pastel Purple
            tagColor = Color(0xFFCE93D8),
            textOnSurface = Color(0xFF2A0033),
            textSecondaryOnSurface = Color(0xFF663B70)
        ),
        NoteColorProfile(
            colorIndex = 5,
            name = "ส้ม",
            surface = Color(0xFFFFF3E0), // Pastel Orange
            tagColor = Color(0xFFFFCC80),
            textOnSurface = Color(0xFF3E2723),
            textSecondaryOnSurface = Color(0xFF7B5E57)
        ),
        NoteColorProfile(
            colorIndex = 6,
            name = "เทา",
            surface = Color(0xFFECEFF1), // Pastel Gray
            tagColor = Color(0xFFB0BEC5),
            textOnSurface = Color(0xFF1A2124),
            textSecondaryOnSurface = Color(0xFF546E7A)
        ),
        NoteColorProfile(
            colorIndex = 7,
            name = "ชมพู",
            surface = Color(0xFFFCE4EC), // Pastel Pink
            tagColor = Color(0xFFF48FB1),
            textOnSurface = Color(0xFF310714),
            textSecondaryOnSurface = Color(0xFF73384B)
        )
    )

    fun getProfile(index: Int): NoteColorProfile {
        return profiles.getOrNull(index) ?: profiles[0]
    }
}
