package com.example.tech.foodapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tech.foodapp.R


// Flashy Display font
val DesignerFont = FontFamily(Font(R.font.playfair_display_bold))

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val NunitoVariable = FontFamily(
    Font(
        resId = R.font.nunito_sans_variable,
        // We tell Compose this file handles multiple weights
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400) // Default weight
        )
    )
)

//@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
//val NunitoItalicVariable = FontFamily(
//    Font(
//        resId = R.font.nunito_sans_italic_variable,
//        variationSettings = FontVariation.Settings(
//            FontVariation.weight(400)
//        )
//    )
//)

val AppTypography = Typography(
    // 1. HERO TITLES (Recipe Names, Featured Restaurant)
    displayLarge = TextStyle(
        fontFamily = DesignerFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
//        letterSpacing = (-0.5).sp // Tighter letter spacing makes it look "flashier"
    ),

    displayMedium = TextStyle(
        fontFamily = DesignerFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
    ),

    // 2. SECTION HEADERS (e.g., "Ingredients", "Popular Dishes")
    headlineMedium = TextStyle(
        fontFamily = DesignerFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 25.sp
    ),

    // 3. RECIPE CARDS & SUB-HEADERS
    titleLarge = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.ExtraBold, // Use the 700+ weight here
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.Bold, // Use the 700+ weight here
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),

    titleSmall = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.Bold, // Use the 700+ weight here
        fontSize = 14.sp,
        lineHeight = 16.sp
    ),

    // 4. MAIN READABLE CONTENT (Descriptions, Instructions)
    bodyLarge = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // 5. ITALIC NOTES (e.g., "Chef's tip: add salt")
    bodyMedium = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // 5. ITALIC NOTES (e.g., "Chef's tip: add salt")
    bodySmall = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 13.sp
    ),

    // 6. ACTION & METADATA (Buttons, Prices, Calories)
    labelLarge = TextStyle(
        fontFamily = NunitoVariable,
        fontWeight = FontWeight.ExtraBold, // Makes price/buttons "pop"
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // 7. SMALL UI DETAILS (Prep time, Distance)
    labelSmall = TextStyle(
        fontFamily = NunitoVariable,
        fontSize = 12.sp,
        lineHeight = 15.sp
    )
)