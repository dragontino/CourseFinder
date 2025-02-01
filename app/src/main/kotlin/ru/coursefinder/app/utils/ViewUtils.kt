package ru.coursefinder.app.utils

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import kotlin.math.roundToInt

internal fun View.applyWindowInsets(@InsetsType insets: Int) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insetsCombat ->
        val systemBars = insetsCombat.getInsets(insets)
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insetsCombat
    }
}


internal fun createSpacerDrawable(spaceDp: Int): Drawable {
    val sizePx = dpToPx(spaceDp).roundToInt()
    return GradientDrawable().apply {
        mutate()
        shape = GradientDrawable.RECTANGLE
        setSize(sizePx, sizePx)
    }
}


fun dpToPx(dp: Int): Float {
    return dp * Resources.getSystem().displayMetrics.density
}