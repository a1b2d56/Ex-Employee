package com.powergrid.exemployee.common

import android.content.Context
import androidx.core.content.edit

object FontPrefs {
    private const val PREFS      = "font_prefs"
    private const val KEY_SCALE  = "font_scale"
    private const val KEY_BOLD   = "font_bold"
    private const val KEY_FAMILY = "font_family"

    fun setScale(ctx: Context, scale: Float) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putFloat(KEY_SCALE, scale) }

    fun getScale(ctx: Context): Float =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_SCALE, 1.0f)

    fun setBold(ctx: Context, bold: Boolean) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_BOLD, bold) }

    fun isBold(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_BOLD, false)

    fun setFontFamily(ctx: Context, family: String) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_FAMILY, family) }

    fun getFontFamily(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FAMILY, "default") ?: "default"
}
