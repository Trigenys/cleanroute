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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CleanRouteLightColors = lightColorScheme(
    primary = Color(0xFF176B4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F5E4),
    onPrimaryContainer = Color(0xFF073622),
    secondary = Color(0xFF4D6357),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8D9),
    onSecondaryContainer = Color(0xFF10271D),
    tertiary = Color(0xFF8B5A10),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDB1),
    onTertiaryContainer = Color(0xFF2C1700),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF18201C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18201C),
    surfaceVariant = Color(0xFFE2EAE5),
    onSurfaceVariant = Color(0xFF414944),
    outline = Color(0xFF717A74),
    outlineVariant = Color(0xFFC1CAC4)
)

private val CleanRouteDarkColors = darkColorScheme(
    primary = Color(0xFF9AD4B5),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFFB5F1CF),
    secondary = Color(0xFFB4CCBE),
    onSecondary = Color(0xFF20352A),
    secondaryContainer = Color(0xFF364B3F),
    onSecondaryContainer = Color(0xFFD0E8D9),
    tertiary = Color(0xFFFFB95F),
    onTertiary = Color(0xFF492900),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = Color(0xFFFFDDB1),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE0E4E1),
    surface = Color(0xFF151A17),
    onSurface = Color(0xFFE0E4E1),
    surfaceVariant = Color(0xFF414944),
    onSurfaceVariant = Color(0xFFC1CAC4),
    outline = Color(0xFF8B938E),
    outlineVariant = Color(0xFF414944)
)

private val CleanRouteTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = Typography().titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    ),
    labelLarge = Typography().labelLarge.copy(
        fontWeight = FontWeight.SemiBold
    )
)

private val CleanRouteShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
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
