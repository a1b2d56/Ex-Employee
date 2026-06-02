package com.powergrid.exemployee.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.powergrid.exemployee.common.ThemePrefs.AppTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.powergrid.exemployee.R

val LocalAppTheme = staticCompositionLocalOf { AppTheme.LIGHT }
val LocalIsBold = staticCompositionLocalOf { false }

val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val FigtreeFont = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold)
)

val OutfitFont = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

val PlusJakartaSansFont = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val SourceSans3Font = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_medium, FontWeight.Medium),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold),
    Font(R.font.source_sans_3_bold, FontWeight.Bold)
)

val NunitoSansFont = FontFamily(
    Font(R.font.nunito_sans_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_medium, FontWeight.Medium),
    Font(R.font.nunito_sans_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_sans_bold, FontWeight.Bold)
)

val WorkSansFont = FontFamily(
    Font(R.font.work_sans_regular, FontWeight.Normal),
    Font(R.font.work_sans_medium, FontWeight.Medium),
    Font(R.font.work_sans_semibold, FontWeight.SemiBold),
    Font(R.font.work_sans_bold, FontWeight.Bold)
)

val ManropeFont = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

@Composable
fun FontWeight.dynamic(): FontWeight {
    return if (LocalIsBold.current) {
        when (this) {
            FontWeight.Normal -> FontWeight.Bold
            FontWeight.Medium -> FontWeight.Bold
            FontWeight.SemiBold -> FontWeight.Bold
            FontWeight.Bold -> FontWeight.Bold
            else -> this
        }
    } else {
        when (this) {
            FontWeight.SemiBold -> FontWeight.Medium
            FontWeight.Bold -> FontWeight.Medium
            else -> this
        }
    }
}

@Composable fun ExEmployeeTheme(
    appTheme: AppTheme = AppTheme.LIGHT,
    fontScale: Float = 1f,
    isBold: Boolean = false,
    fontFamilyKey: String = "default",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> when {
            useDynamic -> dynamicLightColorScheme(context)
            else -> FallbackSkyBlueLightScheme
        }
        AppTheme.DARK -> when {
            useDynamic -> dynamicDarkColorScheme(context)
            else -> FallbackSkyBlueDarkScheme
        }
        AppTheme.MIDNIGHT -> {
            val base = if (useDynamic) dynamicDarkColorScheme(context) else MidnightColorScheme
            // Force pure-black surfaces for AMOLED
            base.copy(
                surface = MidnightColorScheme.surface,
                background = MidnightColorScheme.background,
                surfaceVariant = MidnightColorScheme.surfaceVariant,
                surfaceContainer = MidnightColorScheme.surfaceContainer,
                outlineVariant = MidnightColorScheme.outlineVariant,
            )
        }
        AppTheme.PHANTOM -> PhantomColorScheme
        AppTheme.OBSIDIAN -> ObsidianColorScheme
        AppTheme.ESPRESSO -> EspressoColorScheme
        AppTheme.MATCHA -> MatchaColorScheme
        AppTheme.NORD -> NordColorScheme
        AppTheme.ROSE -> RoseColorScheme
        AppTheme.POWERGRID -> PowerGridColorScheme
    }

    val isDark = appTheme != AppTheme.LIGHT && appTheme != AppTheme.POWERGRID

    // Edge-to-edge system bars
    SideEffect {
        val activity = context as? ComponentActivity ?: return@SideEffect
        val bgArgb = colorScheme.background.toArgb()
        activity.enableEdgeToEdge(
            statusBarStyle = if (isDark) {
                SystemBarStyle.dark(bgArgb)
            } else {
                SystemBarStyle.light(bgArgb, bgArgb)
            },
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(bgArgb)
            } else {
                SystemBarStyle.light(bgArgb, bgArgb)
            },
        )
    }

    val fontFamily = when (fontFamilyKey) {
        "inter" -> InterFont
        "figtree" -> FigtreeFont
        "outfit" -> OutfitFont
        "plus_jakarta_sans" -> PlusJakartaSansFont
        "source_sans_3" -> SourceSans3Font
        "nunito_sans" -> NunitoSansFont
        "work_sans" -> WorkSansFont
        "manrope" -> ManropeFont
        else -> null
    }

    val typography = appTypography(isBold, fontFamily)
    val currentDensity = androidx.compose.ui.platform.LocalDensity.current
    val customDensity = androidx.compose.runtime.remember(currentDensity, fontScale) {
        androidx.compose.ui.unit.Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScale
        )
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalIsBold provides isBold,
        androidx.compose.ui.platform.LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
