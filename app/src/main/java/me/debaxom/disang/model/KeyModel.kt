package me.debaxom.disang.model

import android.graphics.RectF

data class KeyModel(
    val label: String,
    val code: Int,
    val weight: Float = 1f,
    val isModifier: Boolean = false
) {
    val bounds = RectF()
}
