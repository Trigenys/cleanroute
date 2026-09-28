package com.trigenys.cleanroute.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CleanRouteLightColors = lightColorScheme(
    primary = Color(0xFF006948),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF00855D),
    onPrimaryContainer = Color(0xFFF5FFF7),
    secondary = Color(0xFF006C49),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF6CF8BB),
    onSecondaryContainer = Color(0xFF00714D),
    tertiary = Color(0xFF006194),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCE5FF),
    onTertiaryContainer = Color(0xFF004B73),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF111C2D),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF111C2D),
    surfaceVariant = Color(0xFFD8E3FB),
    onSurfaceVariant = Color(0xFF3D4A42),
    outline = Color(0xFF6D7A72),
    outlineVariant = Color(0xFFBCCAC0),
    inverseSurface = Color(0xFF263143),
    inverseOnSurface = Color(0xFFECF1FF),
    inversePrimary = Color(0xFF68DBA9),
    surfaceTint = Color(0xFF006C4A),
    surfaceBright = Color(0xFFF9F9FF),
    surfaceDim = Color(0xFFCFDAF2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F3FF),
    surfaceContainer = Color(0xFFE7EEFF),
    surfaceContainerHigh = Color(0xFFDEE8FF),
    surfaceContainerHighest = Color(0xFFD8E3FB)
)

private val CleanRouteDarkColors = darkColorScheme(
    primary = Color(0xFF68DBA9),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF005137),
    onPrimaryContainer = Color(0xFF85F8C4),
    secondary = Color(0xFF4EDEA3),
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF005236),
    onSecondaryContainer = Color(0xFF6FFBBE),
    tertiary = Color(0xFF93CCFF),
    onTertiary = Color(0xFF00344F),
    tertiaryContainer = Color(0xFF004B73),
    onTertiaryContainer = Color(0xFFCCE5FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111C2D),
    onBackground = Color(0xFFECF1FF),
    surface = Color(0xFF111C2D),
    onSurface = Color(0xFFECF1FF),
    surfaceVariant = Color(0xFF3D4A42),
    onSurfaceVariant = Color(0xFFBCCAC0),
    outline = Color(0xFF87938A),
    outlineVariant = Color(0xFF3D4A42),
    inverseSurface = Color(0xFFECF1FF),
    inverseOnSurface = Color(0xFF263143),
    inversePrimary = Color(0xFF006948)
)

private val BaseTypography = Typography()

private val CleanRouteTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = BaseTypography.headlineLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.32).sp
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = BaseTypography.headlineSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = BaseTypography.titleSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = BaseTypography.bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = BaseTypography.bodySmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = BaseTypography.labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.14.sp
    ),
    labelMedium = BaseTypography.labelMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.36.sp
    ),
    labelSmall = BaseTypography.labelSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
    )
)

private val CleanRouteShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CleanRouteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CleanRouteDarkColors else CleanRouteLightColors,
        typography = CleanRouteTypography,
        shapes = CleanRouteShapes,
        content = content
    )
}
