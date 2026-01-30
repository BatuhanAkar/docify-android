package com.batuscode.docunote.model

import android.graphics.RectF

data class Markup(
    val page: Int,
    val result: List<List<RectF?>>
)
